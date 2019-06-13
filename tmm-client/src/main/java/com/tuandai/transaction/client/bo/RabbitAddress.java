package com.tuandai.transaction.client.bo;

public class RabbitAddress {

    private String ip;

    private Integer port;

    private String userName;

    private String password;

    public RabbitAddress() {
    }

    public RabbitAddress(String ip) {
        this.ip = ip;
    }

    public RabbitAddress(String ip, Integer port, String userName, String password) {
        this.ip = ip;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "RabbitAddress{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", userName='" + userName + '\'' +
                '}';
    }
}
