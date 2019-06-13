package com.tuandai.transaction.client.service;

import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.service.inf.TMMEventListener;
import com.tuandai.transaction.client.service.inf.TMMPublisher;

import java.util.ArrayList;
import java.util.List;

public class TMMPublisherImpl implements TMMPublisher {

    private static List<TMMEventListener> listeners = new ArrayList<>();

    static {
        registryListener();
    }

    @Override
    public void publishEvent(TMMEvent event) {
        // 寻找所有已经注册的监听者，发布事件
        for (TMMEventListener listener : listeners) {
            listener.onApplicationEvent(event);
        }
    }

    public static void registryListener() {
        // 利用spi机制加载类实例，注册监听者服务
        List<TMMEventListener> tMMEventListeners = SettingSupport.getTMMEventListeners();
        listeners.addAll(tMMEventListeners);
    }

}
