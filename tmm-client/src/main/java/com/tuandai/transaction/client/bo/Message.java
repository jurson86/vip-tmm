package com.tuandai.transaction.client.bo;

public class Message {

    private String uid;

    private String message;

    public Message() {
    }

    public Message(String uid, String message) {
        this.uid = uid;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "Message{" +
                "uid='" + uid + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
