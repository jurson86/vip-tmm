package com.tuandai.transaction.repository;

import com.tuandai.transaction.domain.Application;
import com.tuandai.transaction.domain.RegistryAgent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface RegistryAgentRepository {

    void createRegistryAgentTable();

    void addRegistryAgent(List<RegistryAgent> registryAgents);

    RegistryAgent queryRegistryAgentByServerName(@Param("serviceName") String serviceName);

    void deleteRegistryAgentById(List<Integer> pids);

    List<RegistryAgent> queryRegistryAgentAll();

    RegistryAgent selectByPrimaryKey(Integer pid);

    int updateByPrimaryKeySelective(RegistryAgent record);

    int updateByPrimaryKey(RegistryAgent record);

    List<RegistryAgent> queryApplicationListByParams(@Param("map") Map<String,Object> params);

    int deleteByPrimaryKey(Integer pid);

    int insertSelective(RegistryAgent record);


}
