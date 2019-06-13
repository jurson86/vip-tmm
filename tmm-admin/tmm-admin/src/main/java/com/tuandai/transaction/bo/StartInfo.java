package com.tuandai.transaction.bo;

/**
 * 服务员启动信息封装
 */
public class StartInfo {

    private String serverName;

    private String prefixUrl;

    public StartInfo() {
    }

    public StartInfo(String serverName, String prefixUrl) {
        this.serverName = serverName;
        this.prefixUrl = prefixUrl;
    }

    public String getPrefixUrl() {
        return prefixUrl;
    }

    public void setPrefixUrl(String prefixUrl) {
        this.prefixUrl = prefixUrl;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public String toString() {
        return "StartInfo{" +
                "serverName='" + serverName + '\'' +
                ", prefixUrl='" + prefixUrl + '\'' +
                '}';
    }
}
