package com.tuandai.transaction.repository;


import com.tuandai.transaction.bo.Limiter;
import com.tuandai.transaction.bo.TwoTuple;
import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.domain.filter.TransactionCheckFilter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface TransactionCheckRepository {

    void createIfNotExistsTable();

    void insertBatch(List<TransactionCheck> transactionStates);

    List<TransactionCheck> queryTransactionCheckByFilter(@Param("filter") TransactionCheckFilter transactionCheckFilter, @Param("limiter")  Limiter limiter);

    TransactionCheck queryTransactionCheckById(@Param("pid") long pid, @Param("serviceNames") List<String> serviceNames);

    List<TransactionCheck> queryTransactionCheckByIds(@Param("pids") List<Long> pids);

    void update(TransactionCheck transactionCheck);

    // 删除表里面所有的数据
    void deleteAll();

    // 统计各状态下的消息状态数量
    List<TwoTuple<Integer, Long>> messageStateCountMap(@Param("serviceNames") List<String> serviceNames);

    List<TwoTuple<String, Long>> dlqList(@Param("serviceNames") List<String> serviceNames);

    void delete(@Param("pids")List<Long> pids, @Param("serviceNames") List<String> serviceNames);

}
