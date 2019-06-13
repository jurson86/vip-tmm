package com.tuandai.transaction.producer.core;
import com.tuandai.transaction.client.service.TMMEvent;
import com.tuandai.transaction.client.service.TMMServiceImpl;
import com.tuandai.transaction.client.service.inf.TMMEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RocketMqEventListener implements TMMEventListener<TMMEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RocketMqEventListener.class);

    @Override
    public void onApplicationEvent(TMMEvent event) {
        sendAgentStartInfo(event);
    }

    private void sendAgentStartInfo(TMMEvent event) {
        // 设置源目标
        RocketMqServiceImpl.tmmService = (TMMServiceImpl)event.getSource();
        // TODO
    }
}
