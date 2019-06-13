package com.tuandai.transaction.bo;

/**
 * @author DELL
 */

public enum MessageState {

    PRESEND(10, "预发送"), ABNORMAL(11, "死信"), SEND(20, "发送"), PER_DIED(21, "异常"), SEND_DIED(22, "死亡"),
    DONE(30, "完成"), DISCARD(100, "废弃");

    private final int code;

    private final String message;

    MessageState(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static MessageState findByValue(int value) {
        for (MessageState state : MessageState.values()) {
            if (state.code == value) {
                return state;
            }
        }
        return null;
    }


    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    public MessageState isValidCode(int _code)
    {
        return MessageState.valueOf("");
    }
}
