package com.tuandai.transaction.client.service;

import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.config.TMMConfig;
import com.tuandai.transaction.client.model.MqLog;
import com.tuandai.transaction.client.service.inf.*;
import com.tuandai.transaction.client.model.BeginLog;
import com.tuandai.transaction.client.model.EndLog;
import com.tuandai.transaction.client.mq.DefaultMqServiceFactory;
import com.tuandai.transaction.client.mq.MqSender;
import com.tuandai.transaction.client.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 日志事件处理器
 */
public class TMMServiceImpl extends SimpleEventDefinitionRegistry implements TMMService {

    private static final Logger logger = LoggerFactory.getLogger(TMMServiceImpl.class);

    private LogEventService logEventService = null;

    private LogAnalyzerService logAnalyzerService;

    private DatadirCleanupManager datadirCleanupManager;

    private TMMServiceThread tMMServiceThread;

    private ZabbixService zabbixService;

    private MqSender mqSender;

    private TMMPublisherImpl tMMPublisherImpl;

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
        this.tMMPublisherImpl = new TMMPublisherImpl();
    }

    //---------------------------------------------- TMMService 接口实现 -----------------------------------------------

    @Override
    public Boolean sendTransBeginToFlume(BeginLog beginLog) {
        logger.debug("TMMServiceImpl.sendTransBeginToFlume(), beginLog:" + beginLog);
        TMMServiceHelper.checkBeginLogParam(beginLog);
        logEventService.writeLogEvent(TMMServiceHelper.beginLog2EventDefinition(beginLog, SettingSupport.isTmmMessage()));
        return true;
    }

    @Override
    public Boolean sendTransEndToFlume(EndLog endLog) {
        logger.debug("TMMServiceImpl.sendTransEndToFlume(), endLog:" + endLog);
        TMMServiceHelper.checkEndLogParam(endLog);
        logEventService.writeLogEvent(TMMServiceHelper.endLog2EventDefinition(endLog, SettingSupport.isTmmMessage()));
        return true;
    }

    @Override
    public Boolean sendNTrans(MqLog mqLog) {
        logger.debug("TMMServiceImpl.sendNTrans(), mqLog:" + mqLog);
        TMMServiceHelper.checkMqLogParam(mqLog);
        logEventService.writeLogEvent(TMMServiceHelper.mqLog2EventDefinition(mqLog, SettingSupport.isTmmMessage()));
        return true;
    }

    @Override
    public Map<String, Long> monitorData() {
        Map<String, Long> result = zabbixService.allMonitor();
        long beginSize = (long)(this.getBeginMap().size());
        long endSize = (long)(this.getEndMap().size());
        long mqSize =  (long)(this.getMqMap().size());
        long checkSize = (long)(this.getCheckMap().size());
        result.put("beginMap", beginSize);
        result.put("endMap",endSize);
        result.put("mqMap", mqSize);
        result.put("checkMap", checkSize);
        return result;
    }

    @Override
    public void start(TMMConfig tmmConfig)  {
        // 初始化配置器
        SettingSupport.init(tmmConfig);
        // 初始化所有的配置参数
        this.init();
        // 启动线程
        tMMServiceThread.start();
        // 启动删除文件定时任务
        datadirCleanupManager.start();
        // tmm启动事件
        tMMPublisherImpl.publishEvent(new TMMEvent(this));
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
                List<EventDefinition> checkEvents = mqSender.mergeEventDefinition(this.getCheckDefinitionMap());
                mqSender.sendMq(checkEvents);

                // 发送确认失败的消息
                List<EventDefinition> confirmFailList = getResendMq(batchSize);
                mqSender.sendMq(confirmFailList, new MqSender.SendMqHandler() {
                    @Override
                    public EventDefinition preProcess(EventDefinition preEventDefinition) {
                        if (retryMap.containsKey(preEventDefinition.getUid())) {
                            retryMap.put(preEventDefinition.getUid(), retryMap.get(preEventDefinition.getUid()) + 1);
                        }
                        return preEventDefinition;
                    }
                });

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
        logger.info("========================== tmm close! If the tmm terminates unexpectedly, check for [/tmm_vhost] problems or other issues ....");
        System.exit(1);
    }

    private List<EventDefinition> getResendMq(int batchSize) {
        // mq.map 存储的是多个批次的尚未被确认的数据，当他的数量累积到 batchSize的一半时候会发送线程会休眠一段时间等待mq服务器发送确认。当累积到batchSize或者更大的时候，则重发当前批次的所有消息
        Map<String, EventDefinition> mqMap = this.getMqMap();
        // 如果超过一半的没有确认成功，则阻塞一段时间等待确认
        if (mqMap.size() * 2 > batchSize) {
            // 如果没有确认成功的比当前批量值还大则全部重发这一批消息
            if (mqMap.size() > batchSize) {
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

}
