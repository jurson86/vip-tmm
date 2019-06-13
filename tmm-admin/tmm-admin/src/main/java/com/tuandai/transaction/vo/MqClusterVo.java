package com.tuandai.transaction.vo;

import java.io.Serializable;

public class MqClusterVo implements Serializable {

    private Long pid;

    // 后续采用uuid自动生成，不需要前端修改和传值
    private String mqKey;

    private String host;

    private Integer port;

    private String userName;

    private String passWord;

    private String adminUrl;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
    }

    @Override
    public String toString() {
        return "MqClusterVo{" +
                "pid=" + pid +
                ", mqKey='" + mqKey + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", userName='" + userName + '\'' +
                ", passWord='" + passWord + '\'' +
                ", adminUrl='" + adminUrl + '\'' +
                '}';
    }
}
