package com.tuandai.transaction.controller;

import com.alibaba.fastjson.JSON;
import com.tuandai.transaction.ApplicationTest;
import com.tuandai.transaction.vo.ClientParameterVo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * @Author: guoguo
 * @Date: 2018/7/3 0003 15:01
 * @Description:
 */

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = ApplicationTest.class,properties ={"classpath:application-test.properties"} )//没卵用，远程文件如果覆盖还是的加其他配置
public class RoleControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleControllerTest.class);

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mvc;

    @Before
    public void setUp(){
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) ctx.getAutowireCapableBeanFactory();
      /*  defaultListableBeanFactory.removeBeanDefinition("xxlJobExecutor");
        defaultListableBeanFactory.removeBeanDefinition("DLQConsumerTask");
        defaultListableBeanFactory.removeBeanDefinition("monitorAgentTask");
        defaultListableBeanFactory.removeBeanDefinition("preSendCallbackByTask");
        defaultListableBeanFactory.removeBeanDefinition("sendTask");*/
        mvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        //XxlJobExecutor executor = (XxlJobExecutor) ctx.getBean("xxlJobExecutor");
        //System.out.println("xxxxx:"+ctx.getEnvironment().getProperty("xxl.job.admin.addresses"));

        //executor.destroy();

    }

    @Test
    public void testRoleQueryList() throws Exception {
        ClientParameterVo clientParameterVo = new ClientParameterVo();
        //clientParameterVo.setRoleName("guoguo");

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/role/queryRolelist")
                //.header("token","")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                /*.param("userName","guoguo")
                .param("passWord","guoguo")*/
                .content(JSON.toJSONString(clientParameterVo))
                //.param("userName","guoguo")
                //.param("address","guoguo")
                //.param("user","{\"userName\":\"guoguo\"}")s
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();


        Assert.assertNotNull(mvcResult.getResponse().getContentAsString());
    }



}
