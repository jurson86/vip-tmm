package com.tuandai.transaction.repository;

import com.tuandai.transaction.domain.Application;
import com.tuandai.transaction.domain.Role;
import com.tuandai.transaction.domain.filter.ApplicationFilter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
@Mapper
public interface ApplicationMapper {
    int deleteByPrimaryKey(Integer pid);

    int insert(Application record);

    int insertSelective(Application record);

    void insertBatch(@Param("applications") List<Application> applications);

    Application selectByPrimaryKey(Integer pid);

    int updateByPrimaryKeySelective(Application record);

    int updateByPrimaryKey(Application record);

    List<Application> queryApplicationListByParams(@Param("map") Map<String,Object> params);

    List<Application> queryApplicationListByFilter(@Param("filter") ApplicationFilter applicationFilter);

}