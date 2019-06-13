package com.tuandai.transaction.mq;

import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.mq.inf.MqService;

public class RocketMqServiceImpl implements MqService {

    @Override
    public void sendMessage(TransactionCheck transactionCheck) {

    }

//    private RocketMQTemplate rocketMQTemplate;
//
//    public RocketMqServiceImpl() {
//        rocketMQTemplate = SpringUtil.getBean(RocketMQTemplate.class);
//    }
//
//    @Override
//    public void sendMessage(TransactionMessage transactionMessage) {
//
//        String keyStr = transactionMessage.getuId();
//
//        Message<?> message = MessageBuilder.withPayload(transactionMessage.getMessage())
//                .setHeader(MessageConst.PROPERTY_KEYS, keyStr).build();
//
//        rocketMQTemplate.send(transactionMessage.getMessageTopic(), message);
//    }
}
