package com.tuandai.transaction.dao;

import com.tuandai.transaction.bo.Limiter;
import com.tuandai.transaction.bo.TwoTuple;
import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.domain.filter.TransactionCheckFilter;
import com.tuandai.transaction.repository.TransactionCheckRepository;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class TransactionCheckDao {

    @Autowired
    private TransactionCheckRepository transactionCheckRepository;

    public void createIfNotExistsTable() {
        transactionCheckRepository.createIfNotExistsTable();
    }

    public void insertBatch(List<TransactionCheck> transactionStates) {
        transactionCheckRepository.insertBatch(transactionStates);
    }

    public List<TransactionCheck> queryTransactionCheckByFilter(TransactionCheckFilter transactionCheckFilter, Limiter limiter) {
        return transactionCheckRepository.queryTransactionCheckByFilter(transactionCheckFilter, limiter);
    }

    public TransactionCheck queryTransactionCheckById(long pid, List<String> serviceNames) {
        return transactionCheckRepository.queryTransactionCheckById(pid, serviceNames);
    }

    public List<TransactionCheck> queryTransactionCheckByIds(List<Long> pids) {
        return transactionCheckRepository.queryTransactionCheckByIds(pids);
    }

    public void update(TransactionCheck transactionCheck) {
        transactionCheckRepository.update(transactionCheck);
    }

    public void deleteAll() {
        transactionCheckRepository.deleteAll();
    }

    public List<TwoTuple<Integer, Long>> messageStateCountMap(List<String> serviceNames) {
        return transactionCheckRepository.messageStateCountMap(serviceNames);
    }

    public List<TwoTuple<String, Long>> dlqList(List<String> serviceNames) {
        return  transactionCheckRepository.dlqList(serviceNames);
    }

    public void delete(List<Long> pids, List<String> serviceNames) {
        transactionCheckRepository.delete(pids, serviceNames);
    }
}
