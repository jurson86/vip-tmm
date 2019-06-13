package com.tuandai.transaction.controller;

import com.alibaba.fastjson.JSON;
import com.tuandai.transaction.ApplicationTest;
import com.tuandai.transaction.vo.ClientParameterVo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @Date: 2018/7/2 0002 17:34
 * @Description:
 */


@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = ApplicationTest.class,properties ={"classpath:application-test.properties"} )//没用 boostrap 优先级最高了
public class UserControllerTest {

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mvc;


    @Before
    public void setUp(){
        mvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    @Test
    public void testLogin() throws Exception {

        ClientParameterVo clientParameterVo = new ClientParameterVo();

        clientParameterVo.setUserName("guoguo");

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/login")
                //.header("token","")
                //.contentType(MediaType.)
                .param("userName","guoguo")
                .param("passWord","guoguo")
                .content(JSON.toJSONString(clientParameterVo))
                //.param("userName","guoguo")
                //.param("address","guoguo")
                //.param("user","{\"userName\":\"guoguo\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();


        Assert.assertNotNull(mvcResult.getResponse().getContentAsString());
    }

    @After
    public void after(){
        ctx = null;
    }


}
