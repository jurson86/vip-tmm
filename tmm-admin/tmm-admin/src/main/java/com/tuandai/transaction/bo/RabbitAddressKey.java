package com.tuandai.transaction.bo;

public class RabbitAddressKey {

    private RabbitAddress rabbitAddress;

    private String vHost;

    public RabbitAddressKey(RabbitAddress rabbitAddress, String vHost) {
        this.rabbitAddress = rabbitAddress;
        this.vHost = vHost;
    }

    public RabbitAddress getRabbitAddress() {
        return rabbitAddress;
    }

    public void setRabbitAddress(RabbitAddress rabbitAddress) {
        this.rabbitAddress = rabbitAddress;
    }

    public String getvHost() {
        return vHost;
    }

    public void setvHost(String vHost) {
        this.vHost = vHost;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RabbitAddressKey)) {
            return false;
        }
        // 3.将参数正确转型
        RabbitAddressKey rabbitAddressKey = (RabbitAddressKey)obj;
        // 4.对类进行阈值判断
        if (this.rabbitAddress.equals(rabbitAddressKey.rabbitAddress) && (this.vHost.equals(rabbitAddressKey.vHost))) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // 1.定义一个非零的变量
        int result = 13;
        // 2.计算code值为c
        result = 31 * result + (rabbitAddress == null ? 0 : rabbitAddress.hashCode());
        result = 31 * result + (vHost == null ? 0 : vHost.hashCode());
        return result;
    }

}
