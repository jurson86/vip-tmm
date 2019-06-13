package com.tuandai.transaction.domain;

import java.util.Date;
import java.util.Objects;

public class Application {
    private Integer pid;

    private String applicationName;

    private Boolean check = false;

    private Date createTime;

    private Date updateTime;

    public Application() {
    }

    public Application(Integer pid) {
        this.pid = pid;
    }

    public Application(String applicationName) {
        this.applicationName = applicationName;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public Application(Integer pid, String applicationName) {
        this.pid = pid;
        this.applicationName = applicationName;
    }


    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName == null ? null : applicationName.trim();
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

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }


}