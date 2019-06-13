package com.tuandai.transaction.client.service;

import com.tuandai.transaction.client.bo.SendState;
import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.service.inf.EventDefinitionRegistry;
import com.tuandai.transaction.client.service.inf.LogAnalyzerService;


public class LogAnalyzerServiceImpl implements LogAnalyzerService {

    private EventDefinitionRegistry eventDefinitionRegistry;

    public LogAnalyzerServiceImpl(EventDefinitionRegistry eventDefinitionRegistry) {
        this.eventDefinitionRegistry = eventDefinitionRegistry;
    }

    @Override
    public EventDefinition analysis(EventDefinition eventDefinition) {
        if (eventDefinition.getEventType() == EventDefinition.EventType.BEGIN) {
            // 处理开始日志
            eventDefinition.setGoMapTime(System.currentTimeMillis());
            eventDefinitionRegistry.registerEventDefinition(eventDefinition.getUid(), eventDefinition);
        } else if (eventDefinition.getEventType() == EventDefinition.EventType.END) {
            // 处理结束日志
            EventDefinition beginEvent = eventDefinitionRegistry.getEventDefinition(eventDefinition.getUid(), EventDefinition.EventType.BEGIN);
            if (beginEvent != null) {
                // 移除beginMap的数据
                eventDefinitionRegistry.removeEventDefinition(eventDefinition.getUid(), EventDefinition.EventType.BEGIN);

                // 提交则返回到主流程发送mq
                if (eventDefinition.getSendState() == SendState.COMMIT) {
                    // 合并mq的属性
                    beginEvent.mergedEventDefinition(eventDefinition);

                    // 将需要发送的数据保存到mqMap中
                    EventDefinition mqSendEvent = new EventDefinition();
                    copyProperties(beginEvent, mqSendEvent);
                    mqSendEvent.setEventType(EventDefinition.EventType.MQ);
                    eventDefinitionRegistry.registerEventDefinition(beginEvent.getUid(), mqSendEvent);

                    return beginEvent;
                }
            }
        } else if (eventDefinition.getEventType() == EventDefinition.EventType.NTRANS) {
            // 如果是直接发送的消息

            // 将需要发送的数据保存到mqMap中
            EventDefinition mqSendEvent = new EventDefinition();
            copyProperties(eventDefinition, mqSendEvent);
            mqSendEvent.setEventType(EventDefinition.EventType.MQ);
            mqSendEvent.setGoMapTime(System.currentTimeMillis());
            eventDefinitionRegistry.registerEventDefinition(eventDefinition.getUid(), mqSendEvent);

            // 直接返回消息
            return eventDefinition;
        }
        return null;
    }


    private static void copyProperties(EventDefinition source, EventDefinition target) {
        target.setSpanId(source.getSpanId());
        target.setTraceId(source.getTraceId());
        target.setMessageProperties(source.getMessageProperties());
        target.setMessage(source.getMessage());
        target.setGoMapTime(source.getGoMapTime());
        target.setEventType(source.getEventType());
        target.setTime(source.getTime());
        target.setUid(source.getUid());
        target.setServiceName(source.getServiceName());
        target.setSendState(source.getSendState());
        target.setTopic(source.getTopic());
        target.setCheckUrl(source.getCheckUrl());
    }

}
