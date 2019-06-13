package com.tuandai.tran.service;

import com.tuandai.tran.repository.TransactionCheckRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InitTable implements InitializingBean {

    @Autowired
    private TransactionCheckRepository transactionCheckRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        transactionCheckRepository.createIfNotExistsTable();
    }
}
