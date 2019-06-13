package com.tuandai.transaction.producer.service;


import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

public class AutoMethodBeforeAdvice implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
       // TODO
    }

}
