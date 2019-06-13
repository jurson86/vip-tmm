package com.tuandai.transaction.producer.core.inf;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

public interface RabbitTemplatePostProcessor {

    /**
     * 缓存 RabbitTemplate 之前调用的接口
     * @param rabbitTemplate
     */
    void postProcessBeforeInitialization(RabbitTemplate rabbitTemplate);

}
