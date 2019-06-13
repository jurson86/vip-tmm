package com.tuandai.transaction.mq.inf;

import com.tuandai.transaction.domain.TransactionCheck;

/**
 * Mq相关的服务
 */
public interface MqService {

    void sendMessage(TransactionCheck transactionCheck) throws IllegalAccessException;

}
