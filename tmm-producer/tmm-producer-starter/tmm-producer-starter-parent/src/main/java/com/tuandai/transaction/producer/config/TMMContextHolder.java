package com.tuandai.transaction.producer.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class TMMContextHolder implements ApplicationContextAware {

    public static ApplicationContext applicationContext;

    public static final ThreadLocal<Boolean> isAutoConfig = new ThreadLocal<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TMMContextHolder.applicationContext = applicationContext;
    }

}
