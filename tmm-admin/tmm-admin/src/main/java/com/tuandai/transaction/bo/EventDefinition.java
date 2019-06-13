package com.tuandai.transaction.bo;

public class EventDefinition {

    public enum EventType {
        BEGIN, END;
    }

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

    private String checkUrl;

    private EventType eventType = null;

    private long time;

    private String uid;

    private String serviceName;

    private SendState sendState;

    private String topic;

    private String message;

    private long goMapTime;

    public String getCheckUrl() {
        return checkUrl;
    }

    public void setCheckUrl(String checkUrl) {
        this.checkUrl = checkUrl;
    }

    public long getGoMapTime() {
        return goMapTime;
    }

    public void setGoMapTime(long goMapTime) {
        this.goMapTime = goMapTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public SendState getSendState() {
        return sendState;
    }

    public void setSendState(SendState sendState) {
        this.sendState = sendState;
    }

    @Override
    public String toString() {
        return "EventDefinition{" +
                "checkUrl='" + checkUrl + '\'' +
                ", eventType=" + eventType +
                ", time=" + time +
                ", uid='" + uid + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", sendState=" + sendState +
                ", topic='" + topic + '\'' +
                ", message='" + message + '\'' +
                ", goMapTime=" + goMapTime +
                '}';
    }
}
