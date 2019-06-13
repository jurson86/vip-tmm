package com.tuandai.transaction.repository;

import com.tuandai.transaction.domain.DlqServer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DlqServerRepository {

    void createDlqServerTable();

    void addDlqServer(@Param("alqServer") DlqServer alqServer);

    void deleteDlqServer(@Param("queueName") String queueName,@Param("serviceName") String serviceName);

    int deleteByPrimaryKey(Long pid);

    int updateDlqServer(@Param("beforeServiceName") String beforeServiceName,@Param("afterServiceName") String afterServiceName);

    DlqServer queryDlqServerByQueueName(@Param("queueName") String queueName);

    List<DlqServer> queryDlqServerQueList(@Param("serviceName") String serviceName,@Param("queueName") String queueName);

}
