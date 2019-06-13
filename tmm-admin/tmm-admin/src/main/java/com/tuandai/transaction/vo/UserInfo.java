package com.tuandai.transaction.vo;

import com.tuandai.transaction.domain.Role;

import java.io.Serializable;
import java.util.*;

/**
 * @Author: guoguo
 * @Date: 2018/5/31 0031 10:25
 * @Description:
 */
public class UserInfo implements Serializable {

    private Integer pid;

    private String userName;

    private String password;


    //private String salt;

    private Integer status;

    /*private Date createTime;

    private Date updateTime;*/

    private Set<String> roleSet = new HashSet<String>();
    private Set<String> permissionSet = new HashSet<String>();

    private List<Role> roleList = new ArrayList<Role>();

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
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Set<String> getRoleSet() {
        return roleSet;
    }

    public void setRoleSet(Set<String> roleSet) {
        this.roleSet = roleSet;
    }

    public Set<String> getPermissionSet() {
        return permissionSet;
    }

    public void setPermissionSet(Set<String> permissionSet) {
        this.permissionSet = permissionSet;
    }

    public List<Role> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }
}
