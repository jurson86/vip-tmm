package com.tuandai.transaction.client.bo;

/**
 * MQ类型
 */
public enum MqType {

   ROCKET(1, "rocketmq") , RABBIT(2, "rabbitmq");

    private final int value;

    private final String des;

    MqType(int value, String des) {
        this.value = value;
        this.des = des;
    }

    public static MqType findByDes(String des) {
        for (MqType mq : MqType.values()) {
            if (mq.des.equals(des)) {
                return mq;
            }
        }
        return null;
    }

    public static MqType findByValue(int value) {
        for (MqType mq : MqType.values()) {
            if (mq.value == value) {
                return mq;
            }
        }
        return null;
    }

    public int value() {
        return value;
    }

    public String des() {
        return des;
    }

}
