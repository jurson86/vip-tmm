package com.tuandai.transaction.rabbitmq;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer2 {

    @RabbitListener(queues = "hellou",    containerFactory = "uRabbitListenerContainerFactory")
    public void process2(Message message, Channel channel) {
        byte[] body = message.getBody();
        String msg = new String(body);

        //TODO 业务内容
        //logger.info("payload {}", msg);
//		String uid = JSONObject.parseObject(msg).getString("uid");
//		transactionCheckDao.deleteTransactionCheck(uid);
//		Matcher sad = p.matcher(uid);
////		if (sad.find()) {
        //throw new RuntimeException("insufficient  message ");
////		}

    }

}
