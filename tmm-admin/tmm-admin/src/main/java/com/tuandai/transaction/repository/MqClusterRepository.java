package com.tuandai.transaction.repository;

import com.tuandai.transaction.domain.MqCluster;
import com.tuandai.transaction.domain.filter.MqClusterByFilter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MqClusterRepository {

    void createMqClusterTable();

    List<MqCluster> queryMqClusterByFilter(@Param("filter") MqClusterByFilter filter);

    void addMqClusters(@Param("mqClusters") List<MqCluster> mqClusters);

    int deleteMqCluster(@Param("pids") List<Long> pids);

    void updateMqCluster(@Param("mqCluster") MqCluster mqCluster);

}
