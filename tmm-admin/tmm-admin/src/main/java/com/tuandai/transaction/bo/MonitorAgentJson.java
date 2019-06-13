package com.tuandai.transaction.bo;

public class MonitorAgentJson {

    private String serviceName;

    private String url;

    private String monitor;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMonitor() {
        return monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }

    public MonitorAgentJson(String serviceName, String url, String monitor) {
        this.serviceName = serviceName;
        this.url = url;
        this.monitor = monitor;
    }

    @Override
    public String toString() {
        return "MonitorAgentJson{" +
                "serviceName='" + serviceName + '\'' +
                ", url='" + url + '\'' +
                ", monitor='" + monitor + '\'' +
                '}';
    }
}
