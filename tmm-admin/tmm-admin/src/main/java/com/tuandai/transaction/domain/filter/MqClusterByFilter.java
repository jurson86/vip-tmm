package com.tuandai.transaction.domain.filter;

public class MqClusterByFilter {

    private Long pid;

    private String mqKey;

    private String host;

    private Integer port;

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getMqKey() {
        return mqKey;
    }

    public void setMqKey(String mqKey) {
        this.mqKey = mqKey;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
