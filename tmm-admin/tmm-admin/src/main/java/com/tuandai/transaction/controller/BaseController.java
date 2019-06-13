package com.tuandai.transaction.controller;

import com.tuandai.transaction.vo.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: guoguo
 * @Date: 2018/6/5 0005 11:47
 * @Description:
 */

public class BaseController {

    private static Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

    /*private static ScheduledExecutorService scheduExec =  Executors.newSingleThreadScheduledExecutor(); ;

    static {
        scheduExec.scheduleAtFixedRate(new OffLineTask(), 60, 60,TimeUnit.SECONDS);
    }*/

    private static ConcurrentHashMap<String, UserInfo> currentUser = new ConcurrentHashMap<String, UserInfo>();

    public static UserInfo currentUserInfo(String key) {
        return currentUser.get(key);
    }

    public static void setCurrentUserInfo(String key, UserInfo userInfo) {
        currentUser.put(key, userInfo);
    }

    public static void removeUserInfo(String key){
        currentUser.remove(key);
    }

    /*private static class OffLineTask implements Runnable {
        public void run(){
            LOGGER.info("token定时清理");
            currentUser.clear();
        }
    }*/




}
