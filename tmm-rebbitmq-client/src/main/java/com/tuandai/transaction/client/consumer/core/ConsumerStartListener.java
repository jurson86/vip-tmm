package com.tuandai.transaction.client.consumer.core;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.consumer.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashMap;
import java.util.Map;

// 发送消费者启动消息
public class ConsumerStartListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerStartListener.class);

    private RabbitTemplate rabbitTemplate;

    private String applicationName;

    public ConsumerStartListener(RabbitTemplate rabbitTemplate, String applicationName) {
        this.rabbitTemplate = rabbitTemplate;
        this.applicationName = applicationName;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        logger.info("tmm消费者启动，发送启动事件到dlq-tmm中");
        Map<String, String> startMap = new HashMap<>();
        startMap.put("applicationName", applicationName);
        String startMapStr = JSONObject.toJSONString(startMap);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("messageFlag", 1);
        rabbitTemplate.send(Constants.dlq, new Message(startMapStr.getBytes(), messageProperties));
    }
}
