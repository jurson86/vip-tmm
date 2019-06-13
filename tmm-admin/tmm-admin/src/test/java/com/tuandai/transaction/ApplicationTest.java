package com.tuandai.transaction;

import com.xxl.job.core.handler.annotation.JobHander;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author: guoguo
 * @Date: 2018/7/2 0002 17:14
 * @Description:
 */


@SpringBootApplication
@EnableDiscoveryClient  // 消费者
@EnableEurekaClient 	// 生产者
@EnableScheduling
@ComponentScan(basePackages = {"com.tuandai.transaction"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {JobHander.class}),@ComponentScan.Filter(type=FilterType.REGEX,pattern="com\\.tuandai\\.transaction\\.task.*"),
                @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE,classes={com.tuandai.transaction.config.JobConfig.class})
        }
       /* includeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, classes= {Controller.class,Service.class,Repository.class,Component.class})}
        ,useDefaultFilters=false*/
)
//@ConditionalOnWebApplication
public class ApplicationTest {

    public static void main(String[] args){
        //ApplicationContext ctx = new AnnotationConfigApplicationContext("com.tuandai.transaction");
        ConfigurableApplicationContext context = SpringApplication.run(AdminApplication.class, args);
        //获取BeanFactory
        /*DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        String[] beans = context.getBeanDefinitionNames();
        for(String name:beans){
            System.out.println(name);
        }*/
    }
}
