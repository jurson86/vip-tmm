package com.tuandai.transaction.task;

import com.tuandai.transaction.service.inf.MonitorService;
import com.tuandai.transaction.service.inf.RabbitMqService;
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
 * 监控代理服务器的文件数量
 * (cron="0/10 * *  * * ? ") 每10秒钟执行一次
 *
 * 增加@JobHander注解，并继承IJobHandler。
 * JobHander value属性，在配置调度任务时需要用到
 */
@JobHander(value="monitorAgentServiceTaskHandler")
@Service
public class MonitorAgentTask extends IJobHandler {

    private static final Logger logger = LoggerFactory.getLogger(MonitorAgentTask.class);

    @Autowired
    private MonitorService monitorService;

    @Override
    public ReturnT<String> execute(String... strings) throws Exception {
        long begin = System.currentTimeMillis();
        XxlJobLogger.log("monitorAgentServiceTask业务执行器开始处理.");
        boolean isSuccess = true;
        try {
            monitorService.monitorAgentService();
        } catch (Exception e) {
            isSuccess = false;
            XxlJobLogger.log("monitorAgentServiceTask业务执行处理位置异常");
            logger.error("monitorAgentServiceTask业务执行处理位置异常", e);
        }
        long time = System.currentTimeMillis() - begin;
        XxlJobLogger.log("monitorAgentServiceTask业务执行处理完成，执行时间毫秒" + String.valueOf(time));
        return  isSuccess ? ReturnT.SUCCESS : ReturnT.FAIL;
    }
}
