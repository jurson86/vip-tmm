package com.tuandai.transaction.mq.consumer;

import com.tuandai.transaction.bo.RabbitAddress;
import com.tuandai.transaction.bo.RabbitAddressKey;
import com.tuandai.transaction.config.TMMRabbitProperties;
import com.tuandai.transaction.mq.RabbitTemplateFactory;
import com.tuandai.transaction.mq.inf.MqClusterStrategy;
import com.tuandai.transaction.mq.inf.MqClusterStrategyFactory;
import com.tuandai.transaction.mq.inf.RefreshMqCluster;
import com.tuandai.transaction.utils.BZStatusCode;
import com.tuandai.transaction.utils.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.rmi.ServerException;
import java.util.*;

/**
 * tmm_host的虚拟host
 *
 * 为配置的每一个rabbitmq集群注册一个admin申明的消费者
 *
 */
@Component
public class RegisterRabbitMqContainer implements SmartLifecycle, RefreshMqCluster {

    private static final Logger logger = LoggerFactory.getLogger(RegisterRabbitMqContainer.class);

    //@Value("${spring.rabbitmq.virtual-host}")
    private String vhost = "/tmm_vhost";

    private boolean isRunning = false;

    @Autowired
    private Set<MqConsumer> set;

    private List<SimpleMessageListenerContainer> simpleMessageListenerContainerList = null;

    public List<SimpleMessageListenerContainer> handler() {
        List<SimpleMessageListenerContainer> list = new ArrayList<>();

        Map<String, RabbitAddress> rabbitAddressMap = RabbitTemplateFactory.getRabbitAddressMap();
        // 和所有的集群的tmm_host创建关联关系
        for (Map.Entry<String, RabbitAddress> entry : rabbitAddressMap.entrySet()) {
            RabbitAddress tmpRabbitAddress = entry.getValue();

            RabbitAddressKey key = new RabbitAddressKey(tmpRabbitAddress, vhost);
            RabbitTemplate rabbitTemplate = RabbitTemplateFactory.getRabbitTemplate(key);
            // 申明队列和绑定关系
            RabbitAdmin admin = RabbitTemplateFactory.getRabbitAdmin(key);
            if (set != null) {
                Iterator<MqConsumer> it = set.iterator();
                while (it.hasNext()) {
                    MqConsumer consumer = it.next();
                    consumer.doDeclare(admin, key);
                    // 异步监听mq
                    MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(consumer);
                    SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
                    simpleMessageListenerContainer.addQueues(consumer.getQueue());
                    simpleMessageListenerContainer.setConnectionFactory(rabbitTemplate.getConnectionFactory());
                    simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
                    list.add(simpleMessageListenerContainer);
                }
            }
        }
        return list;
    }

    public void registerCustomer() {
        logger.info("============监听mq集群消费者============");
        simpleMessageListenerContainerList = handler();
        if (!CollectionUtils.isEmpty(simpleMessageListenerContainerList)) {
            for (SimpleMessageListenerContainer container : simpleMessageListenerContainerList) {
                // 启动监听器
                container.start();
            }
        }
    }

    @Override
    public void refreshConnection() {
        // 当mq集群配置刷新的时候刷新
        registerCustomer(); // 重新刷新
    }

    @Override
    public void closeConnection() {
        if (!CollectionUtils.isEmpty(simpleMessageListenerContainerList)) {
            // 关闭原来连接
            for (SimpleMessageListenerContainer container : simpleMessageListenerContainerList) {
                // 关闭监听器
                container.stop();
            }
        }
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        callback.run();
        isRunning = false;
    }

    @Override
    public void start() {
        registerCustomer();
        isRunning = true;
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPhase() {
        return 0;
    }
}
