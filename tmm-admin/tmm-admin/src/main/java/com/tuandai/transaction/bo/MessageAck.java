package com.tuandai.transaction.bo;

public class MessageAck {

    public enum SendState {

        COMMIT(0, "提交"), CANCEL(1, "回滚");

        private final int code;

        private final String message;

        SendState(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    private String uid;

    private String message;

    private String serviceName;

    private String topic;

    private SendState state;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public SendState getState() {
        return state;
    }

    public void setState(SendState state) {
        this.state = state;
    }


    @Override
    public String toString() {
        return "MessageAck{" +
                "uid='" + uid + '\'' +
                ", message='" + message + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", topic='" + topic + '\'' +
                ", state=" + state +
                '}';
    }
}
