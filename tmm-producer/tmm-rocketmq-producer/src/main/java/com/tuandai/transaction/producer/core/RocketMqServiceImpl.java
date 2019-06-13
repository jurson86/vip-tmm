package com.tuandai.transaction.producer.core;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.bo.MqType;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.mq.inf.MqService;
import com.tuandai.transaction.client.service.TMMServiceImpl;
import com.tuandai.transaction.client.service.inf.EventDefinitionRegistry;
import com.tuandai.transaction.client.utils.CollectionUtils;
import com.tuandai.transaction.producer.config.RocketConfig;
import com.tuandai.transaction.producer.model.RocketMqTopic;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RocketMqServiceImpl implements MqService {

    private static final Logger logger = LoggerFactory.getLogger(RocketMqServiceImpl.class);

    public static TMMServiceImpl tmmService = null;

    static {
        try {
            RocketConfig tmmConfig = (RocketConfig) SettingSupport.getTmmConfig();
            RocketProucerFactory.init(tmmConfig.getRockerAddressMap());
        } catch (MQClientException e) {
            logger.info("加载 RocketMqServiceImpl 失败....");
        }
    }

    @Override
    public void sendMessage(EventDefinition eventDefinition) throws Exception {
        String topicStr = eventDefinition.getTopic();
        RocketMqTopic rockerMqTopic = JSONObject.parseObject(topicStr, RocketMqTopic.class);

        DefaultMQProducer producer = RocketProucerFactory.getDefaultMQProducer(rockerMqTopic.getIp());

        Message msg = new Message(rockerMqTopic.getTopic(), rockerMqTopic.getTag(), (eventDefinition.getMessage() == null
                ? "": eventDefinition.getMessage()).getBytes());

        SendResult sendResult = producer.send(msg);

        if (sendResult.getSendStatus() == SendStatus.SEND_OK && tmmService != null) {
            EventDefinitionRegistry registry = tmmService;
            registry.removeEventDefinition(eventDefinition.getUid(), EventDefinition.EventType.MQ);
        }
    }

    @Override
    public MqType mqType() {
        return MqType.ROCKET;
    }

    @Override
    public List<EventDefinition> mergeEventDefinition(Map<String, EventDefinition> eventDefinitionMap) {
        List<EventDefinition> results = new ArrayList<>();
        if (eventDefinitionMap != null && eventDefinitionMap.size() > 0) {
            for (Map.Entry<String, EventDefinition> entry : eventDefinitionMap.entrySet()) {
                results.add(entry.getValue());
            }
        }
        return results;
    }
}
