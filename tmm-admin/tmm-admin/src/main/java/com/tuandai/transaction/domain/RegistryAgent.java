package com.tuandai.transaction.domain;

import java.util.Date;

public class RegistryAgent {

    private int pid;

    private String prefixUrl;

    private String serviceName;

    private Boolean check = false;

    private Date updateTime;

    private Date createTime;

    public RegistryAgent() {
    }

    public RegistryAgent(String serviceName) {
        this.serviceName = serviceName;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public String getPrefixUrl() {
        return prefixUrl;
    }

    public void setPrefixUrl(String prefixUrl) {
        this.prefixUrl = prefixUrl;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }
}
