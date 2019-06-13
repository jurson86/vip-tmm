package com.tuandai.transaction.client.consumer.core;

import com.tuandai.transaction.client.consumer.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DeadLetterHelper {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterHelper.class);

    // 过期时间
    private static final String x_message_ttl = "x-message-ttl";

    // 死信交换机
    private static final String x_dead_letter_exchange = "x-dead-letter-exchange";
    private static final String x_dead_letter_exchange_value = "dle-tmm";

    // 路由键
    private static final String x_dead_letter_routing_key = "x-dead-letter-routing-key";

    public static void initArruments(Map<String, Object> arruments, String applicationName) {
        // 设置默认过期时间
        if (arruments.get(x_message_ttl) == null) {
            arruments.put(x_message_ttl, Constants.x_message_ttl_value);
        }
        // 设置默认死信交换机
        if (arruments.get(x_dead_letter_exchange) == null) {
            arruments.put(x_dead_letter_exchange, x_dead_letter_exchange_value);
        }
        // 设置路由键为消费者的服务名
        if (arruments.get(x_dead_letter_routing_key) == null) {
            arruments.put(x_dead_letter_routing_key, applicationName);
        }
    }

    public static void checkArguments(Queue queue) {
        Map<String, Object> arruments = queue.getArguments();
        if (arruments == null) { // 设置空的map
            // 通过反射获取
            Map<String, Object> map = new HashMap<>();
            Field arrumentsField = null;
            try {
                arrumentsField = queue.getClass().getDeclaredField("arguments");
                arrumentsField.setAccessible(true);
                arrumentsField.set(queue, map);
            } catch (Exception e) {
                logger.error("tmm 设置队列死信队列默认参数失败！");
            }
        }
    }
}
