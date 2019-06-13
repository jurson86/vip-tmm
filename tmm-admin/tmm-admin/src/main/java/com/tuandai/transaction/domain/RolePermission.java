package com.tuandai.transaction.domain;

public class RolePermission {
    private Integer pid;

    private Integer applicationId;

    private Integer roleId;

    public RolePermission() {
    }

    public RolePermission(Integer applicationId, Integer roleId) {
        this.applicationId = applicationId;
        this.roleId = roleId;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
}