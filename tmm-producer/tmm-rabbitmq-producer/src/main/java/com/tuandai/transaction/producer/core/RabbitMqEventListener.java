package com.tuandai.transaction.producer.core;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.bo.StartInfo;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.service.TMMEvent;
import com.tuandai.transaction.client.service.TMMServiceImpl;
import com.tuandai.transaction.client.service.inf.TMMEventListener;
import com.tuandai.transaction.producer.bo.RabbitAddress;
import com.tuandai.transaction.producer.config.RabbitConfig;
import com.tuandai.transaction.producer.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.util.CollectionUtils;

import java.util.Map;

public class RabbitMqEventListener implements TMMEventListener<TMMEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqEventListener.class);

    @Override
    public void onApplicationEvent(TMMEvent event) {
        sendAgentStartInfo(event);
    }

    private void sendAgentStartInfo(TMMEvent event) {
        // 保留 对象
        ConfirmCallback.tmmService = (TMMServiceImpl)event.getSource();

        RabbitConfig tmmConfig = (RabbitConfig)SettingSupport.getTmmConfig();
        Map<String, RabbitAddress> map = tmmConfig.getRabbitAddressMap();
        if (!CollectionUtils.isEmpty(map)) {
            for (Map.Entry<String, RabbitAddress> tmp : map.entrySet()) {
                try {
                    String ipName = tmp.getKey();

                    // check 队列
                    RabbitTemplate rabbitTemplate = RabbitTemplateFactory.getRabbitTemplate(Constants.TMM_VHOST, ipName);

                    // 先申明队列和交换机
                    RabbitAdmin rabbitAdmin = RabbitTemplateFactory.getRabbitAdmin(Constants.TMM_VHOST, ipName);
                    // start 交换机
                    rabbitAdmin.declareExchange(new FanoutExchange(Constants.START_EXCHANGE));
                    // start 队列
                    rabbitAdmin.declareQueue(QueueBuilder.durable(Constants.START_QUEUE).build());
                    // check交换机
                    rabbitAdmin.declareExchange(new FanoutExchange(Constants.CHECK_EXCHANGE));

                    // 构造消息内容
                    String messageStr = JSONObject.toJSONString(new StartInfo(SettingSupport.getServerName(),
                            SettingSupport.getPrefixUrl()));

                    // 发送启动消息
                    rabbitTemplate.send(Constants.START_EXCHANGE, "", new Message(messageStr.getBytes(),
                            new MessageProperties()), new CorrelationData("tmm_agent_start_uid"));

                } catch (Exception e) {
                    logger.error("tmm_send_begin_mq_fail , 集群信息:{}, {}", tmp, e);
                    Object object = event.getSource();
                    if (object instanceof TMMServiceImpl) {
                        TMMServiceImpl tmmService = (TMMServiceImpl)object;
                        tmmService.shutdown();
                    }
                }
            }
        }
    }
}
