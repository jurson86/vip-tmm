package com.tuandai.transaction.producer.core;

import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.service.TMMServiceImpl;
import com.tuandai.transaction.client.service.inf.EventDefinitionRegistry;
//import com.tuandai.transaction.producer.config.ApplicationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

public class ConfirmCallback implements RabbitTemplate.ConfirmCallback {

    private static final Logger logger = LoggerFactory.getLogger(ConfirmCallback.class);

    public static TMMServiceImpl tmmService = null;

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        logger.debug("收到确认消息，uid：" + correlationData.getId() + " , ack:" + ack + " ,cause:" + cause);
        if (ack && tmmService != null) {
            EventDefinitionRegistry registry = tmmService;
            EventDefinition remove = registry.removeEventDefinition(correlationData.getId(), EventDefinition.EventType.MQ);
            if (remove == null) {
                logger.warn("收到非发送的消息确认, uid:{}", correlationData.getId());
            }
        } else {
            logger.warn("发送mq确认失败,cause:" + cause);
        }
    }

}
