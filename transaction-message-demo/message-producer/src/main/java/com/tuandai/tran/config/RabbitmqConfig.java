package com.tuandai.tran.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "spring")
public class RabbitmqConfig {

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
