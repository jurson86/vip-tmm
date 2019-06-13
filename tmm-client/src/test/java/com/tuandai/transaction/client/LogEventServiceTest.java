package com.tuandai.transaction.client;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.bo.SendState;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.service.LogEventServiceImpl;
import com.tuandai.transaction.client.service.App;
import com.tuandai.transaction.client.service.TMMServiceImpl;
import com.tuandai.transaction.client.utils.CacheMapFileUtils;
import com.tuandai.transaction.client.utils.FileNameSelector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = App.class, properties ={"classpath:application.properties"} )
public class LogEventServiceTest {

    @Before
    public void initMq() throws Exception {
        TMMServiceImpl tmm = SettingSupport.context.getBean(TMMServiceImpl.class);
        tmm.shutdown();
    }

    @Test
    public void writeLogEventTest() throws InterruptedException {

        ExecutorService exe = Executors.newFixedThreadPool(10);
        // 删除rpc文件夹下的文件
        CacheMapFileUtils.delAllFile(SettingSupport.getRpcDirCanonicalPath());
        LogEventServiceImpl logEventServiceImpl = new LogEventServiceImpl();

        EventDefinition tmp = getEventDefinition();
        int size = 100;
        for (int i = 0; i< size; i++) {

            exe.execute(new Runnable() {
                @Override
                public void run() {
                    logEventServiceImpl.writeLogEvent(tmp);
                }
            });
        }


        Thread.sleep(1000L);
        File fs = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), SettingSupport.getRpcDir(),false);
        assertNotNull(fs);

        CacheMapFileUtils.readDataToMap(fs, new CacheMapFileUtils.ReadDataToMapProcess() {
            int count = 0;
            @Override
            protected boolean process(String keyValueStr) throws Exception {
                EventDefinition sad = JSONObject.parseObject(keyValueStr, EventDefinition.class);
                count++;
                assertEquals(sad.getUid(), tmp.getUid());
                return true;
            }
        });

    }


    /**
     * 基本读写
     */
    @Test
    public void readLogEventTest() throws InterruptedException {
        ExecutorService exe = Executors.newFixedThreadPool(10);
        // 删除rpc文件夹下的文件
        CacheMapFileUtils.delAllFile(SettingSupport.getRpcDirCanonicalPath());
        LogEventServiceImpl logEventServiceImpl = new LogEventServiceImpl();

        // 写日志
        EventDefinition tmp = getEventDefinition();
        int size = 100;
        for (int i = 0; i< size; i++) {

            exe.execute(new Runnable() {
                @Override
                public void run() {
                    logEventServiceImpl.writeLogEvent(tmp);
                }
            });
        }

        Thread.sleep(5000);

        // 读取日志
        List<EventDefinition> list = logEventServiceImpl.readLogEvent(1000);
        assertEquals(list.size(), 100);

    }


    /**
     * 读写到结束文件
     */
    @Test
    public void readLogEventTest2() throws InterruptedException, IOException {
        ExecutorService exe = Executors.newFixedThreadPool(10);
        // 删除rpc文件夹下的文件
        CacheMapFileUtils.delAllFile(SettingSupport.getRpcDirCanonicalPath());
        LogEventServiceImpl logEventServiceImpl = new LogEventServiceImpl();

        // 写日志
        EventDefinition tmp = getEventDefinition();
        int size = 10000;
        for (int i = 0; i< size; i++) {
            logEventServiceImpl.writeLogEvent(tmp);
        }

        File fs[] = CacheMapFileUtils.searchFile(new FileNameSelector("rpc"), SettingSupport.getRpcDir());

        // 写入的文件大于1
        assertNotEquals(fs.length, 1);

        // 读取日志
//        while (true) {
//            List<EventDefinition> list = logEventServiceImpl.readLogEvent(10000);
//            if (list.size() > 0) {
//                continue;
//            }
//            // 持久化
//            logEventServiceImpl.persistentCheckpoint();
//            // logEventServiceImpl.getCheckPoint() TODO
//
//            assertEquals(logEventServiceImpl.isEnd(), true);
//        }
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
