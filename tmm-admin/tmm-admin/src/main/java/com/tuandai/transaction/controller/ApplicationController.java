package com.tuandai.transaction.controller;

import com.github.pagehelper.PageInfo;
import com.netflix.discovery.converters.Auto;
import com.tuandai.transaction.dao.ApplicationDao;
import com.tuandai.transaction.dao.RegistryAgentDao;
import com.tuandai.transaction.domain.Application;
import com.tuandai.transaction.domain.RegistryAgent;
import com.tuandai.transaction.domain.User;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author: guoguo
 * @Date: 2018/6/1 0001 17:02
 * @Description:
 */

@Controller
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/saveApplication")
    @ResponseBody
    public Response saveApplication(@RequestHeader(value = "token") String token,@RequestBody ClientParameterVo clientParameterVo) {
        if(!StringUtils.isNotBlank(clientParameterVo.getApplicationName())){
            return Response.error("参数不能为空");
        }

        UserInfo userInfo = BaseController.currentUserInfo(token);

        int result = applicationDao.saveApplication(clientParameterVo.getApplicationName().trim(),userInfo.getRoleList().get(0).getPid());
        if (result == 0) {
            return Response.error(clientParameterVo.getApplicationName() + "已经存在");
        }
        UserInfo currentUser = userInfoService.findByUsername(userInfo.getUserName());
        BaseController.setCurrentUserInfo(token, currentUser);
        return Response.success("新增成功");
    }

    @RequestMapping("/delApplication")
    @ResponseBody
    public Response delApplication(@RequestBody ClientParameterVo clientParameterVo) {
        applicationDao.deleteApplication(clientParameterVo.getApplicationIds());
        return Response.success("删除成功");
    }

    @RequestMapping("/getApplicationInfo")
    @ResponseBody
    public Response getApplicationInfo(@RequestBody ClientParameterVo clientParameterVo) {
        Application application = applicationDao.getApplication(clientParameterVo.getApplicationId());
        return Response.success(application);
    }

    @RequestMapping("/updateApplication")
    @ResponseBody
    public Response updateApplication(@RequestBody ClientParameterVo clientParameterVo) {
        if(!StringUtils.isNotBlank(clientParameterVo.getApplicationName())){
            return Response.error("参数不能为空");
        }
        applicationDao.updateApplication(clientParameterVo.getApplicationId(), clientParameterVo.getApplicationName().trim());
        return Response.success("更新成功");
    }

    @RequestMapping("/queryApplicationlist")
    @ResponseBody
    public Response queryApplicationlist(@RequestHeader(value = "token") String token,@RequestBody(required = false) ClientParameterVo clientParameterVo) {
        if (clientParameterVo == null) {
            clientParameterVo = new ClientParameterVo();
        }
        UserInfo userInfo = BaseController.currentUserInfo(token);
        PageInfo pageInfo = applicationDao.queryApplicationList(clientParameterVo.getApplicationName(), userInfo.getPid(),clientParameterVo.getPage(), clientParameterVo.getPageSize());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("content", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        return Response.success(result);
    }


}
