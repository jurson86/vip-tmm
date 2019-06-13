package com.tuandai.transaction.bo;

public class RabbitAddress {
    // 真实的Ip
    private String ip;

    private Integer port;

    private String userName;

    private String password;

    private String adminUrl;

    public String getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
    }

    public RabbitAddress() {
    }

    public RabbitAddress(String ip) {
        this.ip = ip;
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
                ", password='" + password + '\'' +
                ", adminUrl='" + adminUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        // 通用的equals写法套路
        // 1.先用 == 检查对象是自身的引用，是则返回true
        if (this == obj) {
            return true;
        }
        // 2.用instanceof 检查类型是否符合，不符合则直接返回false
        if (!(obj instanceof RabbitAddress)) {
            return false;
        }
        // 3.将参数正确转型
        RabbitAddress rabbitAddress = (RabbitAddress)obj;
        // 4.对类进行阈值判断
        if (this.ip.equals(rabbitAddress.ip) && (this.port.equals(rabbitAddress.port))
                && (this.userName.equals(rabbitAddress.userName))) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // 1.定义一个非零的变量
        int result = 13;
        // 2.计算code值为c
        result = 31 * result + (ip == null ? 0 : ip.hashCode());
        result = 31 * result + (port == null ? 0 : port.hashCode());
        result = 31 * result + (userName == null ? 0 : userName.hashCode());
        return result;
    }


    public ClusterIp toClusterIp() {
        ClusterIp clusterIp = new ClusterIp();
        clusterIp.setIp(ip);
        clusterIp.setPort(port);
        clusterIp.setUserName(userName);
        return clusterIp;
    }

}