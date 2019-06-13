package com.tuandai.transaction.repository;

import com.tuandai.transaction.domain.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
@Mapper
public interface RoleMapper {
    int deleteByPrimaryKey(Integer pid);

    int insert(Role record);

    int insertSelective(Role record);

    Role selectByPrimaryKey(Integer pid);

    int updateByPrimaryKeySelective(Role record);

    int updateByPrimaryKey(Role record);

    List<Role> queryRolelistByParams(@Param("map") Map<String,Object> params);


}