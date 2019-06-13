package com.tuandai.transaction.dao;

import com.tuandai.transaction.domain.MqCluster;
import com.tuandai.transaction.domain.filter.MqClusterByFilter;
import com.tuandai.transaction.repository.MqClusterRepository;
import com.tuandai.transaction.utils.BZStatusCode;
import com.tuandai.transaction.utils.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class MqClusterDao {

    private static final Logger logger = LoggerFactory.getLogger(MqClusterDao.class);

    @Autowired
    private MqClusterRepository mqClusterRepository;

    public void createMqClusterTable() {
        mqClusterRepository.createMqClusterTable();
    }

    public List<MqCluster> queryMqClusterByFilter(MqClusterByFilter filter) throws Exception {
        if (filter == null) {
            logger.error("MqClusterDao.queryMqClusterByFilter filter 参数为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        return mqClusterRepository.queryMqClusterByFilter(filter);
    }

    public void addMqClusters(List<MqCluster> mqClusters) {
        if (CollectionUtils.isEmpty(mqClusters)) {
            logger.info("MqClusterDao.addMqClusters mqClusters 参数为空...");
            return;
        }
        mqClusterRepository.addMqClusters(mqClusters);
    }

    public int deleteMqCluster(List<Long> pids) {
        if (CollectionUtils.isEmpty(pids)) {
            logger.info("MqClusterDao.deleteMqCluster pid 参数为空...");
            return 0;
        }
       return mqClusterRepository.deleteMqCluster(pids);
    }

    public void updateMqCluster(MqCluster mqCluster) throws Exception {
        if (mqCluster == null) {
            logger.error("MqClusterDao.deleteMqCluster mqCluster 参数为空...");
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        mqClusterRepository.updateMqCluster(mqCluster);
    }
}
