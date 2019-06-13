package com.tuandai.transaction.client;

import com.tuandai.transaction.client.bo.SendState;
import com.tuandai.transaction.client.service.App;
import com.tuandai.transaction.client.service.inf.TMMService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;


import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = App.class, properties ={"classpath:application.properties"} )
public class ZabbixServiceImplTest {
    
    @Autowired
    private TMMService tmmService;

    @Test
    public void sendStateTest() {
        SendState.COMMIT.value();
        SendState.COMMIT.message();
        SendState s1  = SendState.findByMessage("COMMIT");
        assertEquals(s1,  SendState.COMMIT);
        SendState s2 = SendState.findByValue(1);
        assertEquals(s2,  SendState.CANCEL);
    }

    @Test
    public void monitorDataTest() {
        Map<String, Integer> map = tmmService.monitorData();
        assertNotEquals(map.size(),  0);
    }

}
