package com.tuandai.transaction.domain;

public class RoleUser {
    private Integer pid;

    private Integer roleId;

    private Integer userId;

    public RoleUser() {
    }

    public RoleUser(Integer roleId, Integer userId) {
        this.roleId = roleId;
        this.userId = userId;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}