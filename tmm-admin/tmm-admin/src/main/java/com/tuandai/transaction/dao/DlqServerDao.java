package com.tuandai.transaction.dao;

import com.tuandai.transaction.domain.DlqServer;
import com.tuandai.transaction.repository.DlqServerRepository;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DlqServerDao {

    @Autowired
    private DlqServerRepository dlqServerRepository;

    public void createDlqServerTable() {
        dlqServerRepository.createDlqServerTable();
    }

    public void addDlqServer( DlqServer alqServer) {
        dlqServerRepository.addDlqServer(alqServer);
    }

    public void deleteDlqServer( String queueName,String serviceName) {
        dlqServerRepository.deleteDlqServer(queueName,serviceName);
    }

    public DlqServer queryDlqServerByQueueName( String queueName) {
       return dlqServerRepository.queryDlqServerByQueueName(queueName);
    }

    public List<DlqServer> queryDlqServerQueList(String serviceName, String queueName){
        return dlqServerRepository.queryDlqServerQueList(serviceName,queueName);
    }

    public int updateDlqServer(String beforeServiceName, String afterServiceName){
        return dlqServerRepository.updateDlqServer(beforeServiceName,afterServiceName);
    }

    public int deleteDlqServer(Long pid){
        return dlqServerRepository.deleteByPrimaryKey(pid);
    }

}
