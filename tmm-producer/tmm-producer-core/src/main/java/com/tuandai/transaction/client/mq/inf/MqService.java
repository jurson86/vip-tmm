package com.tuandai.transaction.client.mq.inf;

import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.bo.MqType;

import java.util.List;
import java.util.Map;

public interface MqService {

    /**
     * 发送消息
      */
    void sendMessage( EventDefinition eventDefinition) throws Exception;

    /**
     *  mq 类型
     */
    MqType mqType();

    /**
     * 合并消息, 本来不应该提供该api 接口，暂放到这里
     */
    List<EventDefinition> mergeEventDefinition(Map<String, EventDefinition> eventDefinitionMap);

}
