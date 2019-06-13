package com.tuandai.transaction.task;

import com.tuandai.transaction.service.inf.RabbitMqService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.log.XxlJobLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 死信队列消费者
 * (cron="0/10 * *  * * ? ") 每10秒钟执行一次
 */
@JobHander(value="dLQConsumerTaskHandler")
@Service
public class DLQConsumerTask extends IJobHandler {

    private static final Logger logger = LoggerFactory.getLogger(DLQConsumerTask.class);

    @Autowired
    private RabbitMqService rabbitMqService;

    @Override
    public ReturnT<String> execute(String... params) throws Exception {
        long begin = System.currentTimeMillis();
        XxlJobLogger.log("DLQConsumerTask业务执行器开始处理.");
        boolean isSuccess = true;
        try {
            rabbitMqService.rabbitmqDLQConsumer();
        } catch (Exception e) {
            isSuccess = false;
            XxlJobLogger.log("DLQConsumerTask业务执行处理位置异常");
            logger.error("DLQConsumerTask业务执行处理位置异常", e);
        }
        long time = System.currentTimeMillis() - begin;
        XxlJobLogger.log("DLQConsumerTask业务执行处理完成，执行时间毫秒" +String.valueOf(time));
        return isSuccess ? ReturnT.SUCCESS : ReturnT.FAIL;
    }
}
