package com.tuandai.transaction.client.service;

import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.config.TMMRabbitProperties;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

/**
 * @author Gus Jiang
 * @date 2018/4/28  15:12
 */
public class TMMServiceImplTest {

    @InjectMocks
    private TMMServiceImpl tmmServiceImpl;

    @Before
    public void setUp() {
        //初始化加载配置文件信息
        TMMRabbitProperties tmmRabbitMqProperties = new TMMRabbitProperties();
        SettingSupport settingSupport = new SettingSupport();
        //初始化内存基础数据
        tmmServiceImpl = new TMMServiceImpl();
        MockitoAnnotations.initMocks(this);
    }
    
    public void loadAllPersistentEventDefinition_test() {
        tmmServiceImpl.loadAllPersistentEventDefinition();
    }
}
