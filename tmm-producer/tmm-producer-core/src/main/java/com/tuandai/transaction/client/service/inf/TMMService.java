package com.tuandai.transaction.client.service.inf;

import com.tuandai.transaction.client.config.TMMConfig;
import com.tuandai.transaction.client.model.BeginLog;
import com.tuandai.transaction.client.model.EndLog;
import com.tuandai.transaction.client.model.MqLog;

import java.util.Map;

public interface TMMService {

    Boolean sendTransBeginToFlume(BeginLog beginLog);

    Boolean sendTransEndToFlume(EndLog endLog);

    Boolean sendNTrans(MqLog mqLog);

    void shutdown();

    void start(TMMConfig tmmConfig);

    Map<String, Long> monitorData();

}
