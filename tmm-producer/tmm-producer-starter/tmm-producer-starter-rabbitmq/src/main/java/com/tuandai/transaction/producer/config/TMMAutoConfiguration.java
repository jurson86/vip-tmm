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
    @Value("${spring.tmmService.uid:random}") // 默认随机
    private String uid;

    @Value("${spring.tmmService.check.url:}")
    private String check;

    @Value("${spring.rabbitmq.tmm.vhost:/}")
    private String vhost;

    @Value("${spring.rabbitmq.tmm.exchange:}")
    private String exchange;

    @Value("${spring.rabbitmq.tmm.exchangeType:fanout}")
    private String exchangeType;

    @Value("${spring.rabbitmq.tmm.routeKey:}")
    private String routeKey;

    @Value("${spring.rabbitmq.tmm.ip:default}")
    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }

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
                ", vhost='" + vhost + '\'' +
                ", exchange='" + exchange + '\'' +
                ", exchangeType='" + exchangeType + '\'' +
                ", routeKey='" + routeKey + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
