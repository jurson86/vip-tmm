package com.tuandai.transaction.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Role implements Comparable<Role>{
    private Integer pid;

    private String roleName;

    private Date createTime;


    private Date updateTime;

    private Boolean check =false;

    public Role() {
    }

    public Role(String roleName) {
        this.roleName = roleName;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName == null ? null : roleName.trim();
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

    @Override
    public int compareTo(Role other) {
        return Integer.compare(pid,other.pid);
    }
}