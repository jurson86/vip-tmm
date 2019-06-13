package com.tuandai.tran.config;

import com.tuandai.tran.repository.TransactionCheckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class InitailDBTables {

	@Autowired
	private TransactionCheckRepository transactionCheckRepository;


	public void createTables() {
		transactionCheckRepository.createIfNotExistsTable();
	}
}
