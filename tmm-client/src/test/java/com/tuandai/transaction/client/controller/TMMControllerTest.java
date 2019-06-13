package com.tuandai.transaction.client.controller;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.service.inf.TMMService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Gus Jiang
 * @date 2018/4/28  14:02
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TMMController.class })
public class TMMControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(TMMControllerTest.class);

    @InjectMocks
    private TMMController tmmController;

    private MockMvc mvc;

    @Mock
    TMMService tmmService;

    Map<String, Integer> monitorData = new HashMap<>();

    private void init()
    {
        monitorData.put("rpc", 1);
        monitorData.put("done", 1);
    }

    @Before
    public void setUp() throws Exception {
        //初始化数据
        init();
        //将需要mock的对象预习准备好；
        tmmController = new TMMController();
        MockitoAnnotations.initMocks(this);
        //mock mvc 控制器
        mvc = MockMvcBuilders.standaloneSetup(tmmController).build();
    }


    @After
    public void tearDown() throws Exception {
        reset(tmmService);
    }

    @Test
    public void createMessageTest() throws Exception {
        when(tmmService.monitorData()).thenReturn(monitorData);


        String result = mvc.perform(MockMvcRequestBuilders.post("/monitor/tmm").contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(""))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn()
                .getResponse()
                .getContentAsString();
        logger.info("===================> result: {}" ,result);
        JSONObject jo = JSONObject.parseObject(result);
        assertThat(jo.getInteger("status")).as("判断是否成功").isEqualTo(200);
        assertThat(jo.getObject("data",Map.class)).as("判断是否成功").isEqualTo(monitorData);

    }

}