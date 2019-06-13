package com.tuandai.transaction.bo;

import org.springframework.context.ApplicationEvent;

public class MqClusterEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MqClusterEvent(Object source) {
        super(source);
    }
}
