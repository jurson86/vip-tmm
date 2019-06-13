package com.tuandai.transaction.producer.service;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.model.BeginLog;
import com.tuandai.transaction.producer.config.TMMAutoConfiguration;
import com.tuandai.transaction.producer.config.TMMContextHolder;
import com.tuandai.transaction.producer.model.RabbitMQTopic;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.UUID;

public class AutoMethodBeforeAdvice implements MethodBeforeAdvice {

    private final static ThreadLocal<String> uidCache = new ThreadLocal<>();

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        TMMAutoConfiguration tMMAutoConfiguration = TMMContextHolder.applicationContext.getBean(TMMAutoConfiguration.class);
        Boolean isAuto = TMMContextHolder.isAutoConfig.get();
        if (isAuto == null || !isAuto) {
            return;
        }
        String methodName = method.getName();
        Object log = args[0];
        if (log == null) {
            if ("sendTransBeginToFlume".equals(methodName)) {
                log = new BeginLog();
            } else {
                return;
            }
        }
        Class logClass = log.getClass();

        // setUid
        Method getUidMeth = logClass.getMethod("getUid");
        Method setUidMeth = logClass.getMethod("setUid", String.class);
        String uuid = null;
        if (StringUtils.isEmpty(getUidMeth.invoke(log))) {
            if ("sendTransBeginToFlume".equals(methodName) || "sendNTrans".equals(methodName)) { // 如果是 非结束日志
                if ("random".equals(tMMAutoConfiguration.getUid())) {
                    uuid = UUID.randomUUID().toString();
                } else {
                    // 可指定特定的uid
                    uuid = tMMAutoConfiguration.getUid();
                }
                uidCache.set(uuid);
            } else { // 结束日志 "sendTransEndToFlume".equals(methodName)
                uuid = uidCache.get();
            }
            setUidMeth.invoke(log, uuid);
        }

        // setCheck
        if ("sendTransBeginToFlume".equals(methodName)) {
            Method getCheckMeth = logClass.getMethod("getCheck");
            Method setCheckMeth = logClass.getMethod("setCheck", String.class);
            if (StringUtils.isEmpty(getCheckMeth.invoke(log))) {
                if (!StringUtils.isEmpty(tMMAutoConfiguration.getCheck())) {
                    setCheckMeth.invoke(log, tMMAutoConfiguration.getCheck());
                }
            }
        }


        // setServiceName
        Method getServiceNameMeth = logClass.getMethod("getServiceName");
        Method setServiceNameMeth = logClass.getMethod("setServiceName", String.class);
        if (StringUtils.isEmpty(getServiceNameMeth.invoke(log))) {
            if (!StringUtils.isEmpty(tMMAutoConfiguration.getServiceName())) {
                setServiceNameMeth.invoke(log, tMMAutoConfiguration.getServiceName());
            }
        }

        if (!"sendTransEndToFlume".equals(methodName)) {
            // setTopic
            Method getTopicMeth = logClass.getMethod("getTopic");
            Method setTopicMeth = logClass.getMethod("setTopic", String.class);
            RabbitMQTopic rabbitMQTopic = null;
            String topic = (String)getTopicMeth.invoke(log);
            if (StringUtils.isEmpty(topic)) {
                if (!StringUtils.isEmpty(tMMAutoConfiguration.getVhost()) || !StringUtils.isEmpty(tMMAutoConfiguration.getExchange()) ||
                        !StringUtils.isEmpty(tMMAutoConfiguration.getExchangeType()) || !StringUtils.isEmpty(tMMAutoConfiguration.getRouteKey())
                        || !StringUtils.isEmpty(tMMAutoConfiguration.getIp())) {
                    rabbitMQTopic = new RabbitMQTopic();
                }
                if (!StringUtils.isEmpty(tMMAutoConfiguration.getVhost())) {
                    rabbitMQTopic.setvHost(tMMAutoConfiguration.getVhost());
                }
                if (!StringUtils.isEmpty(tMMAutoConfiguration.getExchange())) {
                    rabbitMQTopic.setExchange(tMMAutoConfiguration.getExchange());
                }
                if (!StringUtils.isEmpty(tMMAutoConfiguration.getExchangeType())) {
                    rabbitMQTopic.setExchangeType(tMMAutoConfiguration.getExchangeType());
                }
                if (!StringUtils.isEmpty(tMMAutoConfiguration.getRouteKey())) {
                    rabbitMQTopic.setRouteKey(tMMAutoConfiguration.getRouteKey());
                }
                if (!StringUtils.isEmpty(tMMAutoConfiguration.getIp())) {
                    rabbitMQTopic.setIp(tMMAutoConfiguration.getIp());
                }

            } else {
                rabbitMQTopic = JSONObject.parseObject(topic, RabbitMQTopic.class);
                if (StringUtils.isEmpty(rabbitMQTopic.getvHost())) {
                    if (!StringUtils.isEmpty(tMMAutoConfiguration.getVhost())) {
                        rabbitMQTopic.setvHost(tMMAutoConfiguration.getVhost());
                    }
                }
                if (StringUtils.isEmpty(rabbitMQTopic.getRouteKey())) {
                    if (!StringUtils.isEmpty(tMMAutoConfiguration.getRouteKey())) {
                        rabbitMQTopic.setRouteKey(tMMAutoConfiguration.getRouteKey());
                    }
                }
                if (StringUtils.isEmpty(rabbitMQTopic.getExchange())) {
                    if (!StringUtils.isEmpty(tMMAutoConfiguration.getExchange())) {
                        rabbitMQTopic.setExchange(tMMAutoConfiguration.getExchange());
                    }
                }
                if (StringUtils.isEmpty(rabbitMQTopic.getExchangeType())) {
                    if (!StringUtils.isEmpty(tMMAutoConfiguration.getExchangeType())) {
                        rabbitMQTopic.setExchangeType(tMMAutoConfiguration.getExchangeType());
                    }
                }
                if (StringUtils.isEmpty(rabbitMQTopic.getIp())) {
                    if (!StringUtils.isEmpty(tMMAutoConfiguration.getIp())) {
                        rabbitMQTopic.setIp(tMMAutoConfiguration.getIp());
                    }
                }
            }
            setTopicMeth.invoke(log, JSONObject.toJSONString(rabbitMQTopic));
        }
//        if (StringUtils.isEmpty(beginLog.getMessageProperties())) {
//            if () {
//                beginLog.setMessageProperties("");
//            }
//        }
    }

}
