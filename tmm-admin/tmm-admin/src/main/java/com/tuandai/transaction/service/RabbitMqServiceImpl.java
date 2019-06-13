package com.tuandai.transaction.service;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.ConnectionFactory;
import com.tuandai.transaction.bo.*;
import com.tuandai.transaction.dao.ApplicationDao;
import com.tuandai.transaction.dao.DlqServerDao;
import com.tuandai.transaction.dao.TransactionCheckDao;
import com.tuandai.transaction.domain.Application;
import com.tuandai.transaction.domain.DlqServer;
import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.domain.filter.TransactionCheckFilter;
import com.tuandai.transaction.mq.RabbitTemplateFactory;
import com.tuandai.transaction.service.inf.RabbitMqService;
import com.tuandai.transaction.utils.BZStatusCode;
import com.tuandai.transaction.utils.ServiceException;
import com.tuandai.transaction.utils.ThreadPoolExecutorUtils;
import com.tuandai.transaction.utils.TransactionCheckHelper;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

@Service
public class RabbitMqServiceImpl implements RabbitMqService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqServiceImpl.class);

    @Autowired
    @Qualifier("httpRest")
    private RestTemplate restTemplate;

    Pattern pattern = Pattern.compile("^dlq--");

    // 死信队列
    public static final String dlq = "dlq-tmm";

    private Map<String, ConnectionFactory> connectionFactoryCache = new ConcurrentHashMap<>();

    private Map<String, DlqServer> dlqCache = new ConcurrentHashMap<>();

    @Autowired
    private TransactionCheckDao transactionCheckDao;

    @Autowired
    private DlqServerDao dlqServerDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Override
    //@Scheduled(cron = "0/10 * * * * ?")
    public void rabbitmqDLQConsumer() throws IllegalAccessException {
        // 查询现有的死信队列
        List<QueueJson> dlqList = queryDLQList();
        // 消费死信队列消息
        for (QueueJson queue : dlqList) {
            if (queue.getName().equals(dlq)) {
                doRabbitmqNewDLQConsumer(queue);
            } else {
                // 消费死信消息
                doRabbitmqDLQConsumer(queue);
            }
        }
    }

    @Override
    public List<QueueJson>  queryDLQList() {
        ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getTaskThreadPoolExecutorUtils();
        Map<String, RabbitAddress> rabbitAddressMap = RabbitTemplateFactory.getRabbitAddressMap();

        List<Future<List<QueueJson>>> ls = new ArrayList<>();
        for (Map.Entry<String, RabbitAddress> entry : rabbitAddressMap.entrySet()) {
            RabbitAddress value = entry.getValue();
            String ipName = entry.getKey();
            Future<List<QueueJson>> tmp = executor.submit(new Callable<List<QueueJson>>() {
                @Override
                public List<QueueJson> call() throws Exception {
                    List<QueueJson> list = new ArrayList<>();
                    String username = value.getUserName();
                    String password = value.getPassword();
                    String adminUrl = value.getAdminUrl();
                    //String ip = value.getIp();
                    // 通过url接口请求队列列表
                    HttpHeaders header = new HttpHeaders();
                    String base64ClientCredentials = new String(Base64.encodeBase64(new String(username + ":" + password).getBytes()));
                    header.add("Authorization", "Basic " + base64ClientCredentials);
                    ResponseEntity<String> response = restTemplate.exchange(adminUrl + "/api/queues/",
                            HttpMethod.GET, new HttpEntity<String>(header), String.class);
                    if (null != response && HttpStatus.OK.equals(response.getStatusCode())) {
                        String resStr = response.getBody();
                        List<QueueJson> tmplist = JSONObject.parseArray(resStr, QueueJson.class);
                        if (tmplist != null) {
                            for (QueueJson queueJson : tmplist) {
                                if (dlq.equals(queueJson.getName()) || pattern.matcher(queueJson.getName()).find()) {
                                    // 兼容旧版本逻辑， 新版本死信队列是dlq-tmm
                                    queueJson.setIpName(ipName);
                                    queueJson.setAdminUrl(adminUrl);
                                    list.add(queueJson);
                                }
                            }
                        }
                    }
                    return list;
                }
            });
            ls.add(tmp);
        }

        List<QueueJson> list = new ArrayList<QueueJson>();
        // 结果遍历
        for (Future<List<QueueJson>> result : ls) {
            try {
                List<QueueJson> qjs = result.get(5, TimeUnit.SECONDS);
                list.addAll(qjs);
            } catch (InterruptedException e) {
                logger.error("调用mq 控制台未知异常", e);
                result.cancel(true);
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                logger.error("调用mq 控制台未知异常", e);
                result.cancel(true);
            } catch (TimeoutException e) {
                logger.error("调用mq 控制台超时！", e);
                result.cancel(true);
            }
        }
        // logger.info("---------- 查询到的死信队列：" + list);
        return list;
    }

    @Override
    public boolean dlqResend(String queue, List<String> serviceNames) {
        boolean isResult = true;
        while (true) {
            TransactionCheckFilter filter = new TransactionCheckFilter();
            filter.setDlqName(queue);
            filter.setMessageState(MessageState.ABNORMAL.code());
            filter.setIsMessage(1);
            filter.setServiceNames(serviceNames);
            List<TransactionCheck> list = transactionCheckDao.queryTransactionCheckByFilter(filter, new Limiter(0, 1000, "pid ASC"));
            if (CollectionUtils.isEmpty(list) || list.size() == 0) {
                break;
            }
            // 重发
            for (TransactionCheck transactionCheck : list) {
                boolean isSend = false;
                try {
                    sendMessageToQueue(transactionCheck);
                    isSend = true;
                } catch (Exception e) {
                    logger.debug("重发消息失败！");
                    isResult = false;
                }
                if (isSend) {
                    TransactionCheck updateTmp = new TransactionCheck();
                    updateTmp.setPid(transactionCheck.getPid());
                    updateTmp.setUpdateTime(new Date());
                    updateTmp.setMessageState(MessageState.DONE.code());
                    transactionCheckDao.update(updateTmp);
                }
            }
        }
        return isResult;
    }

    @Override
    public List<QueueJson> dlqList(List<String> serviceNames) {
        List<QueueJson> resultList = new ArrayList<>();
        List<TwoTuple<String, Long>> list = transactionCheckDao.dlqList(serviceNames);
        if (!CollectionUtils.isEmpty(list)) {
            for (TwoTuple<String, Long> twoTuple : list) {
                String dlqName = twoTuple.a;
                // 解析相关信息
                String[] pnames = dlqName.split("--");
                String vHost = pnames[1]; // 虚拟host
                QueueJson queueJson = new QueueJson();
                queueJson.setMessages(twoTuple.b == null ? 0 : twoTuple.b);
                queueJson.setName(twoTuple.a);
                queueJson.setVhost(vHost);
                resultList.add(queueJson);
            }
        }
        return resultList;
    }

    @Override
    public boolean addDlqService(String serviceName, String dlqName) {
        List<DlqServer> checkQlq = dlqServerDao.queryDlqServerQueList(serviceName,dlqName);
        if(!CollectionUtils.isEmpty(checkQlq)){
            return false;
        }
        DlqServer dlqServer = new DlqServer();
        Date date = new Date(System.currentTimeMillis());
        dlqServer.setCreateTime(date);
        dlqServer.setUpdateTime(date);
        dlqServer.setServiceName(serviceName);
        dlqServer.setDlqName(dlqName);
        dlqServerDao.addDlqServer(dlqServer);
        return true;
    }

    // 新版本死信队列消费
    private void doRabbitmqNewDLQConsumer(QueueJson queue) throws IllegalAccessException {
        RabbitTemplate rabbitTemplate = RabbitTemplateFactory.getRabbitTemplate(queue.getVhost(), queue.getIpName());
        Long count = queue.getMessages() == null ? 0 : queue.getMessages();
        List<TransactionCheck> daoList = new ArrayList<>();
        // 服务名
        List<Application> applications = new ArrayList<>();

        // 每次最多消费20条
        // count = count > 20 ? 20 : count;
        while (count-- > 0) {
            Message message = rabbitTemplate.receive(queue.getName());
            try {
                if (message != null) {
                    byte[] body = message.getBody();
                    String messageStr = new String(body);
                    MessageProperties properties = message.getMessageProperties();
                    if (properties != null) {
                        // 死信消息则路由键是应用名
                        String serviceName = properties.getReceivedRoutingKey();
                        Map<String, Object> headers = properties.getHeaders();
                        if (!CollectionUtils.isEmpty(headers)) {
                            // 死信消息
                            ArrayList<HashMap<String, String>> x_deaths = (ArrayList<HashMap<String, String>>)headers.get("x-death");
                            String headersExchange = headers.get("exchange") == null ? null : (String)headers.get("exchange");
                            int messageFlag = headers.get("messageFlag") == null ? 0 : (int)headers.get("messageFlag");
                            if (!CollectionUtils.isEmpty(x_deaths)) {
                                HashMap<String, String> x_death = x_deaths.get(0);
                                if (!CollectionUtils.isEmpty(x_death)) {
                                    // 进入死信的原因
                                    String reason = x_death.get("reason");
                                    // 哪个队列产生的
                                    String queueName = x_death.get("queue");
                                    String exchange = x_death.get("exchange");
                                    if (StringUtils.isEmpty(exchange)) {
                                        if (StringUtils.isEmpty(headersExchange)) {
                                            // 无法解析该消息
                                            logger.error("无法解析该死信消息，做丢弃处理", messageStr);
                                            continue;
                                        } else {
                                            exchange = headersExchange;
                                        }
                                    }
                                    daoList.add(buildNewTransactionCheck(messageStr, serviceName, queue, exchange, queueName));
                                }
                            } else if (messageFlag == 1) {
                                // 启动消息
                                // 服务名注册消息
                                if (!StringUtils.isEmpty(messageStr)) {
                                    HashMap<String, String> map = (HashMap<String, String>)JSONObject.parseObject(messageStr, HashMap.class);
                                    if (map != null) {
                                        String applicationName = map.get("applicationName");
                                        if (!StringUtils.isEmpty(applicationName)) {
                                            applications.add(buildApplication(applicationName));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("消费死信消息，位置异常，queue：" + queue + " ,message:" + message);
            }
        }

        if (!CollectionUtils.isEmpty(daoList)) {
            transactionCheckDao.insertBatch(daoList);
        }
        if (!CollectionUtils.isEmpty(applications)) {
            applicationDao.insertBatch(applications);
        }
    }

    public Application buildApplication(String serviceName) {
        Date date = new Date();
        Application service = new Application();
        service.setApplicationName(serviceName);
        service.setCreateTime(date);
        service.setUpdateTime(date);
        return service;
    }

    // 旧版本死信队列消费
    private void doRabbitmqDLQConsumer(QueueJson queue) throws IllegalAccessException {
        if(queue == null){
            return;
        }
        RabbitTemplate rabbitTemplate = RabbitTemplateFactory.getRabbitTemplate(queue.getVhost(), queue.getIpName());
        Long count = queue.getMessages() == null ? 0 : queue.getMessages();
        List<TransactionCheck> daoList = new ArrayList<>();
        String str = null;
        DlqServer dlqServer = dlqServerDao.queryDlqServerByQueueName(queue.getName());
        if (dlqServer == null) {
            logger.warn("没有配置服务名和死信队列的关系，死信队列名：{}" , queue.getName());
            return;
        }
        while (count-- > 0) {
            Object mesg = null;
            try {
                mesg = rabbitTemplate.receiveAndConvert(queue.getName());
                if (mesg == null) {
                    break;
                }
                if (mesg instanceof String) {
                    str = (String) mesg;
                } else if(mesg instanceof byte[]) {
                    byte strByte[] = (byte[]) mesg;
                    str = new String(strByte);
                } else {
                    logger.error("消费死信队列消息类型异常,类型为：" , mesg.getClass().getName());
                    break;
                }
            } catch (Exception e) {
                logger.error("消费死信队列消息异常，死信消息:" , mesg, e);
            }
            daoList.add(buildTransactionCheck(queue, str, dlqServer));
            if (daoList.size() > 100) {
                break;
            }
        }
        if (!CollectionUtils.isEmpty(daoList)) {
            transactionCheckDao.insertBatch(daoList);
        }
    }

    public void sendMessageToQueue(TransactionCheck transactionMessage) throws IllegalAccessException {
        if (transactionMessage == null || transactionMessage.getMessageTopic() == null) {
            logger.error("transactionMessage or transactionMessage.getMessageTopic() is not null...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }

        RabbitMQTopic rabbitTopic = JSONObject.parseObject(transactionMessage.getMessageTopic(), RabbitMQTopic.class);
        if (rabbitTopic == null || rabbitTopic.getQueue() == null || rabbitTopic.getvHost() == null) {
            logger.error("rabbitTopic or properties is not null...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }

        RabbitTemplate rabbitTemplate = RabbitTemplateFactory.getRabbitTemplate(rabbitTopic.getvHost(),
                TransactionCheckHelper.clusterIpStr2ClusterIp(transactionMessage.getClusterIp()));
        MessageProperties message = new MessageProperties();
        // 重发的时候设置业务方路由交换机
        message.setHeader("exchange", rabbitTopic.getExchange());
        rabbitTemplate.send(rabbitTopic.getQueue(), new Message(transactionMessage.getMessage().getBytes(), message));
    }


    private TransactionCheck buildNewTransactionCheck(String messageStr, String serviceName, QueueJson queue, String exchange, String queueName) throws IllegalAccessException {
        MessageJson message = JSONObject.parseObject(messageStr, MessageJson.class);
        // 入库
        Date date = new Date();
        TransactionCheck transactionState = new TransactionCheck();
        transactionState.setuId(message.getUid());
        transactionState.setServiceName(serviceName);
        transactionState.setCreateTime(date);
        transactionState.setUpdateTime(date);
        transactionState.setMessageState(MessageState.ABNORMAL.code()); // 消费异常
        transactionState.setMessageSendThreshold(Thresholds.MAX_SEND.code()); // 最大重试次数
        transactionState.setMessageSendTimes(0);
        transactionState.setMessageNextSendTime(new Date());
        transactionState.setMessageNextSendTime(date);
        transactionState.setPresendBackNextSendTime(new Date());
        transactionState.setPresendBackUrl("");
        transactionState.setPresendBackMethod("");
        transactionState.setPresendBackThreshold(0); // 不需要回调
        transactionState.setPresendBackSendTimes(0);
        transactionState.setMessage(messageStr);
        ClusterIp ci = RabbitTemplateFactory.getRabbitAddress(queue.getIpName()).toClusterIp();
        transactionState.setClusterIp(JSONObject.toJSONString(ci));
        String name = "dlq--" + queue.getVhost() + "--" + exchange + "--" + queueName;
        transactionState.setDlqName(name);
        transactionState.setMqType(MqType.RABBIT.value());

        RabbitMQTopic topic = new RabbitMQTopic();
        // 生产者队列名
        topic.setExchange(exchange);
        topic.setExchangeType("fanout");
        topic.setvHost(queue.getVhost());
        topic.setQueue(queueName);
        transactionState.setMessageTopic(JSONObject.toJSONString(topic));
        return transactionState;
    }

    private TransactionCheck buildTransactionCheck(QueueJson queue,String messageStr, DlqServer dlqServer) throws IllegalAccessException {
        MessageJson message = JSONObject.parseObject(messageStr, MessageJson.class);
        // 入库
        Date date = new Date();
        TransactionCheck transactionState = new TransactionCheck();
        transactionState.setuId(message.getUid());
        transactionState.setServiceName(dlqServer.getServiceName());
        transactionState.setCreateTime(date);
        transactionState.setUpdateTime(date);
        transactionState.setMessageState(MessageState.ABNORMAL.code()); // 消费异常
        transactionState.setMessageSendThreshold(Thresholds.MAX_SEND.code()); // 最大重试次数
        transactionState.setMessageSendTimes(0);
        transactionState.setMessageNextSendTime(new Date());
        transactionState.setMessageNextSendTime(date);
        transactionState.setPresendBackNextSendTime(new Date());
        transactionState.setPresendBackUrl("");
        transactionState.setPresendBackMethod("");
        transactionState.setPresendBackThreshold(0); // 不需要回调
        transactionState.setPresendBackSendTimes(0);
        transactionState.setMessage(messageStr);
        ClusterIp ci = RabbitTemplateFactory.getRabbitAddress(queue.getIpName()).toClusterIp();
        transactionState.setClusterIp(JSONObject.toJSONString(ci));
        transactionState.setDlqName(queue.getName());
        transactionState.setMqType(MqType.RABBIT.value());

        RabbitMQTopic topic = new RabbitMQTopic();
        // 生产者队列名
        String[] pnames = queue.getName().split("--");
        String dqueue = pnames[3]; // 要发送的队列名称
        String vHost = pnames[1]; // 虚拟host
        String vExchange = pnames[2];
        topic.setExchange(vExchange);
        topic.setExchangeType("fanout");
        topic.setvHost(vHost);
        topic.setQueue(dqueue);
        transactionState.setMessageTopic(JSONObject.toJSONString(topic));
        return transactionState;
    }

}

