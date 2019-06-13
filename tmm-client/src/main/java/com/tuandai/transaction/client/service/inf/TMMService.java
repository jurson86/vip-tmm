package com.tuandai.transaction.client.service.inf;

import com.tuandai.transaction.client.model.BeginLog;
import com.tuandai.transaction.client.model.EndLog;
import com.tuandai.transaction.client.model.MqLog;

import java.util.Map;

public interface TMMService {

    Boolean sendTransBeginToFlume(BeginLog beginLog);

    Boolean sendTransEndToFlume(EndLog endLog);

    Boolean sendNTrans(MqLog mqLog);

    void shutdown();

    Map<String, Integer> monitorData();

}
