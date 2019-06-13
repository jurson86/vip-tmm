package com.tuandai.transaction.client.mq.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.model.RabbitMQTopic;
import com.tuandai.transaction.client.bo.ExchangeType;
import com.tuandai.transaction.client.mq.inf.MqService;
import com.tuandai.transaction.client.utils.ConstantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.util.StringUtils;


public class RabbitMqServiceImpl implements MqService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqServiceImpl.class);

    private static Exchange getExchangeByType(String type, String exchangeName) throws Exception {
        if (StringUtils.isEmpty(type)) {
            throw new Exception();
        }
        ExchangeType exchangeType = ExchangeType.findByDes(type);
        if (exchangeType == null) {
            logger.error("not ExchangeType.....");
            throw new Exception();
        }
        
        if (exchangeType == ExchangeType.FANOUT) {
            return new FanoutExchange(exchangeName);
        } else if (exchangeType == ExchangeType.HEADRES) {
            return new HeadersExchange(exchangeName);
        } else if (exchangeType == ExchangeType.DIRECT) {
            return new DirectExchange(exchangeName);
        } else if (exchangeType == ExchangeType.TOPIC) {
            return new TopicExchange(exchangeName);
        }
        return null;
    }

    private boolean checkTopic(RabbitMQTopic joTopic) {
        if(StringUtils.isEmpty(joTopic.getvHost()) || StringUtils.isEmpty(joTopic.getExchange()) || StringUtils.isEmpty(joTopic.getExchangeType())) {
            logger.warn("invalid mq topic : {}", joTopic);
            return false;
        }
        return true;
    }

    @Override
    public void sendMessage(EventDefinition eventDefinition) throws Exception {

        RabbitMQTopic joTopic = JSONObject.parseObject(eventDefinition.getTopic(), RabbitMQTopic.class);

        if (!checkTopic(joTopic)) {
            return;
        }

        long time2 = System.currentTimeMillis();
        String msgStr = eventDefinition.getMessage();
        String ip = joTopic.getIp();
        String exchangeName = joTopic.getExchange();
        String vHost = joTopic.getvHost();
        String exchangeType = joTopic.getExchangeType();
        String routeKey = joTopic.getRouteKey();
        boolean isCustomExchange = joTopic.isCustomExchange();

        long time3 = System.currentTimeMillis();
        RabbitTemplate rabbitTemplate = RabbitTemplateFactory.getRabbitTemplate(vHost, ip);
        long now3 = System.currentTimeMillis();
        logger.debug("RabbitTemplateFactory.getRabbitTemplate ,uid:" + eventDefinition.getUid() +",花费时间：" + (now3-time3));

        if (isDeclare(isCustomExchange, vHost)) {
            long time = System.currentTimeMillis();
            // 非自定义
            RabbitAdmin rabbitAdmin = RabbitTemplateFactory.getRabbitAdmin(vHost, ip);
            // 根据不同类型生成交换机
            Exchange exchange = getExchangeByType(exchangeType, exchangeName);
            rabbitAdmin.declareExchange(exchange);
            // 如果是TMM的系统消息则生成队列自动保存消息，以防止消息丢失
            if (vHost.equals(ConstantUtils.TMM_VHOST)) {
                Queue queue = null;
                if (exchangeName.equals(ConstantUtils.START_EXCHANGE)) {
                    queue = QueueBuilder.durable(ConstantUtils.START_QUEUE).build();
                } else if (exchangeName.equals(ConstantUtils.CHECK_EXCHANGE)) {
                    queue = QueueBuilder.durable(ConstantUtils.CHECK_QUEUE).build();
                }
                if (queue != null) {
                    // 强制转换为fanout类型
                    Binding bind = null;
                    if (exchange instanceof FanoutExchange) {
                        bind = BindingBuilder.bind(queue).to((FanoutExchange) exchange);
                    } else if (exchange instanceof TopicExchange) {
                        bind = BindingBuilder.bind(queue).to((TopicExchange) exchange).with(routeKey);
                    } else if (exchange instanceof DirectExchange) {
                        bind = BindingBuilder.bind(queue).to((DirectExchange) exchange).with(routeKey);
                    } else {
                        throw new IllegalArgumentException("暂不支持此项类型");
                    }
                    if (bind != null) {
                        rabbitAdmin.declareQueue(queue);
                        rabbitAdmin.declareBinding(bind);
                    }
                }
            }
            long now = System.currentTimeMillis();
            logger.debug("rabbitAdmin.declareExchange ,uid:" + eventDefinition.getUid() +",花费时间：" + (now-time));
        }

        long time = System.currentTimeMillis();
        CorrelationData correlationData = new CorrelationData(eventDefinition.getUid());
        rabbitTemplate.convertAndSend(exchangeName, routeKey, msgStr, correlationData);
        long now = System.currentTimeMillis();
        logger.info("rabbitTemplate.convertAndSend ，uid:" +   eventDefinition.getUid() + "花费时间：" + (now-time));
        long now2 = System.currentTimeMillis();
        logger.debug("rabbitTemplate.sendMessage, uid:" + eventDefinition.getUid() +" 花费时间：" + (now2-time2));
    }

    private boolean isDeclare(boolean isCustomExchange, String vHost) {
        return !isCustomExchange || vHost.equals(ConstantUtils.TMM_VHOST);
    }

}
