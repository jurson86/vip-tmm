package com.tuandai.transaction.service;

import java.util.Random;

import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qianmi.ms.starter.rocketmq.annotation.RocketMQMessageListener;
import com.qianmi.ms.starter.rocketmq.core.RocketMQListener;
import com.tuandai.transaction.dao.TransactionCheckDao;

@Component
@RocketMQMessageListener(topic = "ProducerTestTopic", consumerGroup = "transaction-group")
public class TransactionRocketMQConsumer implements RocketMQListener<MessageExt>{
	private static final Logger logger = LoggerFactory.getLogger(TransactionRocketMQConsumer.class);
	
	private Random random = new Random();

	@Autowired
	private TransactionCheckDao transactionCheckDao;

    public void onMessage(MessageExt messageExt) {
    	int value = random.nextInt(300);
		String msgId = messageExt.getKeys();
		logger.info("消费消息： " + msgId);
		transactionCheckDao.deleteTransactionCheck(msgId);

		//模拟消费异常
        if ((value % 11) == 0) {
        	logger.info("TransactionRocketMQConsumer RuntimeException! ============= \n {}" , messageExt );
            throw new RuntimeException("Could not find db");
        }
        		    	
    }

}