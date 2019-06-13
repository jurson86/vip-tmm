package com.tuandai.transaction.mq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.tuandai.transaction.bo.*;
import com.tuandai.transaction.dao.TransactionCheckDao;
import com.tuandai.transaction.domain.TransactionCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * check 数据消费者
 * check交换机信息
 * {'exchange':'tmm-check','exchangeType':'fanout','vHost':'tmmVhost'}
 */
@Component
public class RabbitMqCheckConsumer implements MqConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqCheckConsumer.class);

    @Autowired
    private TransactionCheckDao transactionCheckDao;

    private RabbitAddressKey rabbitAddressKey;

    private TransactionCheck eventDefinition2TransactionCheck(EventDefinition eventDefinition, RabbitAddressKey rabbitAddressKey) {
        Date date = new Date();
        TransactionCheck transactionState = new TransactionCheck();
        transactionState.setuId(eventDefinition.getUid());
        transactionState.setServiceName(eventDefinition.getServiceName());
        transactionState.setCreateTime(date);
        transactionState.setUpdateTime(date);
        transactionState.setMessageState(MessageState.PRESEND.code()); // 预发送状态
        transactionState.setMessageSendThreshold(Thresholds.MAX_SEND.code()); // 最大重试次数
        transactionState.setMessageSendTimes(0);
        transactionState.setMessageNextSendTime(new Date());
        transactionState.setMessageNextSendTime(date);
        transactionState.setPresendBackNextSendTime(new Date());
        transactionState.setPresendBackUrl(eventDefinition.getCheckUrl());
        transactionState.setPresendBackMethod(HttpMethod.POST.toString());
        transactionState.setPresendBackThreshold(Thresholds.MAX_PRESEND.code());
        transactionState.setPresendBackSendTimes(0);
        transactionState.setMessage(eventDefinition.getMessage());
        transactionState.setMessageTopic(eventDefinition.getTopic());
        transactionState.setMqType(MqType.RABBIT.value());
        RabbitAddress rabbitAddress = rabbitAddressKey.getRabbitAddress();
        transactionState.setClusterIp(JSONObject.toJSONString(rabbitAddress.toClusterIp()));
        transactionState.setDlqName(null);
        transactionState.setMqType(MqType.RABBIT.value());
        return transactionState;
    }


    @Override
    public Queue getQueue() {
        return new Queue("tmm-check-queue");
    }

    @Override
    public void doDeclare(RabbitAdmin admin, RabbitAddressKey rabbitAddressKey) {
        this.rabbitAddressKey = rabbitAddressKey;
        Queue queue = getQueue();
        admin.declareQueue(queue);
        admin.declareBinding(BindingBuilder.bind(queue).to(new FanoutExchange("tmm_check")));
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            String mqMessage = new String(message.getBody());
            logger.info("收到check消息队列数据：{}", mqMessage);
            String checkMsgs[] = mqMessage.split("\n");
            List<TransactionCheck> transactionStates = new ArrayList<>();
            for (String s : checkMsgs) {
                EventDefinition msg = JSONObject.parseObject(s, EventDefinition.class);
                if (msg == null) {
                    continue;
                }
                transactionStates.add(eventDefinition2TransactionCheck(msg, rabbitAddressKey));
            }
            if (!CollectionUtils.isEmpty(transactionStates)) {
                // 入库
                transactionCheckDao.insertBatch(transactionStates);
            }
            logger.info("check数据处理完毕, message{}, 集群{}", mqMessage, channel.getConnection().getAddress().getHostAddress());
        } catch (Exception e) {
            logger.error("消息消费异常，为了防止出现阻塞rabbitmq，做丢弃处理", e);
        }

    }

}
