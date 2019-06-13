package com.tuandai.transaction.service;

import com.tuandai.transaction.bo.MqClusterEvent;
import com.tuandai.transaction.dao.MqClusterDao;
import com.tuandai.transaction.domain.MqCluster;
import com.tuandai.transaction.domain.filter.MqClusterByFilter;
import com.tuandai.transaction.service.inf.MqClusterService;
import com.tuandai.transaction.utils.BZStatusCode;
import com.tuandai.transaction.utils.ServiceException;
import com.tuandai.transaction.vo.MqClusterVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MqClusterServiceImpl implements MqClusterService, ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(MqClusterServiceImpl.class);

    private ApplicationEventPublisher publisher;

    @Autowired
    private MqClusterDao mqClusterDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMqCluster(MqClusterVo mqClusterVo) throws Exception {
        checkMqClusterVo(mqClusterVo);
        MqCluster mqCluster = new MqCluster();
        BeanUtils.copyProperties(mqClusterVo, mqCluster);
        Date now = new Date(System.currentTimeMillis());
        mqCluster.setCreateTime(now);
        mqCluster.setUpdateTime(now);
        List<MqCluster> mqClusters = new ArrayList<>(1);
        mqClusters.add(mqCluster);

        // 查询是否存在
        MqClusterByFilter filter = new MqClusterByFilter();
        filter.setMqKey(mqClusterVo.getMqKey());
        List<MqCluster> ls = mqClusterDao.queryMqClusterByFilter(filter);
        filter.setMqKey(null);
        filter.setHost(mqClusterVo.getHost());
        filter.setPort(mqClusterVo.getPort());
        List<MqCluster> ls2 = mqClusterDao.queryMqClusterByFilter(filter);
        if (!CollectionUtils.isEmpty(ls) || !CollectionUtils.isEmpty(ls2)) {
            logger.info("新增的mqKey/host+port已经存在..."); // 同一个host+port请配置一个高权限
            return;
        }
        mqClusterDao.addMqClusters(mqClusters);
        publishMqClusterEvent();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMqCluster(String pids) throws Exception {
        List<Long> pidList = checkPids(pids);
        int list = mqClusterDao.deleteMqCluster(pidList);
        publishMqClusterEvent();
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMqCluster(MqClusterVo mqClusterVo) throws Exception {
        checkUpdateMqCluster(mqClusterVo);
        MqCluster mqCluster = new MqCluster();
        BeanUtils.copyProperties(mqClusterVo, mqCluster);
        Date now = new Date(System.currentTimeMillis());
        mqCluster.setCreateTime(now);
        mqCluster.setUpdateTime(now);
        mqClusterDao.updateMqCluster(mqCluster);
        publishMqClusterEvent();
    }

    @Override
    public List<MqCluster> queryMqCluster(MqClusterVo mqClusterVo) throws Exception {
        MqClusterByFilter filter = new MqClusterByFilter();
        if (mqClusterVo != null) {
            BeanUtils.copyProperties(mqClusterVo, filter);
        }
        return mqClusterDao.queryMqClusterByFilter(filter);
    }

    private void checkUpdateMqCluster(MqClusterVo mqClusterVo) throws Exception {
        if (mqClusterVo == null) {
            logger.error("MqClusterService.checkupdateMqCluster mqClusterVo 不能为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        if (mqClusterVo.getPid() == null || mqClusterVo.getPid().equals(0)) {
            logger.error("MqClusterService.checkupdateMqCluster pid 不能为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
    }

    private List<Long> checkPids(String pids) throws Exception {
        if (StringUtils.isEmpty(pids)) {
            logger.error("MqClusterService.checkPids pids 不能为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        String[] pid = pids.split(",");
        List<String> pidStrs = Arrays.asList(pid);
        if (CollectionUtils.isEmpty(pidStrs)) {
            logger.error("MqClusterService.checkPids pids 不能为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        return pidStrs.stream().map(o -> Long.valueOf(o)).collect(Collectors.toList());
    }

    private void checkMqClusterVo(MqClusterVo mqClusterVo) throws Exception {
        if (mqClusterVo == null) {
            logger.error("MqClusterService.checkMqClusterVo mqClusterVo 不能为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        if (StringUtils.isEmpty(mqClusterVo.getMqKey())) {
            logger.error("MqClusterService.checkMqClusterVo mqClusterVo.MqKey 不能为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        if (StringUtils.isEmpty(mqClusterVo.getHost())) {
            logger.error("MqClusterService.checkMqClusterVo mqClusterVo.Host 不能为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        if (mqClusterVo.getPort() == null || mqClusterVo.getPort().equals(0)) {
            logger.error("MqClusterService.checkMqClusterVo mqClusterVo.Port 不能为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        if (StringUtils.isEmpty(mqClusterVo.getAdminUrl())) {
            logger.error("MqClusterService.checkMqClusterVo mqClusterVo.AdminUrl 不能为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        if (StringUtils.isEmpty(mqClusterVo.getPassWord())) {
            logger.error("MqClusterService.checkMqClusterVo mqClusterVo.PassWord 不能为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        if (StringUtils.isEmpty(mqClusterVo.getUserName())) {
            logger.error("MqClusterService.checkMqClusterVo mqClusterVo.UserName 不能为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }

    private void publishMqClusterEvent(){
        try {
            MqClusterEvent event = new MqClusterEvent(this);
            publisher.publishEvent(event);
        } catch (Exception e) {
            logger.error("mq集群更新操作失败....", e);
            throw new ServiceException(BZStatusCode.MQ_CONNECTION_FAIL);
        }
    }

}
