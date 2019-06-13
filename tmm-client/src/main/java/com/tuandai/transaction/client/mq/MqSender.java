package com.tuandai.transaction.client.mq;

import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.mq.inf.MqServiceFactory;
import com.tuandai.transaction.client.utils.ThreadPoolExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MqSender {

    private static final Logger logger = LoggerFactory.getLogger(MqSender.class);

    private MqServiceFactory mqServiceFactory;

    public MqSender(MqServiceFactory mqServiceFactory) {
        this.mqServiceFactory = mqServiceFactory;
    }

    public MqServiceFactory getMqServiceFactory() {
        return mqServiceFactory;
    }

    public void setMqServiceFactory(MqServiceFactory mqServiceFactory) {
        this.mqServiceFactory = mqServiceFactory;
    }

    /**
     * 单线程发送mq
     * 单条消息发送成功（包括超时，失败，异常），才返回
     * @param eventDefinition
     * @throws Exception
     */
    public void sendMq(EventDefinition eventDefinition) throws Exception {
        getMqServiceFactory().createMqService().sendMessage(eventDefinition);
    }


    /**
     *多线程发送mq
     * 所有消息发送成功（包括超时，失败，异常），才返回
     * @param events
     * @throws Exception
     */
    public void sendMq(List<EventDefinition> events) throws Exception {
        sendMq(events, new SendMqHandler());
    }

    public void sendMq(List<EventDefinition> events, SendMqHandler sendMqHandler) throws Exception {
        if (!CollectionUtils.isEmpty(events)) {
            ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getSendMqThreadPoolExecutorUtils();
            List<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();
            for (EventDefinition eventDefinition : events) {
                logger.debug("sendMq,处理消息：" + eventDefinition);
                // 发送前预处理
                EventDefinition sendEvent = sendMqHandler.preProcess(eventDefinition);

                Future<Boolean> future = executor.submit(() -> {
                    try {
                        if (sendEvent != null) {
                            long time =  System.currentTimeMillis();
                            logger.debug("TMMServiceImpl 发送业务mq消息，" + sendEvent.getUid());
                            this.sendMq(sendEvent);
                            logger.debug("TMMServiceImpl 发送业务mq消息成功, uid:" + sendEvent.getUid() +
                                    "， 消耗时间：" + (System.currentTimeMillis() - time));
                        }
                    } catch (Exception e) {
                        logger.warn("SendMq 未知异常!", e);
                        return false;
                    }
                    return true;
                });
                futureList.add(future);
            }

            for (Future<Boolean> future : futureList) {
                try {
                    // 设置超时
                    Boolean isSend = future.get(3, TimeUnit.SECONDS);
                    if (!isSend) {
                        logger.warn("SendMq 任务异常中止!!");
                    }
                } catch (TimeoutException e) {
                    logger.warn("SendMq 超时!", e);
                    future.cancel(true);
                } catch (Exception e) {
                    logger.warn("SendMq 未知异常!", e);
                    future.cancel(true);
                }
            }
        }
    }


    /**
     *  发送mq处理类
     */
   public static class SendMqHandler {

       public EventDefinition preProcess(EventDefinition preEventDefinition) {
            return preEventDefinition;
        }

    }

}
