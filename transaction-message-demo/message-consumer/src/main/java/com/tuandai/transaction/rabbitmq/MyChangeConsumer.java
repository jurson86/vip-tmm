package com.tuandai.transaction.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.tuandai.transaction.dao.TransactionCheckDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MyChangeConsumer {

	private static final Logger logger = LoggerFactory.getLogger(MyChangeConsumer.class);

	@Autowired
	private TransactionCheckDao transactionCheckDao;

	private static Pattern p = Pattern.compile("33$");


	@RabbitListener(queues = "${tmm.queue}",  containerFactory = "bRabbitListenerContainerFactory")
	public void process(Message message, Channel channel) {
		byte[] body = message.getBody();
		String msg = new String(body);

		//TODO 业务内容
		logger.info("payload {}", msg);
//		String uid = JSONObject.parseObject(msg).getString("uid");
//		transactionCheckDao.deleteTransactionCheck(uid);
//		Matcher sad = p.matcher(uid);
////		if (sad.find()) {
			//throw new RuntimeException("insufficient  message ");
////		}

	}



}