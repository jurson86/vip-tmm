package com.tuandai.transaction.producer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RocketConfiguration {

    @Bean
    public RocketConfig createRabbitConfig(TMMConfigAdapter tmmConfigAdapter,
                                           TMMRocketProperties tmmRocketProperties) {
        RocketConfig rabbitConfig = new RocketConfig();
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
        rabbitConfig.setRocketmq(tmmRocketProperties.getRocketmq());
        return rabbitConfig;
    }

}
