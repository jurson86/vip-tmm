package com.tuandai.transaction.service;

import com.tuandai.transaction.dao.ApplicationDao;
import com.tuandai.transaction.dao.RoleDao;
import com.tuandai.transaction.dao.UserDao;
import com.tuandai.transaction.domain.Application;
import com.tuandai.transaction.domain.RegistryAgent;
import com.tuandai.transaction.domain.Role;
import com.tuandai.transaction.domain.User;
import com.tuandai.transaction.repository.*;
import com.tuandai.transaction.service.inf.UserInfoService;
import com.tuandai.transaction.utils.BeanMapper;
import com.tuandai.transaction.vo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @Author: guoguo
 * @Date: 2018/5/31 0031 11:46
 * @Description:
 */


@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    private RoleUserMapper roleUserMapper;
    @Autowired
    private ApplicationDao applicationDao;
    /*@Autowired
    private RegistryAgentRepository registryAgentRepository;*/

    @Override
    public UserInfo findByUsername(String userName) {
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("insertUserName",userName);
        params.put("onLogin","login");
        User user = userDao.findUserByParams(params);
        if(user == null){
            return null;
        }
        UserInfo userInfo = BeanMapper.map(user,UserInfo.class);
        Set<String> roleSet = new HashSet<String>();
        Set<String> permissionSet = new HashSet<String>();
        List<Role> roleList = roleUserMapper.findUserRoles(user.getPid());
        userInfo.setRoleList(roleList);

        if(!CollectionUtils.isEmpty(roleList)){
            for(Role role:roleList){
                roleSet.add(role.getRoleName());
            }
        }

        Map<String,Object> nextParams = new HashMap<String,Object>();
        nextParams.put("userId",user.getPid());
        List<Application> permisstionList = rolePermissionMapper.findUserPermissions(nextParams);
        if(userInfo.getPid().equals(1)){
            permisstionList = applicationDao.queryApplicationList(null);
        }
        if(!CollectionUtils.isEmpty(permisstionList)){
            for( Application application:permisstionList){
                permissionSet.add(application.getApplicationName());
            }
        }
        userInfo.setRoleSet(roleSet);
        userInfo.setPermissionSet(permissionSet);

       /* if(userInfo.getPid().equals(1)){
            userInfo.getRoleSet().clear();
            userInfo.getRoleSet().add("*");
            userInfo.getPermissionSet().clear();
            userInfo.getPermissionSet().add("*");
        }*/
        return userInfo;
    }
}
