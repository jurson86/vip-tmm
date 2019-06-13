package com.tuandai.transaction.mq;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.rabbitmq.client.ConnectionFactory;
import com.tuandai.transaction.bo.ClusterIp;
import com.tuandai.transaction.bo.RabbitAddress;
import com.tuandai.transaction.bo.RabbitAddressKey;
import com.tuandai.transaction.mq.inf.MqClusterStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitUtils;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class RabbitTemplateFactory implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(RabbitTemplateFactory.class);

    @Autowired
    private MqClusterStrategyFactory mqClusterStrategyFactory;

    private static Map<String, RabbitAddress> rabbitAddressMap;

    // 缓存连接池
    private static Map<RabbitAddressKey, CachingConnectionFactory> connectionFactoryCache = new ConcurrentHashMap<>();

    // 缓存RabbitTemplate
    private static Map<RabbitAddressKey, RabbitTemplate> rabbitTemplateCache = new ConcurrentHashMap<>();

    // 缓存admin
    private static Map<RabbitAddressKey, RabbitAdmin> adminCache = new ConcurrentHashMap<>();

    public static void init(Map<String, RabbitAddress> map) {
        rabbitAddressMap = map;
    }

    public static void close() {
        if (!CollectionUtils.isEmpty(connectionFactoryCache)) {
            for (Map.Entry<RabbitAddressKey, CachingConnectionFactory> entry : connectionFactoryCache.entrySet()) {
                CachingConnectionFactory connectionFactory = entry.getValue();
                connectionFactory.destroy(); // 关闭连接
            }
            connectionFactoryCache.clear();
        }
        rabbitTemplateCache.clear();
        adminCache.clear();
    }

    public static Map<String, RabbitAddress> getRabbitAddressMap() {
        return rabbitAddressMap;
    }

    public static RabbitAdmin getRabbitAdmin(RabbitAddressKey rabbitAddressKey) {
        return adminCache.get(rabbitAddressKey);
    }

    public static RabbitAddress getRabbitAddress(String ipName) throws IllegalAccessException {
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

    private static RabbitAddress getRabbitAddress(ClusterIp clusterIp) throws IllegalAccessException {
        for (Map.Entry<String, RabbitAddress> entry : rabbitAddressMap.entrySet()) {
            RabbitAddress tmp = entry.getValue();
            if (clusterIp.getPort() != null && !StringUtils.isEmpty(clusterIp.getUserName())) {
                RabbitAddress key = new RabbitAddress();
                key.setIp(clusterIp.getIp());
                key.setPort(clusterIp.getPort());
                key.setUserName(clusterIp.getUserName());
                if (tmp.equals(key)) {
                    return tmp;
                }
            } else { // 兼容老数据
                if (tmp.getIp().equals(clusterIp.getIp())) {
                    return tmp;
                }
            }
        }
        throw new IllegalAccessException("没有配置该类型的Ip集群");
    }

    public static RabbitTemplate getRabbitTemplate(String vHost, String ipName) throws IllegalAccessException {
        return getRabbitTemplate(new RabbitAddressKey(getRabbitAddress(ipName), vHost));
    }

    public static RabbitTemplate getRabbitTemplate(String vHost, ClusterIp clusterIp) throws IllegalAccessException {
        if (clusterIp == null) {
            throw new IllegalAccessException("没有配置该类型的Ip集群");
        }
        return getRabbitTemplate(new RabbitAddressKey(getRabbitAddress(clusterIp), vHost));
    }

    public static RabbitTemplate getRabbitTemplate(RabbitAddressKey rabbitAddressKey) {

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
            //connectionFactory.setPublisherConfirms(true);
            connectionFactoryCache.put(rabbitAddressKey, connectionFactory);
        }

        if (!adminCache.containsKey(rabbitAddressKey)) {
            adminCache.put(rabbitAddressKey, new RabbitAdmin(connectionFactory));
        }

        if (!rabbitTemplateCache.containsKey(rabbitAddressKey)) {
            RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
            rabbitTemplateCache.put(rabbitAddressKey, rabbitTemplate);
        }
        // 必须要设置,生产者发送消息后的回调，这里RabbitTemplate必须是多例子模式的
        return rabbitTemplateCache.get(rabbitAddressKey);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("====初始化mq配置...");
        Map<String, RabbitAddress> rabbitAddressMap = null;
        try {
            rabbitAddressMap = mqClusterStrategyFactory
                    .createMqClusterStrategy().loadRabbitAddressMap();
        } catch (Exception e) {
            logger.error("rabbitmq初始化失败");
            throw new Exception("rabbitmq初始化失败");
        }
        RabbitTemplateFactory.init(rabbitAddressMap);
    }

    @Override
    public void destroy() throws Exception {
        logger.info("====销毁mq链接...");
        RabbitTemplateFactory.close();
    }
}
