package com.tuandai.transaction.client.service.inf;

import com.tuandai.transaction.client.service.TMMEvent;

import java.util.EventListener;

/**
 * tmm 事件监听器
 */
public interface TMMEventListener<E extends TMMEvent> extends EventListener {

    /**
     * 事件监听器
     * @param event
     */
    void onApplicationEvent(E event);

}
