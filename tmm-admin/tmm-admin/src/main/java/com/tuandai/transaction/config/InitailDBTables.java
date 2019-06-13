package com.tuandai.transaction.config;

import com.tuandai.transaction.dao.*;
import com.tuandai.transaction.mybatis.ScriptRunnerExecSql;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.tuandai.transaction.repository.TransactionCheckRepository;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;


@Component
public class InitailDBTables implements InitializingBean {

	@Autowired
	private TransactionCheckDao transactionCheckDao;

	@Autowired
	private MonitorAgentDao monitorAgentDao;

	@Autowired
	private RegistryAgentDao registryAgentDao;

	@Autowired
	private DlqServerDao dlqServerDao;

	@Autowired
	private ScriptRunnerExecSql scriptRunnerExecSql;

	@Autowired
	private MqClusterDao mqClusterDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		transactionCheckDao.createIfNotExistsTable();
		monitorAgentDao.createMonitorAgentTable();
		registryAgentDao.createRegistryAgentTable();
		dlqServerDao.createDlqServerTable();
		scriptRunnerExecSql.runnerSql();
		mqClusterDao.createMqClusterTable();
	}
}
