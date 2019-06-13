package com.tuandai.transaction.service.inf;

import com.tuandai.transaction.bo.MessageState;
import com.tuandai.transaction.bo.MonitorAgentJson;
import com.tuandai.transaction.domain.RegistryAgent;

import java.util.List;
import java.util.Map;

/**
 * 监控类
 */
public interface MonitorService {

    /**
     * 监控 消息各状态数
     *
     * 状态 -> 当前状态的数量
     */
     Map<MessageState, Long> messageStateMonitor(List<String> serviceNames);

    /**
     * 查询各个代理服务的监控指标
     */
    void monitorAgentService();

    /**
     * 拉取注册中心的服务名称列表
     */
    List<String> getApplicationName();


    /**
     * 查询代理服务器处的rpc,done，map等监控信息
     */
     List<MonitorAgentJson> getAgentMonitor();

    /**
     * 添加注册服务名
     */
    boolean addRegistryAgent(List<RegistryAgent> registryAgents);

    boolean deleteRegistryAgent(String serviceName);

}
