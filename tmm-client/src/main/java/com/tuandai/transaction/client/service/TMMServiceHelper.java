package com.tuandai.transaction.client.service;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.bo.Message;
import com.tuandai.transaction.client.bo.SendState;
import com.tuandai.transaction.client.model.BeginLog;
import com.tuandai.transaction.client.model.EndLog;
import com.tuandai.transaction.client.model.MqLog;
import org.springframework.util.StringUtils;

public class TMMServiceHelper {

    public static void checkBeginLogParam(BeginLog beginlog) {
        if (beginlog == null) {
            throw new IllegalArgumentException("beginLog 不能为空！");
        }

        if (beginlog.getUid() == null) {
            throw new IllegalArgumentException("beginLog的uid不能为空！");
        }
    }

    public static void checkEndLogParam(EndLog endLog) {
        if (endLog.getUid() == null || StringUtils.isEmpty(endLog.getMessage())) {
            throw new IllegalArgumentException("endLog的uid和message不能为空！");
        }
    }

    public static EventDefinition beginLog2EventDefinition(BeginLog beginLog) {
        EventDefinition event = new EventDefinition();
        event.setTopic(beginLog.getTopic());
        event.setMessage(JSONObject.toJSONString(new Message(beginLog.getUid(), beginLog.getMessage())));
        event.setCheckUrl(beginLog.getCheck());
        event.setEventType(EventDefinition.EventType.BEGIN);
        event.setServiceName(beginLog.getServiceName());
        event.setTime(System.currentTimeMillis());
        event.setUid(beginLog.getUid());
        return event;
    }

    public static EventDefinition endLog2EventDefinition(EndLog endLog) {
        EventDefinition event = new EventDefinition();
        event.setTopic(endLog.getTopic());
        event.setMessage(JSONObject.toJSONString(new Message(endLog.getUid(), endLog.getMessage())));
        event.setEventType(EventDefinition.EventType.END);
        event.setSendState(endLog.getState());
        event.setServiceName(endLog.getServiceName());
        event.setTime(System.currentTimeMillis());
        event.setUid(endLog.getUid());
        return event;
    }

    public static EventDefinition mqLog2EventDefinition(MqLog mqLog) {
        EventDefinition event = new EventDefinition();
        event.setTopic(mqLog.getTopic());
        event.setMessage(JSONObject.toJSONString(new Message(mqLog.getUid(), mqLog.getMessage())));
        event.setEventType(EventDefinition.EventType.NTRANS);
        event.setSendState(SendState.COMMIT);
        event.setServiceName(mqLog.getServiceName());
        event.setTime(System.currentTimeMillis());
        event.setUid(mqLog.getUid());
        return event;
    }

    public static void checkMqLogParam(MqLog mqLog) {
        if (mqLog == null) {
            throw new IllegalArgumentException("mqLog不能为空！");
        }
        if (StringUtils.isEmpty(mqLog.getUid())) {
            throw new IllegalArgumentException("mqLog.uid 不能为空！");
        }
        if (StringUtils.isEmpty(mqLog.getMessage())) {
            throw new IllegalArgumentException("mqLog.message 不能为空！");
        }
        if (StringUtils.isEmpty(mqLog.getServiceName())) {
            throw new IllegalArgumentException("mqLog.serviceName 不能为空！");
        }
        if (StringUtils.isEmpty(mqLog.getTopic())) {
            throw new IllegalArgumentException("mqLog.topic 不能为空！");
        }

    }


}
