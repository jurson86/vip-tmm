package com.tuandai.transaction.service.inf;

import com.tuandai.transaction.domain.MqCluster;
import com.tuandai.transaction.vo.MqClusterVo;

import java.util.List;

public interface MqClusterService {

    void addMqCluster(MqClusterVo mqClusterVo) throws Exception;

    int deleteMqCluster(String pids) throws Exception;

    void updateMqCluster(MqClusterVo mqClusterVo) throws Exception;

    List<MqCluster> queryMqCluster(MqClusterVo mqClusterVo) throws Exception;

}
