package com.tuandai.transaction.producer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 此类 用来存储所有的rabbtimq 配置信息
 */

@ConfigurationProperties(prefix = "spring")
public class TMMRabbitProperties {

    /**
     *接受rabbitmq所有属性
     */
    private Map<String, String> rabbitmq = new HashMap<>();

    public Map<String, String> getRabbitmq() {
        return rabbitmq;
    }

    public void setRabbitmq(Map<String, String> rabbitmq) {
        this.rabbitmq = rabbitmq;
    }

}
