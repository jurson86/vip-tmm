package com.tuandai.tran.service.inf;

import com.tuandai.tran.domain.TransactionCheck;

import java.util.List;

public interface TransactionCheckService {

	List<String> messageNotAcceptCheck(List<String> transactionIds);

	List<String> messageRepeatAcceptCheck(List<String> transactionIds);

    void insertTransactionCheck(TransactionCheck transactionCheck);

    void deleteTransactionCheck(String msgId);

    TransactionCheck queryTransactionCheckById(String  msgId);
}
