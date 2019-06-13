package com.tuandai.transaction.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.tuandai.transaction.dao.UserDao;
import com.tuandai.transaction.domain.Application;
import com.tuandai.transaction.domain.Role;
import com.tuandai.transaction.domain.User;
import com.tuandai.transaction.repository.UserMapper;
import com.tuandai.transaction.service.inf.UserInfoService;
import com.tuandai.transaction.utils.Response;
import com.tuandai.transaction.vo.ClientParameterVo;
import com.tuandai.transaction.vo.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Author: guoguo
 * @Date: 2018/6/1 0001 17:02
 * @Description:
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/saveUser")
    @ResponseBody
    public Response saveUser(@RequestBody ClientParameterVo clientParameterVo) {

        //User user = new User(clientParameterVo.getUserName(), clientParameterVo.getPassWord(), 1);
        if(StringUtils.isBlank(clientParameterVo.getUserName()) || StringUtils.isBlank(clientParameterVo.getPassWord()) ){
            return Response.error("参数不能为空");
        }
        int result = userDao.createUser(clientParameterVo.getUserName().trim(), clientParameterVo.getPassWord(), 1);
        if (result == 0) {
            return Response.error(clientParameterVo.getUserName() + "已经存在");
        }
        return Response.success("新增成功");

    }

    @RequestMapping("/queryUserlist")
    @ResponseBody
    public Response queryUserlist(@RequestBody(required = false) ClientParameterVo clientParameterVo) {

        if (clientParameterVo == null) {
            clientParameterVo = new ClientParameterVo();
        }
        PageInfo pageInfo = userDao.queryUserList(clientParameterVo.getUserName(), clientParameterVo.getStatus(), clientParameterVo.getPage(), clientParameterVo.getPageSize());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("content", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        return Response.success(result);

    }

    @RequestMapping("/getUserInfo")
    @ResponseBody
    public Response getUserInfo(@RequestBody ClientParameterVo clientParameterVo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", clientParameterVo.getUserId());
        User user = userDao.findUserByParams(params);
        return Response.success(user);
    }

    @RequestMapping("/delUser")
    @ResponseBody
    public Response delUser(@RequestBody ClientParameterVo clientParameterVo) {
        userDao.deleteUser(clientParameterVo.getUserIds());
        return Response.success("删除成功");
    }

    @RequestMapping("/updateUser")
    @ResponseBody
    public Response updateUser(@RequestBody ClientParameterVo clientParameterVo) {
        if(!StringUtils.isNotBlank(clientParameterVo.getUserName())){
            return Response.error("参数不能为空");
        }
        userDao.updateUser(clientParameterVo.getUserId(), clientParameterVo.getUserName().trim(), null, clientParameterVo.getStatus());
        return Response.success("更新成功");
    }

    @RequestMapping("/restUserPassWord")
    @ResponseBody
    public Response updateUserPassWord(@RequestBody ClientParameterVo clientParameterVo) {
        if(!StringUtils.isNotBlank(clientParameterVo.getPassWord())){
            return Response.error("参数不能为空");
        }
        userDao.updateUser(clientParameterVo.getUserId(), null, clientParameterVo.getPassWord(), null);
        return Response.success("更新成功");
    }

    @RequestMapping("/addUserRole")
    @ResponseBody
    public Response addUserRole(@RequestBody ClientParameterVo clientParameterVo, @RequestHeader(name = "token") String token) {
        userDao.addUserRole(clientParameterVo.getUserId(), clientParameterVo.getRoleIds());
        UserInfo userInfo = BaseController.currentUserInfo(token);
        if (userInfo != null) {
            UserInfo currentUser = userInfoService.findByUsername(userInfo.getUserName());
            BaseController.setCurrentUserInfo(token, currentUser);
        }
        return Response.success("更新成功");
    }

    @RequestMapping("/userRoleList")
    @ResponseBody
    Response userRoleList(@RequestBody ClientParameterVo clientParameterVo) {
        Collection<Role> roleList = userDao.findUerRole(clientParameterVo.getUserId());
        return Response.success(roleList);
    }
}
