package com.tuandai.transaction.domain;

import java.util.Date;

public class DlqServer {

    private Long pid;

    private String dlqName;

    private String serviceName;

    private Date createTime;

    private Date updateTime;

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDlqName() {
        return dlqName;
    }

    public void setDlqName(String dlqName) {
        this.dlqName = dlqName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "DlqServer{" +
                "pid=" + pid +
                ", dlqName='" + dlqName + '\'' +
                ", serverName='" + serviceName + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
