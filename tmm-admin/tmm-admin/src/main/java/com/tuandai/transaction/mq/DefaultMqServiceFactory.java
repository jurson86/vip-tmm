package com.tuandai.transaction.mq;

import com.tuandai.transaction.bo.MqType;
import com.tuandai.transaction.mq.inf.MqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的适配Mq中间件策略
 */
public class DefaultMqServiceFactory extends AbstractMqServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMqServiceFactory.class);

    public DefaultMqServiceFactory(MqType mqType) {
        super(mqType);
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
