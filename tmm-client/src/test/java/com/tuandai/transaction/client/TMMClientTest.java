package com.tuandai.transaction.client;

import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.bo.SendState;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.model.BeginLog;
import com.tuandai.transaction.client.model.EndLog;
import com.tuandai.transaction.client.model.RabbitMQTopic;
import com.tuandai.transaction.client.service.App;
import com.tuandai.transaction.client.service.DoneFileQuartz;
import com.tuandai.transaction.client.service.LogEventServiceImpl;
import com.tuandai.transaction.client.service.TMMServiceImpl;
import com.tuandai.transaction.client.utils.CacheMapFileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = App.class, properties ={"classpath:application.properties"} )
public class TMMClientTest {

    @Autowired
    private TMMServiceImpl tmmService;

    /**
     *  rpc 分文件处理
     */
    @Test
    public void sendTransBeginToFlumeTest() {
        //TMMServiceImpl.getInstance().init(null, "10.100.11.160", 5672, "admin", "admin", false);

        BeginLog beginLog = new BeginLog();
        beginLog.setCheck("tmm/check");
       // beginLog.setKey("");
        beginLog.setMessage(null);
        beginLog.setServiceName("tmm");
        RabbitMQTopic rabbitMQTopic = new RabbitMQTopic();
        rabbitMQTopic.setvHost("myVhost");
        rabbitMQTopic.setExchange("myExchange2");
        rabbitMQTopic.setExchangeType("fanout");
        beginLog.setTopic(rabbitMQTopic.toJSONString());
        beginLog.setUid("msgId21212121212121212121212121212121212121212132121212121212121212121212121212121212121212121212121212121212121212121212121212121");
        for (int i = 0; i < 100000; i++) {
           // TMMServiceImpl.getInstance().sendTransBeginToFlume(beginLog);
        }
//        File[] fs = CacheMapFileUtils.searchFile(new FileNameSelector("rpc"), settingSupport.getRpcDir());
//        assertNotNull(fs);
    }

    /**
     * 持久化
     */
      @Test
    public void persistentEventDefinitionTest() {
      }

    /**
     * 完成覆盖率指标
     */
    @Test
    public void doneFileQuartzTest() throws Exception {
        DoneFileQuartz doneFileQuartz = new DoneFileQuartz();
        doneFileQuartz.doExecute(SettingSupport.getRpcDir());
    }


    @Test
    public void tMMService2() throws Exception {
        TMMServiceImpl tMMService = null;
        try {
            tMMService = new TMMServiceImpl();
        }catch (Exception e) {

        }
        EndLog endlog = new EndLog();
        endlog.setTopic("asd");
        endlog.setUid("asdas");
        endlog.setServiceName("asdasd");
        endlog.setMessage("adasdsdsadasd");
        endlog.setState(SendState.COMMIT);
        try {
            tMMService.sendTransEndToFlume(endlog);
        }catch (Exception e) {
        }
        BeginLog be = new BeginLog();
        be.setCheck("sadasdasdasd");
        be.setMessage("adasdsdsadasd");
        be.setServiceName("sadadas");
        be.setUid("asdsad");
        try {
            tMMService.sendTransBeginToFlume(be);
        }catch (Exception e) {

        }
    }

    @Test
    public  void checkTest() throws InterruptedException, NoSuchFieldException {
        CacheMapFileUtils.delAllFile(SettingSupport.getRpcDirCanonicalPath());
        LogEventServiceImpl logEventServiceImpl = new LogEventServiceImpl();
        EventDefinition tmp = getEventDefinition();
        int size = 100;
        for (int i = 0; i< size; i++) {
            logEventServiceImpl.writeLogEvent(tmp);
        }
        Thread.sleep(10000);
    }

    private EventDefinition getEventDefinition() {
        EventDefinition eventDefinition = new EventDefinition();
        //eventDefinition.setRouteKey("key");
        eventDefinition.setTopic("{'exchange':'tmm-test','exchangeType':'fanout','vHost':'tmmVhost', 'routeKey':''}");
        eventDefinition.setMessage("helloworld");
        eventDefinition.setSendState(SendState.COMMIT);
        eventDefinition.setServiceName("test");
        eventDefinition.setTime(new Date().getTime());
        eventDefinition.setEventType(EventDefinition.EventType.BEGIN);
        eventDefinition.setUid("uid1212");
        return eventDefinition;
    }


}
