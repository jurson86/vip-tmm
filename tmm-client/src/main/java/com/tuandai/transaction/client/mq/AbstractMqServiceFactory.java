package com.tuandai.transaction.client.mq;

import com.tuandai.transaction.client.bo.MqType;
import com.tuandai.transaction.client.mq.inf.MqServiceFactory;

public abstract class AbstractMqServiceFactory implements MqServiceFactory {

    protected MqType mqType;

    public AbstractMqServiceFactory(MqType mqType) {
        this.mqType = mqType;
    }

    protected MqType getMqType() {
        return mqType;
    }

    protected void setMqType(MqType mqType) {
        this.mqType = mqType;
    }
}
