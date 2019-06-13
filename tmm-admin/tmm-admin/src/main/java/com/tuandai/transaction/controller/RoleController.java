package com.tuandai.transaction.controller;

import com.github.pagehelper.PageInfo;
import com.tuandai.transaction.dao.ApplicationDao;
import com.tuandai.transaction.dao.RoleDao;
import com.tuandai.transaction.domain.Application;
import com.tuandai.transaction.domain.RegistryAgent;
import com.tuandai.transaction.domain.Role;
import com.tuandai.transaction.service.inf.UserInfoService;
import com.tuandai.transaction.utils.Response;
import com.tuandai.transaction.vo.ClientParameterVo;
import com.tuandai.transaction.vo.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Author: guoguo
 * @Date: 2018/6/1 0001 16:05
 * @Description:
 */

@Controller
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/saveRole")
    @ResponseBody
    public Response saveRole(@RequestBody ClientParameterVo clientParameterVo) {
        if(!StringUtils.isNotBlank(clientParameterVo.getRoleName())){
            return Response.error("参数不能为空");
        }
        int result = roleDao.saveRole(clientParameterVo.getRoleName().trim());
        if (result == 0) {
            return Response.error(clientParameterVo.getRoleName() + "已经存在");
        }
        return Response.success("新增成功");

    }

    @RequestMapping("/delRole")
    @ResponseBody
    public Response delRole(@RequestBody ClientParameterVo clientParameterVo) {
        roleDao.deleteRole(clientParameterVo.getRoleIds());
        return Response.success("删除成功");
    }

    @RequestMapping("/getRoleInfo")
    @ResponseBody
    public Response getRoleInfo(@RequestBody ClientParameterVo clientParameterVo) {
        Role role = roleDao.getRoleInfo(clientParameterVo.getRoleId());
        return Response.success(role);
    }

    @RequestMapping("/queryRolelist")
    @ResponseBody
    public Response queryRolelist(@RequestBody(required = false) ClientParameterVo clientParameterVo) {
        if (clientParameterVo == null) {
            clientParameterVo = new ClientParameterVo();
        }
        PageInfo pageInfo = roleDao.queryRoleList(clientParameterVo.getRoleName(), clientParameterVo.getPage(), clientParameterVo.getPageSize());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("content", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        return Response.success(result);
    }

    @RequestMapping("/updateRole")
    @ResponseBody
    public Response updateRole(@RequestBody ClientParameterVo clientParameterVo) {
        if(!StringUtils.isNotBlank(clientParameterVo.getRoleName())){
            return Response.error("参数不能为空");
        }
        roleDao.updateRole(clientParameterVo.getRoleId(), clientParameterVo.getRoleName().trim());
        return Response.success("更新成功");
    }

    @RequestMapping("/addRolePermission")
    @ResponseBody
    public Response addRolePermission(@RequestBody ClientParameterVo clientParameterVo, @RequestHeader(name = "token") String token) {
        roleDao.addRolePermission(clientParameterVo.getRoleId(), clientParameterVo.getApplicationIds());
        UserInfo userInfo = BaseController.currentUserInfo(token);
        if (userInfo != null) {
            UserInfo currentUser = userInfoService.findByUsername(userInfo.getUserName());
            BaseController.setCurrentUserInfo(token, currentUser);
        }
        return Response.success("更新成功");
    }


    @RequestMapping("/rolePermissionList")
    @ResponseBody
    Response rolePermissionList(@RequestBody ClientParameterVo clientParameterVo) {

        List<Application> hasPermission = roleDao.rolePermissionList(clientParameterVo.getRoleId());
        List<Application> permissionList = applicationDao.queryApplicationList(null);
        HashSet set = new HashSet();

        for (Application hasApplication : hasPermission) {
            set.add(hasApplication.getPid());
        }
        List<Application> result = new ArrayList<Application>();
        for (Application application : permissionList) {
            if (!set.contains(application.getPid())) {
                result.add(application);
            }
        }
        result.addAll(hasPermission);
        Collections.sort(result, new Comparator<Application>() {
            @Override
            public int compare(Application o1, Application o2) {
                return o1.getPid().compareTo(o2.getPid());
            }
        });
        if (CollectionUtils.isEmpty(hasPermission)) {
            return Response.success(permissionList);
        }
        return Response.success(result);
    }
}
