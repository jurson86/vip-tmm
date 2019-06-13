package com.tuandai.transaction.mq;

import com.tuandai.transaction.bo.MqType;
import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.mq.inf.MqServiceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqSendHelper {

    //@Value("${mq.type}")
    //private String mqType;

    private MqServiceFactory mqServiceFactory = new DefaultMqServiceFactory(MqType.findByDes("rabbitmq"));

    public MqSendHelper(MqServiceFactory mqServiceFactory) {
        this.mqServiceFactory = mqServiceFactory;
    }

    public MqSendHelper() {
    }

    /**
     * 发送带key[事物id]的事物消息
     * @param transactionMessage
     */
    public void sendTranTopicMsg(TransactionCheck transactionMessage) throws IllegalAccessException {
        getMqServiceFactory().createMqService().sendMessage(transactionMessage);
    }

    public MqServiceFactory getMqServiceFactory() {
        return mqServiceFactory;
    }

}
