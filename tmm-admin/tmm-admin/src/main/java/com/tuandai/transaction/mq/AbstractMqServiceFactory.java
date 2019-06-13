package com.tuandai.transaction.mq;

import com.tuandai.transaction.bo.MqType;
import com.tuandai.transaction.mq.inf.MqServiceFactory;

public abstract class AbstractMqServiceFactory implements MqServiceFactory {

    public AbstractMqServiceFactory(MqType mqType) {
        this.mqType = mqType;
    }

    protected MqType mqType;

    protected MqType getMqType() {
        return mqType;
    }

    protected void setMqType(MqType mqType) {
        this.mqType = mqType;
    }
}
