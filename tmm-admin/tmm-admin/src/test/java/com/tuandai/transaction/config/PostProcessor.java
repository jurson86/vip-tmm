package com.tuandai.transaction.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @Author: guoguo
 * @Date: 2018/7/3 0003 16:12
 * @Description:
 */
public class PostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if ("narCodeService".equals(beanName)) {//过滤掉bean实例ID为narCodeService
            return bean;
        }
        System.out.println("后置处理器处理bean=【"+beanName+"】开始");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
