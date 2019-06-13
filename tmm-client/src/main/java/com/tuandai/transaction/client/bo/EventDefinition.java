package com.tuandai.transaction.client.bo;

import org.springframework.util.StringUtils;

public class EventDefinition {

    private EventType eventType = null;

    private long time;

    private String uid;

    private String serviceName;

    private SendState sendState;

    private String topic;

    private String message;

    private long goMapTime;

    private String checkUrl;

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

    // 合并EventDefinition
    public EventDefinition mergedEventDefinition(EventDefinition other) {
        this.sendState = other.sendState;
        if (!StringUtils.isEmpty(other.getTopic())) {
            this.topic = other.getTopic();
        }
        if (!StringUtils.isEmpty(other.getMessage())) {
            this.message = other.message;
        }
        return this;
    }

    public enum EventType {
        BEGIN, END, MQ, NTRANS;
    }

    @Override
    public String toString() {
        return "EventDefinition{" +
                "eventType=" + eventType +
                ", time=" + time +
                ", uid='" + uid + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", sendState=" + sendState +
                ", topic='" + topic + '\'' +
                ", message='" + message + '\'' +
                ", goMapTime=" + goMapTime +
                ", checkUrl='" + checkUrl + '\'' +
                '}';
    }
}
