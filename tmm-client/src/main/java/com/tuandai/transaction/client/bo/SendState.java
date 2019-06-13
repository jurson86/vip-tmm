package com.tuandai.transaction.client.bo;
public enum SendState {

    COMMIT(0, "提交"), CANCEL(1, "回滚");

    private final int value;

    private final String message;

    SendState(int value, String message) {
        this.value = value;
        this.message = message;
    }

    public int value() {
        return value;
    }

    public String message() {
        return message;
    }

    public static SendState findByMessage(String message) {
        for (SendState tccState : SendState.values()) {
            if (tccState.toString().equals(message)) {
                return tccState;
            }
        }
        return null;
    }

    public static SendState findByValue(int value) {
        for (SendState tccState : SendState.values()) {
            if (tccState.value == value) {
                return tccState;
            }
        }
        return null;
    }
}