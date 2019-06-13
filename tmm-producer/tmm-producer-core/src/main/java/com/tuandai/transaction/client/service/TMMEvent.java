package com.tuandai.transaction.client.service;

import java.util.EventObject;

/**
 * tmm 事件, 可扩展为TMMEventStart， TMMEventEnd 等事件，目前场景简单不做扩展
 */
public class TMMEvent extends EventObject {

    private Object object;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public TMMEvent(Object source) {
        super(source);
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "TMMEvent{" +
                "object=" + object +
                '}';
    }
}
