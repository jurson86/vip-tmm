package com.tuandai.transaction.dao;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tuandai.transaction.domain.*;
import com.tuandai.transaction.repository.RoleMapper;
import com.tuandai.transaction.repository.RolePermissionMapper;
import com.tuandai.transaction.repository.RoleUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author: guoguo
 * @Date: 2018/6/1 0001 12:15
 * @Description:
 */

@Component
public class RoleDao {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private RoleUserMapper roleUserMapper;

    public int saveRole(String roleName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("insertRoleName", roleName);

        List<Role> roleList = roleMapper.queryRolelistByParams(params);
        if (roleList.size() > 0) {
            return 0;
        }
        Role role = new Role(roleName);
        return roleMapper.insertSelective(role);
    }

    public Role getRoleInfo(Integer roleId) {
        return roleMapper.selectByPrimaryKey(roleId);
    }

    public PageInfo queryRoleList(String roleName, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("roleName", roleName);
        List<Role> roleList = roleMapper.queryRolelistByParams(params);
        PageInfo<Role> pageInfo = new PageInfo<Role>(roleList);
        return pageInfo;
    }

    public void deleteRole(List<Integer> roleIds) {
        for (Integer roleId : roleIds) {
            roleMapper.deleteByPrimaryKey(roleId);
            roleUserMapper.deleteRoleRealate(null, roleId);
            rolePermissionMapper.deleteRolePermissionRealate(roleId, null);
        }
    }

    public void updateRole(Integer roleId, String roleName) {
        Role role = roleMapper.selectByPrimaryKey(roleId);
        role.setRoleName(roleName);
        role.setUpdateTime(new Date());
        roleMapper.updateByPrimaryKeySelective(role);
    }

    public void addRolePermission(Integer roleId, List<Integer> applicationList) {
        rolePermissionMapper.deleteRolePermissionRealate(roleId, null);
        if (applicationList.size() > 0) {
            List<RolePermission> rolePermissionList = new ArrayList<RolePermission>();
            Set<Integer> noRepeat = new HashSet<Integer>(applicationList);
            for (Integer pid : noRepeat) {
                RolePermission rolePermission = new RolePermission(pid, roleId);
                rolePermissionList.add(rolePermission);
            }
            rolePermissionMapper.insertBatch(rolePermissionList);
        }
    }

    public List<Application> rolePermissionList(Integer roleId) {
        List<Application> permissionList = rolePermissionMapper.findRolePermissions(roleId);
        for (Application application : permissionList) {
            application.setCheck(true);
        }
        return permissionList;
    }
}
