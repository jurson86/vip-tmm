package com.tuandai.transaction.client.mq.rabbitmq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tuandai.transaction.client.bo.RabbitAddress;
import com.tuandai.transaction.client.bo.RabbitAddressKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.StringUtils;

public class RabbitTemplateFactory {

    private static final Logger logger = LoggerFactory.getLogger(RabbitTemplateFactory.class);

    private static Map<String, RabbitAddress> rabbitAddressMap;

    public static void init(Map<String, RabbitAddress> map) {
        rabbitAddressMap = map;
    }

    // 缓存连接池  RabbitAddress -> 连接池
    private static Map<RabbitAddressKey, CachingConnectionFactory> connectionFactoryCache = new ConcurrentHashMap<>();

    // 缓存RabbitTemplate
    private static Map<RabbitAddressKey, RabbitTemplate> rabbitTemplateCache = new ConcurrentHashMap<>();

    // 缓存admin
    private static Map<RabbitAddressKey, RabbitAdmin> adminCache = new ConcurrentHashMap<>();

    public static RabbitAdmin getRabbitAdmin(String vHost, String ipName) throws IllegalAccessException {
        RabbitAddress rabbitAddress = getRabbitAddress(ipName);
        return adminCache.get(new RabbitAddressKey(rabbitAddress, vHost));
    }

    private static RabbitAddress getRabbitAddress(String ipName) throws IllegalAccessException {
        RabbitAddress rabbitAddress = null;
        if (!StringUtils.isEmpty(ipName)) {
            rabbitAddress = rabbitAddressMap.get(ipName);
        }
        if (rabbitAddress == null) {
            rabbitAddress = getFirstRabbitAddress(rabbitAddressMap);
            if (rabbitAddress == null) {
                throw new IllegalAccessException("没有配置该类型的Ip集群");
            }
        }
        return rabbitAddress;
    }

    public static RabbitTemplate getRabbitTemplate(String vHost, String ipName) throws IllegalAccessException {
        return getRabbitTemplate(new RabbitAddressKey(getRabbitAddress(ipName), vHost));
    }

    public static RabbitTemplate getRabbitTemplate(RabbitAddressKey rabbitAddressKey) {
        long time = System.currentTimeMillis();
        RabbitAddress rabbitAddress = rabbitAddressKey.getRabbitAddress();
        String vHost = rabbitAddressKey.getvHost();
        String ip = rabbitAddress.getIp();
        Integer port = rabbitAddress.getPort();
        String password = rabbitAddress.getPassword();
        String username = rabbitAddress.getUserName();

        CachingConnectionFactory connectionFactory = null;
        if (connectionFactoryCache.containsKey(rabbitAddressKey)) {
            connectionFactory = connectionFactoryCache.get(rabbitAddressKey);
        } else {
            connectionFactory = new CachingConnectionFactory(ip, port);
            connectionFactory.setChannelCacheSize(100);
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
            connectionFactory.setVirtualHost(vHost);
            // 必须要设置,生产者发送消息后的回调，这里RabbitTemplate必须是多例子模式的
            connectionFactory.setPublisherConfirms(true);
            connectionFactoryCache.put(rabbitAddressKey, connectionFactory);
        }

        if (!adminCache.containsKey(rabbitAddressKey)) {
            adminCache.put(rabbitAddressKey, new RabbitAdmin(connectionFactory));
        }

        if (!rabbitTemplateCache.containsKey(rabbitAddressKey)) {
            RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
            rabbitTemplate.setConfirmCallback(new ConfirmCallback());
            rabbitTemplateCache.put(rabbitAddressKey, rabbitTemplate);
        }
        long now = System.currentTimeMillis();
        logger.debug("RabbitTemplateFactory.getRabbitTemplate(vHost, rabbitAddress, isPublisherConfirms) 花费时间：" + (now-time) + "ms");
        // 必须要设置,生产者发送消息后的回调，这里RabbitTemplate必须是多例子模式的
        return rabbitTemplateCache.get(rabbitAddressKey);
    }

    @Deprecated
    public static RabbitTemplate updateRabbitTemplate(String vHost, String ipName) throws IllegalAccessException {
        return updateRabbitTemplate(new RabbitAddressKey(getRabbitAddress(ipName), vHost));
    }

    @Deprecated
    public static RabbitTemplate updateRabbitTemplate(RabbitAddressKey rabbitAddressKey) {
        RabbitAddress rabbitAddress = rabbitAddressKey.getRabbitAddress();
        String vHost = rabbitAddressKey.getvHost();

        String ip = rabbitAddress.getIp();
        Integer port = rabbitAddress.getPort();
        String password = rabbitAddress.getPassword();
        String username = rabbitAddress.getUserName();

        CachingConnectionFactory connectionFactory = null;
        connectionFactory = new CachingConnectionFactory(ip, port);
        connectionFactory.setUsername(password);
        connectionFactory.setPassword(username);
        connectionFactory.setVirtualHost(vHost);
        connectionFactory.setPublisherConfirms(true); // 必须要设置,生产者发送消息后的回调，这里RabbitTemplate必须是多例子模式的
        connectionFactoryCache.put(rabbitAddressKey, connectionFactory);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplateCache.put(rabbitAddressKey, rabbitTemplate);
        return rabbitTemplate;
    }

    private static RabbitAddress getFirstRabbitAddress(Map<String, RabbitAddress> rabbitAddressMap) throws IllegalAccessException {
        if (rabbitAddressMap.size() == 1) {
            for (Map.Entry<String, RabbitAddress> entry : rabbitAddressMap.entrySet()) {
                return entry.getValue();
            }
        } else {
            throw new IllegalAccessException("请设置发送的mq集群IP！");
        }
        return null;
    }

}
