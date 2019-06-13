package com.tuandai.transaction.client.exception;

public class ExceptionUtils {

    static public void fail(String message) {
        if (message == null) {
            throw new AssertionError();
        }
        throw new AssertionError(message);
    }
}
