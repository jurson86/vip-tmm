package com.tuandai.transaction.client.mq;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.bo.RabbitAddress;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.config.TMMRabbitProperties;
import com.tuandai.transaction.client.model.RabbitMQTopic;
import com.tuandai.transaction.client.mq.rabbitmq.RabbitTemplateFactory;
import com.tuandai.transaction.client.service.App;
import com.tuandai.transaction.client.service.TMMServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = App.class, properties ={"classpath:application.properties"} )
public class RabbitTemplateFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger(RabbitTemplateFactoryTest.class);

    @Before
    public void initMq() throws Exception {
        TMMServiceImpl tmm = SettingSupport.context.getBean(TMMServiceImpl.class);
        tmm.shutdown();
    }

    @Test
    public void rabbitTemplateFactoryTest() throws IllegalAccessException {
        RabbitTemplate template = RabbitTemplateFactory.getRabbitTemplate("myVhost", "first");
        assertNotNull(template);
        RabbitTemplate updateRabbit = RabbitTemplateFactory.updateRabbitTemplate("myVhost", "first");
        assertNotNull(updateRabbit);
    }

    @Test
    public void rabbitMqHelperTest() throws Exception {

        // fanout
        RabbitMQTopic rabbitMQTopic = new RabbitMQTopic();
        rabbitMQTopic.setvHost("myVhost");
        rabbitMQTopic.setExchange("test");
        rabbitMQTopic.setExchangeType("fanout");
        rabbitMQTopic.setIp("first");
        EventDefinition eventDefinition  = new EventDefinition();
        eventDefinition.setTopic(JSONObject.toJSONString(rabbitMQTopic));
        eventDefinition.setMessage("test tmm");
        logger.info("开始");
        MqSender sender = new MqSender(new DefaultMqServiceFactory());
        sender.sendMq(eventDefinition);
        logger.info("结束");

        RabbitTemplate rabbitTemplate = RabbitTemplateFactory.getRabbitTemplate("myVhost", "first");
        ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.deleteExchange("test");

        // topic
        rabbitMQTopic.setExchangeType("topic");
        rabbitMQTopic.setRouteKey("routKey");
        eventDefinition.setTopic(JSONObject.toJSONString(rabbitMQTopic));
        sender.sendMq(eventDefinition);
        rabbitAdmin.deleteExchange("test");

    }

}
