package com.tuandai.transaction.mq;

import com.tuandai.transaction.mq.inf.MqClusterStrategy;
import com.tuandai.transaction.mq.inf.MqClusterStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 默认的配置策略生产器
 */
@Component
public class DefaultMqClusterStrategyFactory implements MqClusterStrategyFactory {

    public DefaultMqClusterStrategyFactory() {
    }

    @Value("${spring.rabbitmq.MqClusterStrategy:db}")
    private String mqClusterStrategyType;

    @Autowired
    private DBMqClusterStrategy dBMqClusterStrategy;

    @Autowired
    private YmlMqClusterStrategy ymlMqClusterStrategy;

    @Override
    public MqClusterStrategy createMqClusterStrategy() {
        if ("db".equals(mqClusterStrategyType)) {
            return dBMqClusterStrategy;
        } else if ("yml".equals(mqClusterStrategyType)) {
            return ymlMqClusterStrategy;
        }
        return null;
    }
}
