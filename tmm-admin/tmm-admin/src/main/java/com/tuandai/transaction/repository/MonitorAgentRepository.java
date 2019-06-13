package com.tuandai.transaction.repository;

import com.tuandai.transaction.domain.MonitorAgent;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MonitorAgentRepository {

    void createMonitorAgentTable();

    void addMonitorAgent(List<MonitorAgent> monitorAgents);

    //void deleteMonitorAgentByServiceName(List<String> services);

    void deleteMonitorAgent();

    List<MonitorAgent> queryMonitorAgentAll();
}
