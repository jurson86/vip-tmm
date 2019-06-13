package com.tuandai.transaction.client;


import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.AdminApplication;
import com.tuandai.transaction.bo.RabbitMQTopic;
import com.tuandai.transaction.dao.TransactionCheckDao;
import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.mq.MqSendHelper;
import com.tuandai.transaction.service.TransactionCheckServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = AdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class TransactionCheckServiceImplTest {

    @Autowired
    private TransactionCheckDao transactionCheckDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TransactionCheckServiceImpl transactionCheckService;

    /**
     * 启动message-procutor
     * @throws InterruptedException
     */
    @Test
    public void preSendCallbackByTaskTest() throws InterruptedException {

        // 删除所有数据
        transactionCheckDao.deleteAll();
        // 发送一条check数据
        rabbitTemplate.convertAndSend("tmm-check", "", "{\"checkUrl\":\"/msg/tmm/check\",\"eventType\":\"BEGIN\",\"goMapTime\":0,\"message\":\"helloword\",\"serviceName\":\"transaction-producer\",\"time\":1519894589054,\"topic\":\"{'exchange':'myExchange2','exchangeType':'fanout','vHost':'myVhost', 'routeKey':''}\",\"uid\":\"uid-test-16\"}");

        Thread.sleep(5000);
        transactionCheckService.preSendCallbackByTask();
        Thread.sleep(10000);
        List<TransactionCheck> list = transactionCheckDao.queryTransactionCheckByFilter(null, null);
        assertEquals(1, list.size());
        assertEquals(list.get(0).getMessageState().longValue(), 30);
    }


    @Test
    public void sendTaskTest() throws InterruptedException {

        // 删除所有数据
        transactionCheckDao.deleteAll();
        // 发送一条check数据
        rabbitTemplate.convertAndSend("tmm-check", "", "{\"checkUrl\":\"/msg/tmm/check\",\"eventType\":\"BEGIN\",\"goMapTime\":0,\"message\":\"helloword\",\"serviceName\":\"transaction-producer\",\"time\":1519894589054,\"topic\":\"{'exchange':'myExchange2','exchangeType':'fanout','vHost':'myVhost', 'routeKey':''}\",\"uid\":\"uid-test-16\"}");
        // 60s
        Thread.sleep(5000);

        List<TransactionCheck> list = transactionCheckDao.queryTransactionCheckByFilter(null, null);
        assertEquals(1, list.size());
        TransactionCheck newTmp = new TransactionCheck();
        newTmp.setPid(list.get(0).getPid());
        newTmp.setMessageState(20);
        newTmp.setUpdateTime(new Date());
        transactionCheckDao.update(newTmp);


        transactionCheckService.sendTask();
        Thread.sleep(1000);
        List<TransactionCheck> list2 = transactionCheckDao.queryTransactionCheckByFilter(null, null);
        assertEquals(1, list2.size());
        assertEquals(list2.get(0).getMessageState().longValue(), 30);
    }



}
