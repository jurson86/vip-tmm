package com.tuandai.transaction.client.model;

public class MqLog {

    private String uid;

    private String serviceName;

    // RabbitMQTopic 序列化的对象或者其他
    private String topic;

    private String message;

    // MessageProperties 序列化的对象
    private String messageProperties;

    public String getMessageProperties() {
        return messageProperties;
    }

    public void setMessageProperties(String messageProperties) {
        this.messageProperties = messageProperties;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
