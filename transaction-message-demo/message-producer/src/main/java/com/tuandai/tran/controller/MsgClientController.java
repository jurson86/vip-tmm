package com.tuandai.tran.controller;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Throwables;
import com.tuandai.tran.domain.TransactionCheck;
import com.tuandai.transaction.client.bo.SendState;
import com.tuandai.transaction.client.model.*;
import com.tuandai.transaction.client.service.inf.TMMService;
import com.tuandai.transaction.producer.annotation.TmmAutoConfig;
import com.tuandai.transaction.producer.model.ExchangeType;
import com.tuandai.transaction.producer.model.RabbitMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;


import com.tuandai.tran.service.inf.TransactionCheckService;
import org.springframework.web.bind.annotation.*;

import static com.tuandai.transaction.client.utils.ConstantUtils.SPAN_ID_NAME;
import static com.tuandai.transaction.client.utils.ConstantUtils.TRACE_ID_NAME;

@RestController
@RequestMapping(value = "/msg")
public class MsgClientController {

	private static final Logger logger = LoggerFactory.getLogger(MsgClientController.class);

	@Autowired
	private TransactionCheckService transactionCheckService;

	@Autowired
	private TMMService tMMService;

	private static Random random = new Random();
	private static Random random2 = new Random();
	
	/**
	 *  自定义TOPIC发送- 事务消息
	 *
	 * @param hello
	 * @return
	 */
	@RequestMapping(value = "/producer", method = RequestMethod.GET)
	public String Producer(@RequestParam String hello) throws IOException {

        logger.info("接受到请求,hello:{}", hello);
        // 可自定义发送消息id
        String msgId = UUID.randomUUID().toString();
        try
        {

            // 记录发送的TOPIC,其中ip为配置文件里对应的，rabbitmq服务，如上配置的： default 、 first
            RabbitMQTopic rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
                    .vHost("TMM")
                    .exchange("mychange")
                    .exchangeType(ExchangeType.FANOUT.des())
                    .ip("default").build();

            // 打印开始日志,其中check默认为配置里面的check地址，也可在此处变更；
            BeginLog beginLog = BeginLog.newBeginLogBuilder()
                    .check("/msg/tmm/check")
                    .serviceName("transaction-producer")
                    .topic(rabbitMQTopic.toJSONString())
                    .uid(msgId).build();
            tMMService.sendTransBeginToFlume(beginLog);

            // TODO 业务执行代码
//            throw new  Exception();

            // 打印结束日志
            EndLog endLog = EndLog.newEndLogBuilder()
                    .state(SendState.COMMIT)
                    .message("hello world")
                    .uid(msgId).build();
            tMMService.sendTransEndToFlume(endLog);
        }
        catch (Exception e){

            // 打印结束日志
            EndLog endLog = EndLog.newEndLogBuilder()
                    .state(SendState.CANCEL)
                    .uid(msgId).build();
            tMMService.sendTransEndToFlume(endLog);

            // TODO 业务异常
        }

		return hello;
	}


    /**
     *  自定义TOPIC发送- 事务消息
     *
     * @param hello
     * @return
     */
    @RequestMapping(value = "/producer1", method = RequestMethod.GET)
    @TmmAutoConfig(isAuto = true)
    public String Producer1(@RequestParam String hello) throws IOException {

        logger.info("接受到请求,hello:{}", hello);
        try
        {

            // 打印开始日志,其中check默认为配置里面的check地址，也可在此处变更；
            RabbitMQTopic rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
                    .vHost("TMM")
                    .exchange("mychange")
                    .exchangeType(ExchangeType.FANOUT.des())
                    .ip("first").build();

            // 打印开始日志,其中check默认为配置里面的check地址，也可在此处变更；
            BeginLog beginLog = BeginLog.newBeginLogBuilder()
                    .check("/msg/tmm/check")
                    .topic(rabbitMQTopic.toJSONString()).build();
            tMMService.sendTransBeginToFlume(beginLog);

            // TODO 业务执行代码

            // 打印结束日志
            EndLog endLog = EndLog.newEndLogBuilder()
                    .state(SendState.COMMIT)
                    .message("hello world").build();
            tMMService.sendTransEndToFlume(endLog);
        }
        catch (Exception e){

            // 打印结束日志
            EndLog endLog = EndLog.newEndLogBuilder()
                    .state(SendState.CANCEL).build();
            tMMService.sendTransEndToFlume(endLog);

            // TODO 业务异常
        }

        return hello;
    }


    /**
	 * 同域名, 不同端口发送消息测试
	 */
	@RequestMapping(value = "/producer/port", method = RequestMethod.GET)
	public String producerPort(@RequestParam String msg) throws IOException {

		String msgId = UUID.randomUUID().toString();

		// 记录发送的消息
		RabbitMQTopic rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
				.vHost("TMM")
				.exchange("port.test.exchange")
				.exchangeType(ExchangeType.FANOUT.des())
				.ip( msg.equals("5672") ? "third" : "fourth").build();

		// 打印开始日志
		BeginLog beginLog = BeginLog.newBeginLogBuilder()
				.check("/msg/tmm/check")
				.serviceName("transaction-producer")
				.topic(rabbitMQTopic.toJSONString())
				.uid(msgId).build();
		tMMService.sendTransBeginToFlume(beginLog);

		// 打印结束日志
		TransactionCheck transactionCheck = new TransactionCheck(msgId, msg);
		transactionCheckService.insertTransactionCheck(transactionCheck);

		EndLog endLog = EndLog.newEndLogBuilder()
				.state(SendState.COMMIT)
				.message("hello world")
				.serviceName("transaction-producer")
				.uid(msgId).build();
		tMMService.sendTransEndToFlume(endLog);
		return msg;
	}

	/**
	 * 同域名，同端口，不同用户测试
	 */
	@RequestMapping(value = "/producer/user", method = RequestMethod.GET)
	public String producerUser(@RequestParam String msg, @RequestParam boolean diff) throws IOException {

		String msgId = UUID.randomUUID().toString();

		RabbitMQTopic rabbitMQTopic = null;
		if (diff) { // 不同则发送不懂的交换机
			if (msg.equals("user")) {
				// 如果是user用户，则发送消息到TMMUser里面
				rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
						.vHost("TMMUser") // user用户独有
						.exchange("user.test.exchange")
						.exchangeType(ExchangeType.FANOUT.des())
						.ip("sixth").build();
			} else {
				// 如果是admin用户则发送消息到TMMAdmin里面
				rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
						.vHost("TMMUser2") // admin 用户独有
						.exchange("admin.test.exchange")
						.exchangeType(ExchangeType.FANOUT.des())
						.ip("sixth2").build();
			}
		} else { // 相同则发送到同一个交换机
			if (msg.equals("user")) {
				// 如果是user用户，则发送消息到TMMUser里面
				rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
						.vHost("TMM")
						.exchange("user.diff.exchange")
						.exchangeType(ExchangeType.FANOUT.des())
						.ip("sixth").build();
			} else {
				// 如果是admin用户则发送消息到TMMAdmin里面
				rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
						.vHost("TMM")
						.exchange("user.diff.exchange")
						.exchangeType(ExchangeType.FANOUT.des())
						.ip("sixth2").build();
			}
		}

		// 打印开始日志
		BeginLog beginLog = BeginLog.newBeginLogBuilder()
				.check("/msg/tmm/check")
				.serviceName("transaction-producer")
				.topic(rabbitMQTopic.toJSONString())
				.uid(msgId).build();
		tMMService.sendTransBeginToFlume(beginLog);

		// 打印结束日志
		TransactionCheck transactionCheck = new TransactionCheck(msgId, msg);
		transactionCheckService.insertTransactionCheck(transactionCheck);

		EndLog endLog = EndLog.newEndLogBuilder()
				.state(SendState.COMMIT)
				.message("hello world")
				.serviceName("transaction-producer")
				.uid(msgId).build();
		tMMService.sendTransEndToFlume(endLog);
		return msg;
	}

	@RequestMapping(value = "/tmm/check", method = RequestMethod.POST)
	public String  check(@RequestBody String body) {
		String uid = JSON.parseObject(body).getString("uid");


		RabbitMQTopic rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
				.exchange("mychange").isCustomExchange(true).vHost("myVhost")
				.exchangeType(ExchangeType.FANOUT.des()).build();

		EndLog endLog = new EndLog();
		endLog.setState(SendState.COMMIT);
		endLog.setMessage("重发！check");
		endLog.setServiceName("transaction-producer");
		endLog.setUid(uid);
		endLog.setTopic(rabbitMQTopic.toJSONString());
		String result = JSONObject.toJSONString(endLog);
		logger.info("回调返回参数：" + result);
		return result;
	}

	@RequestMapping(value = "/producer/no/tran1", method = RequestMethod.GET)
	public String noProducer1(@RequestParam String msg) throws IOException {

		String msgId = UUID.randomUUID().toString();

		// 记录发送的消息
		RabbitMQTopic rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder()
				.vHost("TMM")
				.exchange("mychange2")
				.exchangeType(ExchangeType.FANOUT.des())
				.ip( msg.equals("160") ? "first" : "default").build();
		MqLog mqLog = new MqLog();
		mqLog.setMessage("hello no trans");
		mqLog.setServiceName("transaction-producer");
		mqLog.setTopic(rabbitMQTopic.toJSONString());
		mqLog.setUid(msgId);

		TransactionCheck transactionCheck = new TransactionCheck(msgId, msg);
		transactionCheckService.insertTransactionCheck(transactionCheck);
		tMMService.sendNTrans(mqLog);
		return msg;
	}


    @RequestMapping(value = "/producer/no/tran2", method = RequestMethod.GET)
    @TmmAutoConfig(isAuto = true)
    public String noProducer2(@RequestParam String msg) throws IOException {

        MqLog mqLog = new MqLog();
        mqLog.setMessage("hello no trans");

        // TODO 业务代码

        tMMService.sendNTrans(mqLog);
        return msg;
    }


//	@RequestMapping(value = "/producer/rocket", method = RequestMethod.GET)
//	public String ProducerRocket(@RequestParam String msg) throws IOException {
//		logger.info("接受到请求,msg:{}", msg);
//		String msgId = UUID.randomUUID().toString();
//
//		RocketMqTopic rockerMqTopic = new RocketMqTopic();
//		rockerMqTopic.setTag(msgId);
//		rockerMqTopic.setTopic("fengpengyongTopic");
//
//		BeginLog beginLog = BeginLog.newBeginLogBuilder()
//				.check("/msg/tmm/check")
//				.serviceName("transaction-producer")
//				.topic(JSONObject.toJSONString(rockerMqTopic))
//				.uid("asdasdsad").build();
//		tMMService.sendTransBeginToFlume(beginLog);
//
//		{
//
//			logger.info("正在处理请求.....", msg);
//
//		}
//
//		EndLog endLog = EndLog.newEndLogBuilder()
//				.state(SendState.COMMIT)
//				.message("hello world")
//				.serviceName("transaction-producer")
//				.uid("asdasdsad").build();
//		tMMService.sendTransEndToFlume(endLog);
//
//		return msgId;
//	}

}