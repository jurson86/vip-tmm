package com.tuandai.tran.controller;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tuandai.tran.config.RabbitmqConfig;
import com.tuandai.tran.domain.TransactionCheck;
import com.tuandai.tran.service.inf.TransactionCheckService;
import com.tuandai.transaction.client.bo.SendState;
import com.tuandai.transaction.client.model.BeginLog;
import com.tuandai.transaction.client.model.EndLog;
import com.tuandai.transaction.client.model.MqLog;
import com.tuandai.transaction.client.service.inf.TMMService;
import com.tuandai.transaction.producer.model.ExchangeType;
import com.tuandai.transaction.producer.model.RabbitMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api")
@EnableConfigurationProperties({RabbitmqConfig.class})
public class ApiController {

	private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

	private Map<String, String> rabbitmq = new HashMap<>();

	@Autowired
	private TransactionCheckService transactionCheckService;

	@Autowired
	private TMMService tMMService;

	@Autowired
	private RabbitmqConfig rabbitmqConfig;

	@RequestMapping(value = "/mq/cluster/list", method = RequestMethod.GET)
	public String mqClusterList() throws IOException {
		Map<String, String> mqMap = rabbitmqConfig.getRabbitmq();
		Map<String, Map<String, String>> result = new HashMap<>();
		for (Map.Entry<String, String> entry : mqMap.entrySet()) {
			String value = entry.getValue();
			String key = entry.getKey();
			String[] str = key.split("\\.");
			if (str[0].equals("tmm")) {
				continue;
			}
			Map<String, String> tmpMap = null;
			if (result.containsKey(str[0])) {
				tmpMap = result.get(str[0]);
			} else {
				tmpMap = new HashMap<>();
			}
			tmpMap.put(str[1], value);
			result.put(str[0], tmpMap);
		}
		return JSONObject.toJSONString(result);
	}


	@RequestMapping(value = "/producer/no/trans", method = RequestMethod.GET)
	public String noProducer(@RequestParam String ipName, @RequestParam String uid, @RequestParam String exchange,
							 @RequestParam String exchangeType) throws IOException {

		//String msgId = UUID.randomUUID().toString();

		// 记录发送的消息
		RabbitMQTopic rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
				.vHost("TMM")
				.exchange(exchange)
				.exchangeType(ExchangeType.findByDes(exchangeType).des())
				.ip(ipName).build();
		MqLog mqLog = new MqLog();
		mqLog.setMessage("hello no trans");
		mqLog.setServiceName("transaction-producer");
		mqLog.setTopic(rabbitMQTopic.toJSONString());
		mqLog.setUid(uid);

		TransactionCheck transactionCheck = new TransactionCheck(uid, ipName);
		transactionCheckService.insertTransactionCheck(transactionCheck);
		tMMService.sendNTrans(mqLog);
		return uid + ":" + ipName;
	}

	// localhost:8081/api/producer/trans?ipName=wqe&uid=213&state=2
	@RequestMapping(value = "/producer/trans", method = RequestMethod.GET)
	public String producer(@RequestParam String ipName, @RequestParam String uid,
						   @RequestParam(defaultValue = "0") Integer state, @RequestParam String exchange,
						   @RequestParam String exchangeType) throws IOException {
		// state 0 提交， 1 取消,  2模拟异常

		// 记录发送的消息
		RabbitMQTopic rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
				.vHost("TMM")
				.exchange(exchange)
				.exchangeType(ExchangeType.findByDes(exchangeType).des())
				.ip(ipName).build();

		// 打印开始日志
		BeginLog beginLog = BeginLog.newBeginLogBuilder()
				.check("/test/api/check")
				.serviceName("transaction-producer")
				.topic(rabbitMQTopic.toJSONString())
				.uid(uid).build();
		tMMService.sendTransBeginToFlume(beginLog);

		// 打印结束日志
		TransactionCheck transactionCheck = new TransactionCheck(uid, ipName);
		transactionCheckService.insertTransactionCheck(transactionCheck);

		if (state == 0 || state == 1) {
			EndLog endLog = EndLog.newEndLogBuilder()
					.state(SendState.findByValue(state))
					.message("hello world")
					.serviceName("transaction-producer")
					.uid(uid).build();
			tMMService.sendTransEndToFlume(endLog);
		}
		return uid + ":" + ipName;
	}

	@RequestMapping(value = "/check", method = RequestMethod.POST)
	public String  check(@RequestBody String body) {
		String uid = JSON.parseObject(body).getString("uid");
		EndLog endLog = null;
		if (uid.equals("00000000")) {
			return "fail";
		} else {
			endLog = EndLog.newEndLogBuilder()
					.state(SendState.COMMIT)
					.message("check 重发")
					.serviceName("transaction-producer")
					.uid(uid).build();
		}
		String result = JSONObject.toJSONString(endLog);
		logger.info("回调返回参数：" + result);
		return result;
	}

		
}