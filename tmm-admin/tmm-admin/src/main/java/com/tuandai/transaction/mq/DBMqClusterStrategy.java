package com.tuandai.transaction.mq;

import com.tuandai.transaction.bo.RabbitAddress;
import com.tuandai.transaction.dao.MqClusterDao;
import com.tuandai.transaction.domain.MqCluster;
import com.tuandai.transaction.domain.filter.MqClusterByFilter;
import com.tuandai.transaction.mq.inf.MqClusterStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库加载mq集群配置
 */
@Component
public class DBMqClusterStrategy implements MqClusterStrategy {

    @Autowired
    private MqClusterDao mqClusterDao;

    // mqKey -> RabbitAddress
    @Override
    public Map<String, RabbitAddress> loadRabbitAddressMap() throws Exception {

        MqClusterByFilter filter = new MqClusterByFilter();
        List<MqCluster> list = mqClusterDao.queryMqClusterByFilter(filter);
        Map<String, RabbitAddress> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(list)) {
            for (MqCluster mqCluster : list) {
                RabbitAddress rabbitAddress  = new RabbitAddress();
                rabbitAddress.setUserName(mqCluster.getUserName());
                rabbitAddress.setIp(mqCluster.getHost());
                rabbitAddress.setPort(mqCluster.getPort());
                rabbitAddress.setAdminUrl(mqCluster.getAdminUrl());
                rabbitAddress.setPassword(mqCluster.getPassWord());
                map.put(mqCluster.getMqKey(), rabbitAddress);
            }
        }
        return map;
    }
}
