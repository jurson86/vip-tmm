package com.tuandai.transaction.mq.consumer;

import com.tuandai.transaction.bo.RabbitAddressKey;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

public interface MqConsumer extends ChannelAwareMessageListener {

    void doDeclare(RabbitAdmin admin, RabbitAddressKey rabbitAddressKey);

    Queue getQueue();

}