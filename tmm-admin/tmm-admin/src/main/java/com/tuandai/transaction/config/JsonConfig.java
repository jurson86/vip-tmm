package com.tuandai.transaction.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @Author: guoguo
 * @Date: 2018/6/7 0007 16:37
 * @Description:
 */
public class JsonConfig extends ObjectMapper {

    public JsonConfig(){
        super();
        this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.SIMPLIFIED_CHINESE);
        System.setProperty("user.timezone","Asia/Shanghai");
        Calendar calendar = Calendar.getInstance();
        //TimeZone china = TimeZone.getTimeZone("GMT+:08:00");
        this.setTimeZone(calendar.getTimeZone());
        this.setDateFormat(dateFormat);
    }
}
