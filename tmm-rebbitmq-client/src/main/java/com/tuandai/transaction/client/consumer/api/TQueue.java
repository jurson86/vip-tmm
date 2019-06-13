package com.tuandai.transaction.client.consumer.api;

import com.tuandai.transaction.client.consumer.core.DeadLetterHelper;
import org.springframework.amqp.core.Queue;

import java.util.HashMap;
import java.util.Map;

public class TQueue extends Queue {

    public TQueue(String name, String applicationName) {
        super(name, true, false, false, initMap(null, applicationName));
    }

    public TQueue(String name, boolean durable, String applicationName) {
        super(name, durable, false, false, initMap(null, applicationName));
    }

    public TQueue(String name, boolean durable, boolean exclusive, boolean autoDelete, String applicationName) {
        super(name, durable, exclusive, autoDelete, initMap(null, applicationName));
    }

    public TQueue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments, String applicationName) {
        super(name, durable, exclusive, autoDelete, initMap(arguments, applicationName));
    }

    private static Map<String, Object> initMap(Map<String, Object> arguments, String applicationName) {
        if (arguments == null) {
            arguments = new HashMap<>();
        }
        DeadLetterHelper.initArruments(arguments, applicationName);
        return arguments;
    }
}
