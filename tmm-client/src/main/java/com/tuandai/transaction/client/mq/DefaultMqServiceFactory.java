package com.tuandai.transaction.client.mq;

import com.tuandai.transaction.client.bo.MqType;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.mq.inf.MqService;
import com.tuandai.transaction.client.mq.rabbitmq.RabbitMqServiceImpl;
import com.tuandai.transaction.client.mq.rocketmq.RocketMqServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultMqServiceFactory extends AbstractMqServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMqServiceFactory.class);

    public DefaultMqServiceFactory() {
        super(SettingSupport.getMqType());
    }

    @Override
    public MqService createMqService() {
        MqType mqType = getMqType();

        if (mqType == null) {
            logger.error("mqType is not null ....");
        }

        return mqType == MqType.RABBIT ? new RabbitMqServiceImpl() : new RocketMqServiceImpl();
    }

}
