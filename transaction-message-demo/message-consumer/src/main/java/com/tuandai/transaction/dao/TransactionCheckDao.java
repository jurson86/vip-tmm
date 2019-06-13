package com.tuandai.transaction.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tuandai.transaction.mapper.TransactionCheckMapper;

@RestController
public class TransactionCheckDao {

    @Autowired
    private TransactionCheckMapper transactionCheckMapper;

    @RequestMapping(value = "/transactioncheck", method = RequestMethod.GET)
    @Transactional
    public void deleteTransactionCheck(@RequestParam("msgId") String msgId) {
        transactionCheckMapper.deleteTransactionCheck(msgId);
    }

}
