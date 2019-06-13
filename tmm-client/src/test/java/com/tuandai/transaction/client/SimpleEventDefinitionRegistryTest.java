package com.tuandai.transaction.client;

import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.bo.SendState;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.service.LogAnalyzerServiceImpl;
import com.tuandai.transaction.client.service.SimpleEventDefinitionRegistry;
import com.tuandai.transaction.client.service.App;
import com.tuandai.transaction.client.utils.CacheMapFileUtils;
import com.tuandai.transaction.client.utils.ConstantUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = App.class, properties ={"classpath:application.properties"} )
public class SimpleEventDefinitionRegistryTest {

    /**
     * beginMap基本注册
     */
    @Test
    public void registerEventDefinitionTest() {
        SimpleEventDefinitionRegistry registry = new SimpleEventDefinitionRegistry();
        EventDefinition event = getEventBegin();
        registry.registerEventDefinition("name", event);
        assertEquals(registry.getBeginMap().get("name"), event);
    }

    /**
     * check数据分离，超时分离
     */
    @Test
    public void getCheckDefinitionMapTest() throws InterruptedException {
        // 加入beanMap
        SimpleEventDefinitionRegistry registry = new SimpleEventDefinitionRegistry();
        EventDefinition event = getEventBegin();
        registry.registerEventDefinition("name2", event);
        assertEquals(registry.getBeginMap().get("name2"), event);

        Thread.sleep(2000);

        Map<String, EventDefinition> check_map = registry.getCheckDefinitionMap();
        assertEquals(check_map.get("name2"), event);
    }


    /**
     * begin 持久化
     */
    @Test
    public void persistentEventDefinitionTest()  {

        // 删除rpc文件夹下的文件
        CacheMapFileUtils.delAllFile(SettingSupport.getRpcDirCanonicalPath());

        // 加入beanMap
        SimpleEventDefinitionRegistry registry = new SimpleEventDefinitionRegistry();
        registry.init();
        EventDefinition beginEvent = getEventBegin();
        EventDefinition endEvent = getEventEnd();

        registry.registerEventDefinition("begin", beginEvent);
        registry.registerEventDefinition("end", endEvent);
        assertEquals(registry.getBeginMap().get("begin"), beginEvent);
        assertEquals(registry.getEndMap().get("end"), endEvent);
        registry.persistentEventDefinition();

        // 检查文件是否存在
        boolean isExists = CacheMapFileUtils.existsFileName(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.mapFile);

        assertEquals(isExists, true);

    }

    /**
     * check持久化
     */
    @Test
    public void checkPersistentEventDefinitionTest() throws InterruptedException {

        // 删除rpc文件夹下的文件
        CacheMapFileUtils.delAllFile(SettingSupport.getRpcDirCanonicalPath());

        // 加入beanMap
        SimpleEventDefinitionRegistry registry = new SimpleEventDefinitionRegistry();
        registry.init();
        EventDefinition beginEvent = getEventBegin();

        registry.registerEventDefinition("begin", beginEvent);
        assertEquals(registry.getBeginMap().get("begin"), beginEvent);

        // 2秒
        Thread.sleep(2000);

        Map<String, EventDefinition> checkMap = registry.getCheckDefinitionMap();
        assertEquals(checkMap.get("begin"), beginEvent);


        registry.persistentEventDefinition();

        boolean isExists = CacheMapFileUtils.existsFileName(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.mapCheckFile);

        assertEquals(isExists, true);

    }




    /**
     * 加载beginMap
     */
    @Test
    public void loadAllPersistentEventDefinitionTest() {

        // 删除rpc文件夹下的文件
        CacheMapFileUtils.delAllFile(SettingSupport.getRpcDirCanonicalPath());

        // 加入beanMap
        SimpleEventDefinitionRegistry registry = new SimpleEventDefinitionRegistry();
        registry.init();
        EventDefinition beginEvent = getEventBegin();

        registry.registerEventDefinition("begin", beginEvent);
        assertEquals(registry.getBeginMap().get("begin"), beginEvent);
        registry.persistentEventDefinition();

        registry.loadAllPersistentEventDefinition();

        assertNotNull(registry.getBeginMap().get(beginEvent.getUid()));
    }



    private EventDefinition getEventBegin() {
        EventDefinition event = new EventDefinition();
        event.setUid("uid1212");
        event.setEventType(EventDefinition.EventType.BEGIN);
        event.setTime(new Date().getTime());
        event.setServiceName("test");
        event.setMessage("helloword");
        event.setTopic("{'exchange':'tmm-test','exchangeType':'fanout','vHost':'tmmVhost', 'routeKey':'routeKey'}");
        return event;
    }

    private EventDefinition getEventEnd() {
        EventDefinition event = new EventDefinition();
        event.setUid("uid1212");
        event.setEventType(EventDefinition.EventType.END);
        event.setTime(new Date().getTime());
        event.setServiceName("test");
        event.setSendState(SendState.COMMIT);
        event.setMessage("helloword");
        event.setTopic("{'exchange':'tmm-test','exchangeType':'fanout','vHost':'tmmVhost', 'routeKey':'routeKey'}");
        return event;
    }

    @Test
    public void logAnalyzerServiceImpl() {
        SimpleEventDefinitionRegistry registry = new SimpleEventDefinitionRegistry();
        LogAnalyzerServiceImpl logAnalyzerServiceImpl = new LogAnalyzerServiceImpl(registry);
        EventDefinition eventDefinition = getEventBegin();
        logAnalyzerServiceImpl.analysis(eventDefinition);
        EventDefinition eventDefinitions = getEventEnd();
        EventDefinition sad = logAnalyzerServiceImpl.analysis(eventDefinitions);
        assertNotNull(sad);
    }


}
