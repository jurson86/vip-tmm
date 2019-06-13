package com.tuandai.transaction.repository;

import com.tuandai.transaction.domain.Application;
import com.tuandai.transaction.domain.RegistryAgent;
import com.tuandai.transaction.domain.RolePermission;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface RolePermissionMapper {
    int deleteByPrimaryKey(Integer pid);

    int insert(RolePermission record);

    int insertSelective(RolePermission record);

    RolePermission selectByPrimaryKey(Integer pid);

    int updateByPrimaryKeySelective(RolePermission record);

    int updateByPrimaryKey(RolePermission record);

    List<Application> findRolePermissions(Integer roleId);

    List<Application> findUserPermissions(@Param("map") Map<String,Object> params);

    int insertBatch(List<RolePermission> rolePermissionList);

    int deleteRolePermissionRealate(@Param("roleId") Integer roleId, @Param("applicationId") Integer applicationId);



}