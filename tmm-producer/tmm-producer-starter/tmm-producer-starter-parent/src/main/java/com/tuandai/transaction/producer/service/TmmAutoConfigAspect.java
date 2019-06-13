package com.tuandai.transaction.producer.service;

import com.tuandai.transaction.producer.annotation.TmmAutoConfig;
import com.tuandai.transaction.producer.config.TMMContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class TmmAutoConfigAspect {

    @Pointcut("@annotation(com.tuandai.transaction.producer.annotation.TmmAutoConfig)")
    public void TmmAutoConfigInterceptor() {
    }

    @Before(value = "TmmAutoConfigInterceptor()")
    public void before(final JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        TmmAutoConfig tmmAuto = method.getAnnotation(TmmAutoConfig.class);
        if (tmmAuto != null && tmmAuto.isAuto()) {
            TMMContextHolder.isAutoConfig.set(true);
        }
    }

}
