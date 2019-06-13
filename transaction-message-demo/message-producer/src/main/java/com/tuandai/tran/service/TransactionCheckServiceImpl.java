package com.tuandai.tran.service;

import com.tuandai.tran.domain.TransactionCheck;
import com.tuandai.tran.domain.filter.TransactionCheckFilter;
import com.tuandai.tran.repository.TransactionCheckRepository;
import com.tuandai.tran.service.inf.TransactionCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionCheckServiceImpl implements TransactionCheckService {

    @Autowired
    private TransactionCheckRepository transactionCheckRepository;

    @Override
    public List<String> messageNotAcceptCheck(List<String> msgIds) {
        TransactionCheckFilter filter = new TransactionCheckFilter();
        filter.setMsgIds(msgIds);
        filter.setMaxAcceptCount(1);
        List<TransactionCheck> list = transactionCheckRepository.queryTransactionCheckByFilter(filter);
        List<String> ids = list.stream().map(o -> o.getMsgId()).collect(Collectors.toList());
        return ids;
    }

    @Override
    public List<String> messageRepeatAcceptCheck(List<String> msgIds) {
        TransactionCheckFilter filter = new TransactionCheckFilter();
        filter.setMsgIds(msgIds);
        filter.setMinAcceptCount(0);
        List<TransactionCheck> list = transactionCheckRepository.queryTransactionCheckByFilter(filter);
        List<String> ids = list.stream().map(o -> o.getMsgId()).collect(Collectors.toList());
        return ids;
    }

    @Override
    public void insertTransactionCheck(TransactionCheck transactionCheck) {
        transactionCheckRepository.insert(transactionCheck);
    }

    @Override
    public void deleteTransactionCheck(String msgId) {
        transactionCheckRepository.delete(msgId);
    }

	@Override
	public TransactionCheck queryTransactionCheckById(String msgId) {
        return transactionCheckRepository.queryByMessageId(msgId);
	}

}
