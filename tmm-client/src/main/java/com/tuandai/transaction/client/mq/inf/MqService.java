package com.tuandai.transaction.client.mq.inf;

import com.tuandai.transaction.client.bo.EventDefinition;

public interface MqService {

    void sendMessage( EventDefinition eventDefinition) throws Exception;

}
