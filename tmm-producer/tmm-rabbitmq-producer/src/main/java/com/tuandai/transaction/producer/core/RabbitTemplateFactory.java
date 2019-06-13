package com.tuandai.transaction.producer.core;

import com.tuandai.transaction.producer.bo.RabbitAddress;
import com.tuandai.transaction.producer.bo.RabbitAddressKey;
import com.tuandai.transaction.producer.core.inf.RabbitTemplatePostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RabbitTemplateFactory {

    private static List<RabbitTemplatePostProcessor> rabbitTemplatePostProcessors = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(RabbitTemplateFactory.class);

    static {
        // 加载 spi配置
        ServiceLoader<RabbitTemplatePostProcessor> loadedParsers = ServiceLoader.load(RabbitTemplatePostProcessor.class);
        Iterator<RabbitTemplatePostProcessor> driversIterator = loadedParsers.iterator();
        try{
            while(driversIterator.hasNext()) {
                RabbitTemplatePostProcessor rabbitTemplatePostProcessor = driversIterator.next();
                rabbitTemplatePostProcessors.add(rabbitTemplatePostProcessor);
            }
        } catch(Throwable t) {
            logger.error("tmm 加载 RabbitTemplatePostProcessor 扩展类失败,请检查resources/META-INF/services/com.tuandai.transaction.producer.core.inf.RabbitTemplatePostProcessor 文件");
        }
    }

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
            doRabbitTemplatePostProcessor(rabbitTemplate);
            rabbitTemplate.setConfirmCallback(new ConfirmCallback());
            rabbitTemplateCache.put(rabbitAddressKey, rabbitTemplate);
        }
        long now = System.currentTimeMillis();
        logger.debug("RabbitTemplateFactory.getRabbitTemplate(vHost, rabbitAddress, isPublisherConfirms) 花费时间：" + (now-time) + "ms");
        // 必须要设置,生产者发送消息后的回调，这里RabbitTemplate必须是多例子模式的
        return rabbitTemplateCache.get(rabbitAddressKey);
    }

    private static void doRabbitTemplatePostProcessor(RabbitTemplate rabbitTemplate) {
        if (!CollectionUtils.isEmpty(rabbitTemplatePostProcessors)) {
            for (RabbitTemplatePostProcessor rabbitTemplatePostProcessor : rabbitTemplatePostProcessors) {
                rabbitTemplatePostProcessor.postProcessBeforeInitialization(rabbitTemplate);
            }
        }
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
