package com.tuandai.transaction.client.utils;

/**
 * 
 */
public enum BZStatusCode {

    OK(200, "请求成功");

    private final int code;

    private final String message;


    BZStatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

}
