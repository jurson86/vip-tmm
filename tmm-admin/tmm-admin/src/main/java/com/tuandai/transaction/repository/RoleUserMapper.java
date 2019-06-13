package com.tuandai.transaction.repository;

import com.tuandai.transaction.domain.Role;
import com.tuandai.transaction.domain.RoleUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface RoleUserMapper {
    int deleteByPrimaryKey(Integer pid);

    int insert(RoleUser record);

    int insertSelective(RoleUser record);

    RoleUser selectByPrimaryKey(Integer pid);

    int updateByPrimaryKeySelective(RoleUser record);

    int updateByPrimaryKey(RoleUser record);

    List<Role> findUserRoles(Integer userId);

    int deleteRoleRealate(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

    int insertBatch(List<RoleUser> roleUserList);
}