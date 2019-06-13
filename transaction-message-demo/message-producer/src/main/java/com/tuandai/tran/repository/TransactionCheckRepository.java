package com.tuandai.tran.repository;


import com.tuandai.tran.domain.TransactionCheck;
import com.tuandai.tran.domain.filter.TransactionCheckFilter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionCheckRepository {

    void createIfNotExistsTable();

    void insert(TransactionCheck transactionCheck);

    void update(TransactionCheck transactionCheck);

    void delete(String msgId);

    TransactionCheck queryByMessageId(String msgId);

    List<TransactionCheck> queryTransactionCheckByFilter(@Param("filter") TransactionCheckFilter filter);

}
