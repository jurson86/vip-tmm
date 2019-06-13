package com.tuandai.transaction.dao;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tuandai.transaction.domain.Role;
import com.tuandai.transaction.domain.RoleUser;
import com.tuandai.transaction.domain.User;
import com.tuandai.transaction.repository.RoleMapper;
import com.tuandai.transaction.repository.RoleUserMapper;
import com.tuandai.transaction.repository.UserMapper;
import com.tuandai.transaction.utils.MD5Utils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @Author: guoguo
 * @Date: 2018/5/30 0030 16:05
 * @Description:
 */

@Component
public class UserDao {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleUserMapper roleUserMapper;

    @Autowired
    private RoleMapper roleMapper;

    public int createUser(String userName, String passWord, Integer status) {
       /* RandomNumberGenerator randomNumberGenerator = new SecureRandomNumberGenerator();
         String algorithmName = "md5";
         int hashIterations = 2;
        String newPassword = new SimpleHash(algorithmName, passWord,  ByteSource.Util.bytes(userName), hashIterations).toHex();*/
        //String newPassword = new SimpleHash(algorithmName, user.getPassword()).toHex();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("insertUserName", userName);
        List<User> userList = userMapper.queryUserlistByParams(params);
        if (!org.springframework.util.CollectionUtils.isEmpty(userList)) {
            return 0;
        }
        String newPassword = MD5Utils.encryption(passWord,2);
        User user = new User(userName, newPassword, 1);
        return userMapper.insertSelective(user);
    }

    public int createUserAndRole(User user){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("insertUserName", user.getUserName());
        List<User> userList = userMapper.queryUserlistByParams(params);
        if (!org.springframework.util.CollectionUtils.isEmpty(userList)) {
            return 0;
        }
        String newPassword = MD5Utils.encryption(user.getPassword(),2);
        user.setPassword(newPassword);
        userMapper.insertSelective(user);
        RoleUser roleUser = new RoleUser(2,user.getPid());
        roleUserMapper.insert(roleUser);
        return 1;
    }

    public User findUserByParams(Map<String, Object> params) {
        List<User> userList = userMapper.queryUserlistByParams(params);
        User user = org.springframework.util.CollectionUtils.isEmpty(userList) ? null : userList.get(0);
        return user;
    }


    public PageInfo queryUserList(String userName, Integer status, int page, int pageSize) {
        PageHelper.startPage(page, pageSize," create_time DESC");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userName", userName);
        params.put("status", status);
        List<User> userList = userMapper.queryUserlistByParams(params);
        PageInfo<User> pageInfo = new PageInfo<User>(userList);
        return pageInfo;
    }

    public void deleteUser(List<Integer> userIds) {
        for (Integer userId : userIds) {
            userMapper.deleteByPrimaryKey(userId);
            roleUserMapper.deleteRoleRealate(userId, null);
        }
    }

    public void updateUser(Integer userId, String userName, String passWord, Integer status) {
        User user = userMapper.selectByPrimaryKey(userId);
        user.setUserName(userName);
        user.setPassword(StringUtils.isNotBlank(passWord) ? MD5Utils.encryption(passWord,2) : null);
        user.setStatus(status);
        user.setUpdateTime(new Date());
        userMapper.updateByPrimaryKeySelective(user);
    }

    public void addUserRole(Integer userId, List<Integer> roleList) {
        roleUserMapper.deleteRoleRealate(userId, null);
        if (roleList.size() > 0) {
            List<RoleUser> roleUsersList = new ArrayList<RoleUser>();
            Set<Integer> noRepeat = new HashSet<Integer>(roleList);
            for (Integer roleId : noRepeat) {
                RoleUser roleUser = new RoleUser(roleId, userId);
                roleUsersList.add(roleUser);
            }
            roleUserMapper.insertBatch(roleUsersList);
        }
    }


    public  List<Role> findUerRole(Integer userId) {
        List<Role> hasRoles = roleUserMapper.findUserRoles(userId);
        HashSet set = new HashSet();
        for (Role role : hasRoles) {
            role.setCheck(true);
            set.add(role.getPid());
        }
        List<Role> allRoles = roleMapper.queryRolelistByParams(new HashMap<String, Object>());
        List<Role> result = new ArrayList<Role>();
        //hasRoles.clear();
        for (Role role : allRoles) {
            if (!set.contains(role.getPid())) {
                result.add(role);
            }
        }

        result.addAll(hasRoles);
        Collections.sort(result, new Comparator<Role>() {
            @Override
            public int compare(Role o1, Role o2) {
                return o1.getPid().compareTo(o2.getPid());
            }
        });
        if (CollectionUtils.isEmpty(hasRoles)) {
            return allRoles;
        }
        return result;
    }


}
