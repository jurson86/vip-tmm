package com.tuandai.transaction.client.bo;

public enum ExchangeType {

    FANOUT(1, "fanout"), HEADRES(2, "headers"), DIRECT(3, "direct"), TOPIC(4, "topic");

    private final int value;

    private final String des;

    ExchangeType(int value, String des) {
        this.value = value;
        this.des = des;
    }

    public static ExchangeType findByDes(String des) {
        for (ExchangeType exchangeType : ExchangeType.values()) {
            if (exchangeType.des.equals(des)) {
                return exchangeType;
            }
        }
        return null;
    }

    public static ExchangeType findByValue(int value) {
        for (ExchangeType exchangeType : ExchangeType.values()) {
            if (exchangeType.value == value) {
                return exchangeType;
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
