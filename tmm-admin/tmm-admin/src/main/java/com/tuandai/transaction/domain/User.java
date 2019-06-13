package com.tuandai.transaction.domain;

import java.util.Date;

public class User {
    private Integer pid;

    private String userName;

    private String password;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    public User() {
    }

    public User(String userName, String password, Integer status) {
        this.userName = userName;
        this.password = password;
        this.status = status;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

}