package com.tuandai.transaction.mq.inf;

import com.tuandai.transaction.bo.RabbitAddress;

import java.util.Map;

/**
 * 加载mq配置策略
 */
public interface MqClusterStrategy {

    Map<String, RabbitAddress> loadRabbitAddressMap() throws Exception;

}
