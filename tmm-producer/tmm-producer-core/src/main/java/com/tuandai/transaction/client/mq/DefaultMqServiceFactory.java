package com.tuandai.transaction.client.mq;

import com.tuandai.transaction.client.bo.MqType;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.mq.inf.MqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

        MqService currentMqService = null;
        List<MqService> mqServices = SettingSupport.getMqServices();
        for (MqService mqService : mqServices) {
            MqType customMqType = mqService.mqType();
            if (mqType == customMqType) {
                currentMqService = mqService;
                break;
            }
        }
        if (currentMqService == null) {
            logger.error("没有找到适合的MqService， 请检查当前是否包含对应的mq驱动程序...., 当前设置的 mqType:{}, mqServices:{}", mqType, mqServices);
            throw new IllegalArgumentException("没有找到适合的MqService， 请检查当前是否包含对应的mq驱动程序....");
        }

        return currentMqService;
    }

}
