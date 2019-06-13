package com.tuandai.transaction.config;

import com.tuandai.transaction.dao.ApplicationDao;
import com.tuandai.transaction.dao.RegistryAgentDao;
import com.tuandai.transaction.domain.Application;
import com.tuandai.transaction.domain.RegistryAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: guoguo
 * @Date: 2018/7/5 0005 16:52
 * @Description:
 */

@Component
public class TmmListener implements ApplicationListener<ApplicationReadyEvent> { //需要指定类型，不然spring 会指定一个现有得监听器，这个监听器不确定，如果存在一个定时监听器，那这个时候就会出现bug


    @Autowired
    private RegistryAgentDao registryAgentDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<RegistryAgent> registryAgentDaoList = registryAgentDao.queryRegistryAgentAll();

        for(RegistryAgent registryAgent:registryAgentDaoList){
            Application application  = new Application();
            application.setPid(registryAgent.getPid());
            application.setApplicationName(registryAgent.getServiceName());
            application.setCreateTime(registryAgent.getCreateTime());
            application.setUpdateTime(registryAgent.getUpdateTime());
            applicationDao.dataMove(application);
        }

    }

}

