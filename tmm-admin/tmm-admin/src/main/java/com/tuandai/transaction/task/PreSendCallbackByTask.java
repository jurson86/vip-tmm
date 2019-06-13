package com.tuandai.transaction.task;

import com.tuandai.transaction.service.inf.TransactionCheckService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.log.XxlJobLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 预发送mq回调处理定时任务
 * (cron="0/5 * *  * * ? ") 每5秒钟执行一次
 */
@JobHander(value="preSendCallbackByTaskTaskHandler")
@Service
public class PreSendCallbackByTask extends IJobHandler {

    private static final Logger logger = LoggerFactory.getLogger(PreSendCallbackByTask.class);

    @Autowired
    private TransactionCheckService transactionCheckService;

    @Override
    public ReturnT<String> execute(String... params) throws Exception {
        long begin = System.currentTimeMillis();
        XxlJobLogger.log("preSendCallbackByTaskTask业务执行器开始处理.");
        boolean isSuccess = true;
        try {
            transactionCheckService.preSendCallbackByTask();
        } catch (Exception e) {
            isSuccess = false;
            XxlJobLogger.log("preSendCallbackByTaskTask业务执行处理位置异常");
            logger.error("preSendCallbackByTaskTask业务执行处理位置异常", e);
        }
        long time = System.currentTimeMillis() - begin;
        XxlJobLogger.log("preSendCallbackByTaskTask业务执行处理完成，执行时间毫秒" + String.valueOf(time));
        return  isSuccess ? ReturnT.SUCCESS : ReturnT.FAIL;
    }
}
