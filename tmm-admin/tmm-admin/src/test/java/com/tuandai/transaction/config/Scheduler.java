package com.tuandai.transaction.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

/**
 * @Author: guoguo
 * @Date: 2018/7/3 0003 11:45
 * @Description:
 */

@Configuration
public class Scheduler {

    @Conditional(SchedulerCondition.class)
    @Bean(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public ScheduledAnnotationBeanPostProcessor scheduledAnnotationBeanPostProcessor(){
        return new ScheduledAnnotationBeanPostProcessor();
    }
}
