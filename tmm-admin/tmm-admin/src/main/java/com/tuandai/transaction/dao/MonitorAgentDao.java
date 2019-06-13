package com.tuandai.transaction.dao;

import com.tuandai.transaction.domain.MonitorAgent;
import com.tuandai.transaction.repository.MonitorAgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class MonitorAgentDao {

    @Autowired
    private MonitorAgentRepository monitorAgentRepository;

    public void createMonitorAgentTable() {
        monitorAgentRepository.createMonitorAgentTable();
    }

    public void addMonitorAgent(List<MonitorAgent> monitorAgents) {
        if (!CollectionUtils.isEmpty(monitorAgents)) {
            monitorAgentRepository.addMonitorAgent(monitorAgents);
        }
    }

//    public void deleteMonitorAgentByServiceName(List<String> services) {
//        monitorAgentRepository.deleteMonitorAgentByServiceName(services);
//    }

    public void deleteMonitorAgent() {
        monitorAgentRepository.deleteMonitorAgent();
    }

    public List<MonitorAgent> queryMonitorAgentAll() {
       return monitorAgentRepository.queryMonitorAgentAll();
    }
}
