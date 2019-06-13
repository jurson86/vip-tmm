package com.tuandai.transaction.mq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.tuandai.transaction.bo.RabbitAddressKey;
import com.tuandai.transaction.bo.StartInfo;
import com.tuandai.transaction.dao.ApplicationDao;
import com.tuandai.transaction.dao.RegistryAgentDao;
import com.tuandai.transaction.domain.Application;
import com.tuandai.transaction.domain.RegistryAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class RabbitMqStartConsumer implements MqConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqCheckConsumer.class);

    @Autowired
    private RegistryAgentDao registryAgentDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Override
    public Queue getQueue() {
        return new Queue("tmm-start-queue");
    }

    @Override
    public void doDeclare(RabbitAdmin admin, RabbitAddressKey rabbitAddressKey) {
        Queue queue = getQueue();
        admin.declareQueue(queue);
        admin.declareBinding(BindingBuilder.bind(queue).to(new FanoutExchange("start_exchange")));
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            String mqMessage = new String(message.getBody());
            logger.info("收到start消息队列数据：{}", mqMessage);
            StartInfo startInfo  = JSONObject.parseObject(mqMessage, StartInfo.class);
            if (startInfo == null) {
                return;
            }
            List<RegistryAgent> list = new ArrayList<>();
            RegistryAgent registryAgent = new RegistryAgent();
            Date time = new Date(System.currentTimeMillis());
            registryAgent.setCreateTime(time);
            registryAgent.setUpdateTime(time);
            registryAgent.setServiceName(startInfo.getServerName());
            registryAgent.setPrefixUrl(startInfo.getPrefixUrl());
            list.add(registryAgent);
            RegistryAgent tmp = registryAgentDao.queryRegistryAgentByServerName(startInfo.getServerName());

            if (tmp == null) {
                registryAgentDao.addRegistryAgent(list);
            } else {
                RegistryAgent updateRecord = new RegistryAgent();
                updateRecord.setCreateTime(tmp.getCreateTime());
                updateRecord.setUpdateTime(new Date());
                updateRecord.setServiceName(tmp.getServiceName());
                updateRecord.setPrefixUrl(startInfo.getPrefixUrl());
                updateRecord.setPid(tmp.getPid());
                registryAgentDao.updateByPrimaryKey(updateRecord);
            }

            List<Application> applications = new ArrayList<>();
            Application application = new Application();
            application.setCreateTime(time);
            application.setUpdateTime(time);
            application.setApplicationName(startInfo.getServerName());
            applications.add(application);
            applicationDao.insertBatch(applications);
            logger.info("start数据处理完毕 {}， {}", mqMessage, tmp);
        } catch (Exception e) {
            logger.error("消息消费异常，为了防止出现阻塞rabbitmq，做丢弃处理", e);
        }
    }
}
