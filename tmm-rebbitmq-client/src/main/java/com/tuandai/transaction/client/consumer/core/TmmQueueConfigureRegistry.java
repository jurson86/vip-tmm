package com.tuandai.transaction.client.consumer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class TmmQueueConfigureRegistry implements ApplicationContextAware, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(TmmQueueConfigureRegistry.class);

    private volatile ApplicationContext applicationContext;

    private String applicationName;

    private DeadLetterProcessor deadLetterProcessor;

    public TmmQueueConfigureRegistry(String applicationName, DeadLetterProcessor deadLetterProcessor) {
        this.applicationName = applicationName;
        this.deadLetterProcessor = deadLetterProcessor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Collection<Queue> contextQueues = new LinkedList<>(
                this.applicationContext.getBeansOfType(Queue.class).values());
        Set<String> tmmQueueNames = deadLetterProcessor.getTmmQueueNames();
        if (CollectionUtils.isEmpty(tmmQueueNames)) {
            return;
        }
        // 找到注解申明的bean
        for (Queue queue : contextQueues) {
            if (tmmQueueNames.contains(queue.getName())) {
                logger.info("tmm TmmQueueConfigure 拦截Queue -> queue: " + queue);
                // 反射改变值
                DeadLetterHelper.checkArguments(queue);
                // 再次获取
                Map<String, Object> a = queue.getArguments();
                if (a != null) {
                    DeadLetterHelper.initArruments(a, applicationName);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
       this.applicationContext  = applicationContext;
    }

}
