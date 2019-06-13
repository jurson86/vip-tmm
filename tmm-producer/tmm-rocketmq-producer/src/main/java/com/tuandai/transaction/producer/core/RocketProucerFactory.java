package com.tuandai.transaction.producer.core;

import com.tuandai.transaction.producer.bo.RocketAddress;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RocketProucerFactory {

    private static final Logger logger = LoggerFactory.getLogger(RocketProucerFactory.class);

    private static final ConcurrentHashMap<String, DefaultMQProducer> mqProducerMap = new ConcurrentHashMap<>();

    public static void init(Map<String, RocketAddress> map) throws MQClientException {

        try {
            for (Map.Entry<String, RocketAddress> entry : map.entrySet()) {
                RocketAddress rocketAddress = entry.getValue();

                DefaultMQProducer defaultMQProducer = new DefaultMQProducer(rocketAddress.getGroup());
                defaultMQProducer.setNamesrvAddr(rocketAddress.getNamesrvAddr());
                defaultMQProducer.setSendMsgTimeout(rocketAddress.getSendMsgTimeout());
                defaultMQProducer.start();
                mqProducerMap.put(entry.getKey(), defaultMQProducer);
            }
        } catch (MQClientException e) {
            logger.error("启动 rocketmq 生产者 异常", e);
            throw e;
        }
    }

    public static DefaultMQProducer getDefaultMQProducer(String key) {
        return mqProducerMap.get(key);
    }
}
