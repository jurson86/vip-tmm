package com.tuandai.transaction.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @Author: guoguo
 * @Date: 2018/7/3 0003 11:48
 * @Description:
 */
public class SchedulerCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata annotatedTypeMetadata) {
        System.out.println(context.getEnvironment().getProperty("spring.main.web_environment"));
        return Boolean.valueOf(context.getEnvironment().getProperty("com.myapp.config.scheduler.enabled"));
    }
}
