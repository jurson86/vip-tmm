package com.tuandai.transaction.producer.core;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.bo.*;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.mq.inf.MqService;
import com.tuandai.transaction.producer.config.RabbitConfig;
import com.tuandai.transaction.producer.model.ExchangeType;
import com.tuandai.transaction.producer.model.RabbitMQTopic;
import com.tuandai.transaction.producer.utils.Constants;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.util.*;


public class RabbitMqServiceImpl implements MqService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqServiceImpl.class);

    private static MessageDigest md5 = null;
    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
            RabbitConfig tmmConfig = (RabbitConfig)SettingSupport.getTmmConfig();
            // 初始化 mq
            RabbitTemplateFactory.init(tmmConfig.getRabbitAddressMap());
        } catch (Exception e) {
            logger.info("加载 RabbitMqServiceImpl 失败....");
        }
    }

    private static Exchange getExchangeByType(String exchangeTypeStr, String exchangeName) throws Exception {
        if (StringUtils.isEmpty(exchangeTypeStr)) {
            logger.error("not exchangeTypeStr.....");
            throw new Exception();
        }
        ExchangeType exchangeType = ExchangeType.findByDes(exchangeTypeStr);

        if (exchangeType == null) {
            logger.error("not ExchangeType.....");
            throw new Exception();
        }
        
        if (exchangeType == ExchangeType.FANOUT) {
            return new FanoutExchange(exchangeName);
        } else if (exchangeType == ExchangeType.HEADRES) {
            return new HeadersExchange(exchangeName);
        } else if (exchangeType == ExchangeType.DIRECT) {
            return new DirectExchange(exchangeName);
        } else if (exchangeType == ExchangeType.TOPIC) {
            return new TopicExchange(exchangeName);
        }
        return null;
    }

    private boolean checkTopic(RabbitMQTopic joTopic) {
        if(StringUtils.isEmpty(joTopic.getvHost()) || StringUtils.isEmpty(joTopic.getExchange()) || StringUtils.isEmpty(joTopic.getExchangeType())) {
            logger.warn("invalid mq topic : {}", joTopic);
            return false;
        }
        return true;
    }

    @Override
    public void sendMessage(EventDefinition eventDefinition) throws Exception {

        RabbitMQTopic joTopic = JSONObject.parseObject(eventDefinition.getTopic(), RabbitMQTopic.class);

        if (!checkTopic(joTopic)) {
            return;
        }

        long time2 = System.currentTimeMillis();
        String ip = joTopic.getIp();
        String exchangeName = joTopic.getExchange();
        String vHost = joTopic.getvHost();
        String exchangeType = joTopic.getExchangeType();
        String routeKey = joTopic.getRouteKey();
        boolean isCustomExchange = joTopic.isCustomExchange();

        long time3 = System.currentTimeMillis();
        RabbitTemplate rabbitTemplate = RabbitTemplateFactory.getRabbitTemplate(vHost, ip);
        long now3 = System.currentTimeMillis();
        logger.debug("RabbitTemplateFactory.getRabbitTemplate ,uid:" + eventDefinition.getUid() +",花费时间：" + (now3-time3));

        if (isDeclare(isCustomExchange)) {
            long time = System.currentTimeMillis();
            // 非自定义
            RabbitAdmin rabbitAdmin = RabbitTemplateFactory.getRabbitAdmin(vHost, ip);
            // 根据不同类型生成交换机
            Exchange exchange = getExchangeByType(exchangeType, exchangeName);
            rabbitAdmin.declareExchange(exchange);
            long now = System.currentTimeMillis();
            logger.debug("rabbitAdmin.declareExchange ,uid:" + eventDefinition.getUid() +",花费时间：" + (now-time));
        }

        String uid = eventDefinition.getUid();
        long time = System.currentTimeMillis();
        CorrelationData correlationData = new CorrelationData(uid);

        String topicStr = eventDefinition.getTopic();
        String strMd5 = getMd5(topicStr);
        // 转化消息
        Message message = convertMessageIfNecessary(eventDefinition , time, strMd5);

        rabbitTemplate.convertAndSend(exchangeName, routeKey, message, correlationData);

        long now = System.currentTimeMillis();

        String log = "{\"type\":\"tmmProduce\",\"producer\":\"{}\",\"ptime\":\"{}\", \"uid\":\"{}\",\"st\":\"{}\", \"traceId\":\"{}\", \"spanId\":\"{}\"}";

        logger.info(log, strMd5, time, uid, now - time, eventDefinition.getTraceId(), eventDefinition.getSpanId());
        long now2 = System.currentTimeMillis();
        logger.debug("rabbitTemplate.sendMessage, uid:" + eventDefinition.getUid() +" 花费时间：" + (now2-time2));
    }

    @Override
    public MqType mqType() {
        return MqType.RABBIT;
    }

    @Override
    public List<EventDefinition> mergeEventDefinition(Map<String, EventDefinition> eventDefinitionMap) {
        return doMergeEventDefinition(eventDefinitionMap);
    }

    private List<EventDefinition> doMergeEventDefinition(Map<String, EventDefinition> checkMap) {
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
                EventDefinition eventDefinition = new EventDefinition();
                List<EventDefinition> list = entry.getValue();
                StringBuilder builder = new StringBuilder();
                for (EventDefinition event : list) {
                    builder.append(JSONObject.toJSONString(event)).append("\n");
                }
                eventDefinition.setMessage(builder.toString());

                RabbitMQTopic tmp = RabbitMQTopic.newRabbitMQTopicBuilder()
                        .ip(entry.getKey()).exchange(Constants.CHECK_EXCHANGE).build();
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

    private boolean isDeclare(boolean isCustomExchange) {
        return !isCustomExchange;
    }


    private Message convertMessageIfNecessary(EventDefinition eventDefinition, long now, String topicMd5) {
        // 转化为message 对象
        MessageProperties messageProperties = null;
        Message message = null;

        if (StringUtils.isEmpty(eventDefinition.getMessageProperties())) {
            messageProperties = new MessageProperties();
        } else {
            messageProperties = JSONObject.parseObject(eventDefinition.getMessageProperties(), MessageProperties.class);
        }

        // 设置消息头属性
        messageProperties.setHeader("uid", eventDefinition.getUid());
        messageProperties.setHeader("producer", topicMd5);
        messageProperties.setHeader("ptime", now);
        Map<String, Object> headers = messageProperties.getHeaders();
        if (headers != null) {
            if (headers.get("spanId") == null) {
                messageProperties.setHeader("spanId", eventDefinition.getSpanId());
            }
            if (headers.get("traceId") == null) {
                messageProperties.setHeader("traceId", eventDefinition.getTraceId());
            }
        }

        byte[] body = (eventDefinition.getMessage()==null ? "" : eventDefinition.getMessage()).getBytes();

        message = new Message(body, messageProperties);

        return message;
    }

    public static String getMd5(String str) {
        return DigestUtils.md5Hex(str);
    }


}
