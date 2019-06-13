package com.tuandai.transaction.service.inf;

import com.tuandai.transaction.bo.QueueJson;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public interface RabbitMqService {

    List<QueueJson> queryDLQList();

    // 消费死信队列
    void rabbitmqDLQConsumer() throws IllegalAccessException;

    // 死信队列重发
    boolean dlqResend(String queue, List<String> serviceNames);

    // 死信队列统计
    List<QueueJson> dlqList(List<String> serviceNames);

    // 添加死信和服务名绑定
    boolean addDlqService(String serviceName, String dlqName);

}
