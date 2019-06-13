package com.tuandai.transaction.repository;

import com.github.pagehelper.Page;
import com.tuandai.transaction.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
@Mapper
public interface UserMapper {
    int deleteByPrimaryKey(Integer pid);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer pid);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    List<User> queryUserlistByParams(@Param("map") Map<String,Object> params);

    void deleteUserByIds(List<Integer> userIds);
}