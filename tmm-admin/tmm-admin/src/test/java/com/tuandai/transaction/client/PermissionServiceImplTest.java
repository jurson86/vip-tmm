package com.tuandai.transaction.client;

/**
 * @Author: guoguo
 * @Date: 2018/5/30 0030 16:29
 * @Description:
 */

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.tuandai.transaction.AdminApplication;
import com.tuandai.transaction.ApplicationTest;
import com.tuandai.transaction.dao.RoleDao;
import com.tuandai.transaction.dao.UserDao;
import com.tuandai.transaction.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = ApplicationTest.class)
public class PermissionServiceImplTest {

    @Autowired
    UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Test
    public void testInsert(){
        //for(int i=0;i<10;i++){
           // userDao.createUser("admin","admin",new Integer(1));
        //}
    }

    @Test
    public void testUserList(){
        PageInfo pageInfo = userDao.queryUserList("guoguo",null,1,20);
        System.out.println(JSON.toJSONString(pageInfo));
    }

    @Test
    public void testCreate(){
        User user = new User("xiaoGuoGuo","guoguo",1);
        userDao.createUserAndRole(user);
    }



}
