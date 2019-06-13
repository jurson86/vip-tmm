package com.tuandai.transaction.client.mq.rabbitmq;

import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.service.inf.EventDefinitionRegistry;
import com.tuandai.transaction.client.service.TMMServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

public class ConfirmCallback implements RabbitTemplate.ConfirmCallback {

    private static final Logger logger = LoggerFactory.getLogger(ConfirmCallback.class);

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        logger.debug("收到确认消息，uid：" + correlationData.getId() + " , ack:" + ack + " ,cause:" + cause);
        if (ack) {
            EventDefinitionRegistry registry = SettingSupport.context.getBean(TMMServiceImpl.class);
            EventDefinition remove = registry.removeEventDefinition(correlationData.getId(), EventDefinition.EventType.MQ);
            if (remove == null) {
                logger.debug("确认消息，删除mqMap数据：" + correlationData.getId());
            }
        } else {
            logger.warn("发送mq确认失败,cause:" + cause);
        }
    }

}
