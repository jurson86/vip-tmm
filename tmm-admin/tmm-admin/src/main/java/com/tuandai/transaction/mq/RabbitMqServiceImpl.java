package com.tuandai.transaction.mq;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.tuandai.transaction.bo.ExchangeType;
import com.tuandai.transaction.bo.RabbitMQTopic;
import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.mq.inf.MqService;
import com.tuandai.transaction.utils.BZStatusCode;
import com.tuandai.transaction.utils.ServiceException;
import com.tuandai.transaction.utils.TransactionCheckHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.client.RestTemplate;


public class RabbitMqServiceImpl implements MqService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqServiceImpl.class);

    private RestTemplate restTemplate;

    public RabbitMqServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RabbitMqServiceImpl() {
    }

    @Override
    public void sendMessage(TransactionCheck transactionMessage) throws IllegalAccessException {

        if (transactionMessage == null || transactionMessage.getMessageTopic() == null) {
            logger.error("transactionMessage or transactionMessage.getMessageTopic() is not null...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }

        RabbitMQTopic rabbitTopic = JSONObject.parseObject(transactionMessage.getMessageTopic(), RabbitMQTopic.class);

        if (rabbitTopic == null || rabbitTopic.getvHost() == null || rabbitTopic.getExchange() == null || rabbitTopic.getExchangeType() == null) {
            logger.error("rabbitTopic or properties is not null...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }

        RabbitTemplate rabbitTemplate = RabbitTemplateFactory.getRabbitTemplate(rabbitTopic.getvHost(),
                TransactionCheckHelper.clusterIpStr2ClusterIp(transactionMessage.getClusterIp()));
        ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        // 根据不同类型生成交换机
        rabbitAdmin.declareExchange(getExchangeByType(rabbitTopic));
        RabbitMqMessage message = new RabbitMqMessage();
        message.setMessage(transactionMessage.getMessage());
        message.setUid(transactionMessage.getuId());
        rabbitTemplate.convertAndSend(rabbitTopic.getExchange(), rabbitTopic.getRouteKey(), JSONObject.toJSONString(message));
    }

    private Exchange getExchangeByType(RabbitMQTopic rabbitTopic) {
        String type = rabbitTopic.getExchangeType();
        if (type == null) {
            return new FanoutExchange(rabbitTopic.getExchange());
        }
        ExchangeType exchangeType = ExchangeType.findByDes(type);
        if (exchangeType == null) {
            logger.error("not ExchangeType.....");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_CONVERSION);
        }
        
        if (exchangeType == ExchangeType.FANOUT) {
            return new FanoutExchange(rabbitTopic.getExchange());
        } else if (exchangeType == ExchangeType.HEADRES) {
            return new HeadersExchange(rabbitTopic.getExchange());
        } else if (exchangeType == ExchangeType.DIRECT) {
            return new DirectExchange(rabbitTopic.getExchange());
        } else if (exchangeType == ExchangeType.TOPIC) {
            return new TopicExchange(rabbitTopic.getExchange());
        }
        return null;
    }

    class RabbitMqMessage {
        private String uid;

        private String message;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
