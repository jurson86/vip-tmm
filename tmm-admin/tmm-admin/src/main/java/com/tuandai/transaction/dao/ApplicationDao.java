package com.tuandai.transaction.dao;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tuandai.transaction.domain.Application;
import com.tuandai.transaction.domain.RegistryAgent;
import com.tuandai.transaction.domain.RolePermission;
import com.tuandai.transaction.domain.filter.ApplicationFilter;
import com.tuandai.transaction.repository.ApplicationMapper;
import com.tuandai.transaction.repository.RegistryAgentRepository;
import com.tuandai.transaction.repository.RolePermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @Author: guoguo
 * @Date: 2018/6/1 0001 15:54
 * @Description:
 */

@Component
public class ApplicationDao {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private DlqServerDao dlqServerDao;

    /*@Autowired
    private RegistryAgentRepository registryAgentRepository;*/

    public void insertBatch(List<Application> applications) {
        if (CollectionUtils.isEmpty(applications)) {
            return;
        }
        List<String> list = new ArrayList<>();
        for (Application application : applications) {
            list.add(application.getApplicationName());
        }
        Map<String, Application> map = queryApplicationMapByApplicationName(list);
        List<Application> parms = new ArrayList<>();
        for (Application application : applications) {
            if (!map.containsKey(application.getApplicationName())) {
                parms.add(application);
            }
        }
        if (!CollectionUtils.isEmpty(parms)) {
            applicationMapper.insertBatch(parms);
        }
    }

    public Map<String, Application> queryApplicationMapByApplicationName(List<String> applicationNames) {
        ApplicationFilter filter = new ApplicationFilter();
        filter.setApplicationNames(applicationNames);
        List<Application> results = applicationMapper.queryApplicationListByFilter(filter);
        Map<String, Application> map = new HashMap<>();
        if (results != null) {
            for (Application result : results) {
                map.put(result.getApplicationName(), result);
            }
        }
        return map;
    }

    public int saveApplication(String applicationName,Integer roleId){
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("insertApplicationName",applicationName);
        List<Application> applicationList = applicationMapper.queryApplicationListByParams(params);
        if(applicationList.size()>0){
            return 0;
        }
        Application application = new Application(applicationName);
        applicationMapper.insertSelective(application);
        if(!roleId.equals(1)) {
            RolePermission rolePermission = new RolePermission(application.getPid(), roleId);
            rolePermissionMapper.insert(rolePermission);
        }
        return 1;
    }

    public void dataMove(Application application){
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("insertApplicationName",application.getApplicationName());
        List<Application> applicationList = applicationMapper.queryApplicationListByParams(params);
        if(applicationList.size()>0){
            return;
        }
        params.remove("insertApplicationName");
        params.put("applicationId",application.getPid());
        List<Application> applicationListNext = applicationMapper.queryApplicationListByParams(params);
        if(applicationListNext.size()>0){
            return;
        }
        applicationMapper.insertSelective(application);
    }

    public Application getApplication(Integer applicationid){
        return applicationMapper.selectByPrimaryKey(applicationid);
    }

    public void updateApplication(Integer applicationId,String applicationName){
        Application application = applicationMapper.selectByPrimaryKey(applicationId);
        String beforeServiceName = application.getApplicationName();
        application.setApplicationName(applicationName);
        application.setUpdateTime(new Date());
        applicationMapper.updateByPrimaryKeySelective(application);
        dlqServerDao.updateDlqServer(beforeServiceName,applicationName);

    }

    public void deleteApplication(List<Integer> pidList){
         for(Integer pid : pidList){
             Application application = applicationMapper.selectByPrimaryKey(pid);
             dlqServerDao.deleteDlqServer(null,application.getApplicationName());
             applicationMapper.deleteByPrimaryKey(pid);
             rolePermissionMapper.deleteRolePermissionRealate(null,pid);
        }
    }

    public PageInfo queryApplicationList(String applicationName,Integer userId, int page, int pageSize){
        PageHelper.startPage(page, pageSize,"    create_time DESC ");
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("applicationName",applicationName);
        List<Application> applicationList = null;
        if(userId.equals(1)) {
            applicationList = applicationMapper.queryApplicationListByParams(params);
        }else {
            params.put("userId",userId);
            applicationList = rolePermissionMapper.findUserPermissions(params);
        }
        PageInfo<Application> pageInfo = new PageInfo<Application>(applicationList);
        return pageInfo;
    }

    public List<Application> queryApplicationList(String applicationName){
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("applicationName",applicationName);
        List<Application> applicationList = applicationMapper.queryApplicationListByParams(params);

       /* List<RegistryAgent> applicationList = null;
        if(userId.equals(1)) {
             applicationList = registryAgentRepository.queryApplicationListByParams(params);
        }else {
             params.put("applicationName",applicationName);
             applicationList = rolePermissionMapper.findUserPermissions(params);
        }*/
        return applicationList;
    }


}
