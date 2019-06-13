package com.tuandai.transaction.client.service.inf;

import com.tuandai.transaction.client.service.TMMEvent;

/**
 * 事件发布器
 */
public interface TMMPublisher {

    void publishEvent(TMMEvent event);

}
