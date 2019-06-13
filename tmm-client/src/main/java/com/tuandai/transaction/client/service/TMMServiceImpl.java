package com.tuandai.transaction.client.service;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.bo.RabbitAddress;
import com.tuandai.transaction.client.bo.StartInfo;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.model.MqLog;
import com.tuandai.transaction.client.service.inf.*;
import com.tuandai.transaction.client.model.BeginLog;
import com.tuandai.transaction.client.model.EndLog;
import com.tuandai.transaction.client.model.RabbitMQTopic;
import com.tuandai.transaction.client.mq.DefaultMqServiceFactory;
import com.tuandai.transaction.client.mq.MqSender;
import com.tuandai.transaction.client.mq.rabbitmq.RabbitTemplateFactory;
import com.tuandai.transaction.client.utils.ConstantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 日志事件处理器
 */
public class TMMServiceImpl extends SimpleEventDefinitionRegistry implements TMMService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(TMMServiceImpl.class);

    private LogEventService logEventService = null;

    private LogAnalyzerService logAnalyzerService;

    private DatadirCleanupManager datadirCleanupManager;

    private TMMServiceThread tMMServiceThread;

    private ZabbixService zabbixService;

    private MqSender mqSender;

    private volatile boolean stop = false;

    public TMMServiceImpl() {
    }

    @Override
    public void init() {
        super.init();
        this.logEventService =  new LogEventServiceImpl();
        this.logAnalyzerService = new LogAnalyzerServiceImpl(this);
        this.datadirCleanupManager = new DatadirCleanupManager();
        this.tMMServiceThread = new TMMServiceThread();
        this.zabbixService = new ZabbixServiceImpl();
        this.mqSender = new MqSender(new DefaultMqServiceFactory());
    }

    //---------------------------------------------- TMMService 接口实现 -----------------------------------------------

    @Override
    public Boolean sendTransBeginToFlume(BeginLog beginLog) {
        logger.debug("TMMServiceImpl.sendTransBeginToFlume(), beginLog:" + beginLog);
        TMMServiceHelper.checkBeginLogParam(beginLog);
        logEventService.writeLogEvent(TMMServiceHelper.beginLog2EventDefinition(beginLog));
        return true;
    }

    @Override
    public Boolean sendTransEndToFlume(EndLog endLog) {
        logger.debug("TMMServiceImpl.sendTransEndToFlume(), endLog:" + endLog);
        TMMServiceHelper.checkEndLogParam(endLog);
        logEventService.writeLogEvent(TMMServiceHelper.endLog2EventDefinition(endLog));
        return true;
    }

    @Override
    public Boolean sendNTrans(MqLog mqLog) {
        logger.debug("TMMServiceImpl.sendNTrans(), mqLog:" + mqLog);
        TMMServiceHelper.checkMqLogParam(mqLog);
        logEventService.writeLogEvent(TMMServiceHelper.mqLog2EventDefinition(mqLog));
        return true;
    }

    @Override
    public void shutdown() {
        stop = true;
        try {
            SettingSupport.close();
        } catch (Exception e) {
            logger.error("SettingSupport_close() fail ...");
        }
    }

    @Override
    public Map<String, Integer> monitorData() {
        Map<String, Integer> result = zabbixService.allMonitor();
        int beginSize = this.getBeginMap().size();
        int endSize = this.getEndMap().size();
        int mqSize =  this.getMqMap().size();
        int checkSize = this.getCheckMap().size();
        result.put("beginMap", beginSize);
        result.put("endMap",endSize);
        result.put("mqMap", mqSize);
        result.put("checkMap", checkSize);
        return result;
    }

    // ----------------------------------------------- ApplicationContextAware 接口实现 --------------------------------

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 初始化配置器
        SettingSupport.init(applicationContext);
        // 初始化所有的配置参数
        this.init();
        // 初始化 mq
        RabbitTemplateFactory.init(SettingSupport.getRabbitAddressMap());
        // 初始化后观察者
        // 启动线程
        tMMServiceThread.start();
        // 启动删除文件定时任务
        datadirCleanupManager.start();
        // 发送启动信息到指定exchange
        sendAgentStartInfo();
    }

    // -----------------------------------------------------------------------------------------------------------------

    class TMMServiceThread extends Thread {
        @Override
        public void run() {
            setName("tmm-mq-service");
            executor();
        }
    }

    public void executor() {
        // 加载磁盘数据
        this.loadAllPersistentEventDefinition();
        // 加载checkpoint数据
        logEventService.loadCheckpoint();
        int batchSize = SettingSupport.getBatchSize();

        logger.info("tmm_init_RpcDirCanonicalPath ：" + SettingSupport.getRpcDirCanonicalPath());
        logger.info("tmm_init_BatchSize ：" + SettingSupport.getBatchSize());
        logger.info("tmm_init_RabbitAddressMap ：" + SettingSupport.getRabbitAddressMap());
        logger.info("tmm_init_RpcSize ：" + SettingSupport.getRpcSize());
        logger.info("tmm_init_SendMqCorePoolThreadNum ：" + SettingSupport.getSendMqCorePoolThreadNum());
        logger.info("tmm_init_SendMqQueueSize ：" + SettingSupport.getSendMqQueueSize());
        logger.info("tmm_init_SendMqMaxPoolThreadNum ：" + SettingSupport.getSendMqMaxPoolThreadNum());

        while (!stop) {
            try {
                Thread.sleep(500);

                long startTime = System.currentTimeMillis();
                // 读取数据
                List<EventDefinition> events = logEventService.readLogEvent(batchSize);
                // 分析发送mq
                mqSender.sendMq(events, new MqSender.SendMqHandler() {
                    @Override
                    public EventDefinition preProcess(EventDefinition preEventDefinition) {
                        // 发送前先分析日志
                        EventDefinition resultEventDefinition = logAnalyzerService.analysis(preEventDefinition);
                        logger.debug("MqSender.sendMq().preProcess(), 分析结果为：" + resultEventDefinition);
                        return resultEventDefinition;
                    }
                });


                // 发送check数据
                List<EventDefinition> checkEvents = getCheckStr(this.getCheckDefinitionMap());
                mqSender.sendMq(checkEvents);

                // 发送确认失败的消息
                List<EventDefinition> confirmFailList = getResendMq(batchSize);
                mqSender.sendMq(confirmFailList);

                // 持久化
                this.persistentEventDefinition();
                // 更新checkPoint
                boolean persistent = logEventService.persistentCheckpoint();
                long time = System.currentTimeMillis() - startTime;
                if (persistent && time > 30) {
                    logger.info(" write log excuse time =============>：[" + time + "ms]");
                }
            } catch (Exception e) {
                logger.warn("error: {}", e);
                // 重置checkpoint文件
                logEventService.resetCheckpoint();
            }
        }
        logger.error("========================== tmm start error!  Please check whether mq cluster has [/tmm_host] or there are other reasons for the tmm...");
        System.exit(1);
    }

    private List<EventDefinition> getCheckStr(Map<String, EventDefinition> checkMap) {
        if (checkMap.size() <= 0) {
            return null;
        }

        Map<String, List<EventDefinition>> mapGroup = new HashMap<>();

        for (Map.Entry<String, EventDefinition> entry : checkMap.entrySet()) {
            EventDefinition event = entry.getValue();
            RabbitMQTopic rabbitMQTopic = JSONObject.parseObject(event.getTopic(), RabbitMQTopic.class);
            if (mapGroup.containsKey(rabbitMQTopic.getIp())) {
                mapGroup.get(rabbitMQTopic.getIp()).add(event);
            } else {
                List<EventDefinition> list = new ArrayList<>();
                list.add(event);
                mapGroup.put(rabbitMQTopic.getIp(), list);
            }
        }

        List<EventDefinition> resultList = new ArrayList<>();
        if (mapGroup.size() > 0) {
            for (Map.Entry<String, List<EventDefinition>> entry : mapGroup.entrySet()) {
                // String result = "";
                EventDefinition eventDefinition = new EventDefinition();
                List<EventDefinition> list = entry.getValue();
                StringBuilder builder = new StringBuilder();
                for (EventDefinition event : list) {
                    // result = result + JSONObject.toJSONString(event)  + "\n";
                    builder.append(JSONObject.toJSONString(event)).append("\n");
                }
                // eventDefinition.setMessage(result);
                eventDefinition.setMessage(builder.toString());

                RabbitMQTopic tmp = RabbitMQTopic.newRabbitMQTopicBuilder()
                        .ip(entry.getKey()).exchange(ConstantUtils.CHECK_EXCHANGE).build();
                eventDefinition.setTopic(JSONObject.toJSONString(tmp));
                String msgId = UUID.randomUUID().toString();
                eventDefinition.setUid(msgId);
                resultList.add(eventDefinition);
            }
        }

        if (!CollectionUtils.isEmpty(resultList)) {
            logger.debug("TMMServiceImpl.getCheckStr(), 需要发送check的消息为：" + resultList);
        }
        return resultList;
    }

    private List<EventDefinition> getResendMq(int batchSize) {
        Map<String, EventDefinition> mqMap = this.getMqMap();
        if (mqMap.size() * 2 > batchSize) { // 如果超过一半的没有确认成功，则阻塞一段时间等待确认
            if (mqMap.size() > batchSize) { // 如果没有确认成功的比当前批量值还大则全部重发这一批消息
                throw new IllegalArgumentException("mqMap 过大，checkpoint重置！");
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.error("阻塞mqMap失败", e);
            }
        }

        List<EventDefinition> result = new ArrayList<>();
        Iterator<Map.Entry<String, EventDefinition>> iterator = mqMap.entrySet().iterator();
        long dt = System.currentTimeMillis() - 60000;
        while(iterator.hasNext()) {
            Map.Entry<String, EventDefinition> entry = iterator.next();
            EventDefinition value = entry.getValue();
            // 重试得范畴
            if (value.getGoMapTime() <  dt) {
                result.add(entry.getValue());
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            logger.info("TMMServiceImpl.getResendMq(),需要重发的消息为：" + result);
        }
        return result;
    }


    private void sendAgentStartInfo() {
        EventDefinition eventDefinition = null;
        try {
            Map<String, RabbitAddress> map = SettingSupport.getRabbitAddressMap();
            if (!CollectionUtils.isEmpty(map)) {
                for (Map.Entry<String, RabbitAddress> tmp : map.entrySet()) {
                    String ipName = tmp.getKey();
                    eventDefinition = new EventDefinition();
                    eventDefinition.setMessage(JSONObject.toJSONString(new StartInfo(SettingSupport.getServerName(), SettingSupport.getPrefixUrl())));
                    RabbitMQTopic rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
                            .ip(ipName).exchange(ConstantUtils.START_EXCHANGE).build();
                    eventDefinition.setTopic(JSONObject.toJSONString(rabbitMQTopic));
                    eventDefinition.setUid(UUID.randomUUID().toString());
                    mqSender.sendMq(eventDefinition);
                }
            }
        } catch (Exception e) {
            this.shutdown();
            logger.error("tmm_send_begin_mq_fail ....{}, {}", eventDefinition, e);
        }
    }

}
