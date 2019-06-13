package com.tuandai.transaction.producer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * tmm 自动配置类, 如果配置了该类的属性那么自动配置tmm
 */
@ConfigurationProperties
public class TMMAutoConfiguration {

    // 服务名
    @Value("${spring.application.name}")
    private String serviceName;

    // 只允许可靠消息 配置此字段，原则上不允许事务消息配置此字段，而是实时去传递，因为事务消息，需要根据此字段去 check
    // random / string
    @Value("${spring.tmmService.uid:}") // 默认随机
    private String uid;

    @Value("${spring.tmmService.check:}")
    private String check;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    @Override
    public String toString() {
        return "TMMAutoConfiguration{" +
                "serviceName='" + serviceName + '\'' +
                ", uid='" + uid + '\'' +
                ", check='" + check + '\'' +
                '}';
    }
}
