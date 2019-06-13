package com.tuandai.transaction.bo;

import java.util.List;

public class QueueJson {

    public class Argument {
        String serviceName;

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
    }

    private String name;

    private String vhost;

    private Long messages;

    private String ipName;

    private String adminUrl;

    private List<Argument> arguments;

    public List<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
    }

    public String getIpName() {
        return ipName;
    }

    public void setIpName(String ipName) {
        this.ipName = ipName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    public Long getMessages() {
        return messages;
    }

    public void setMessages(Long messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "QueueJson{" +
                "name='" + name + '\'' +
                ", vhost='" + vhost + '\'' +
                ", messages=" + messages +
                ", ipName='" + ipName + '\'' +
                ", adminUrl='" + adminUrl + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
