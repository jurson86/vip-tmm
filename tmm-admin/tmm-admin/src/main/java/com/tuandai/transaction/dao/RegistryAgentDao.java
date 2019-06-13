package com.tuandai.transaction.dao;

import com.tuandai.transaction.domain.RegistryAgent;
import com.tuandai.transaction.repository.RegistryAgentRepository;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class RegistryAgentDao {

    @Autowired
    private RegistryAgentRepository registryAgentRepository;

    public int updateByPrimaryKey(RegistryAgent record) {
        return registryAgentRepository.updateByPrimaryKey(record);
    }

    public void createRegistryAgentTable() {
        registryAgentRepository.createRegistryAgentTable();
    }

    public  void addRegistryAgent(List<RegistryAgent> registryAgents) {
        registryAgentRepository.addRegistryAgent(registryAgents);
    }

    public RegistryAgent queryRegistryAgentByServerName(String serviceName) {
        if (StringUtils.isEmpty(serviceName)) {
            return null;
        }
       return registryAgentRepository.queryRegistryAgentByServerName(serviceName);
    }

    public void deleteRegistryAgentById(List<Integer> pids) {
        registryAgentRepository.deleteRegistryAgentById(pids);
    }

    public List<RegistryAgent> queryRegistryAgentAll() {
        return  registryAgentRepository.queryRegistryAgentAll();
    }
}
