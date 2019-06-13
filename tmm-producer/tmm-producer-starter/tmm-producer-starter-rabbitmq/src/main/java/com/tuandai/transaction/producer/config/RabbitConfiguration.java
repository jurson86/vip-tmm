package com.tuandai.transaction.producer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    @Bean
    public RabbitConfig createRabbitConfig(TMMConfigAdapter tmmConfigAdapter,
                                           TMMRabbitProperties tmmRabbitProperties) {
        RabbitConfig rabbitConfig = new RabbitConfig();
        rabbitConfig.setMqType(tmmConfigAdapter.getMqType());
        rabbitConfig.setBatchSize(tmmConfigAdapter.getBatchSize());
        rabbitConfig.setRpcDirName(tmmConfigAdapter.getRpcDirName());
        rabbitConfig.setCheckIntervalTime(tmmConfigAdapter.getCheckIntervalTime());
        rabbitConfig.setRpcSize(tmmConfigAdapter.getRpcSize());
        rabbitConfig.setSendMqCorePoolThreadNum(tmmConfigAdapter.getSendMqCorePoolThreadNum());
        rabbitConfig.setSendMqMaxPoolThreadNum(tmmConfigAdapter.getSendMqMaxPoolThreadNum());
        rabbitConfig.setSendMqQueueSize(tmmConfigAdapter.getSendMqQueueSize());
        rabbitConfig.setServerName(tmmConfigAdapter.getServerName());
        rabbitConfig.setPrefixUrl(tmmConfigAdapter.getPrefixUrl());
        rabbitConfig.setRabbitmq(tmmRabbitProperties.getRabbitmq());
        rabbitConfig.setTmmMessage(Boolean.valueOf(tmmConfigAdapter.getTmmMessage()));
        return rabbitConfig;
    }

}
