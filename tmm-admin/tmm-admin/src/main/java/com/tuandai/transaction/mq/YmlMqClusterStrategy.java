package com.tuandai.transaction.mq;

import com.tuandai.transaction.bo.RabbitAddress;
import com.tuandai.transaction.config.TMMRabbitProperties;
import com.tuandai.transaction.mq.inf.MqClusterStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 配置文件的方式添加mq集群配置
 */
@Component
@EnableConfigurationProperties(TMMRabbitProperties.class)
public class YmlMqClusterStrategy implements MqClusterStrategy {

    @Autowired
    private TMMRabbitProperties tMMRabbitProperties;

    @Override
    public Map<String, RabbitAddress> loadRabbitAddressMap() throws Exception {
        return tMMRabbitProperties.getRabbitAddressMap();
    }

}
