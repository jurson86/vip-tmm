package com.tuandai.transaction.client.service;

import com.tuandai.transaction.client.utils.CacheMapFileUtils;
import com.tuandai.transaction.client.utils.FileNameSelector;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DoneFileQuartz  implements Job {

    private static final Logger logger = LoggerFactory.getLogger(DoneFileQuartz.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail detail = context.getJobDetail();
        File rpcDir = (File)detail.getJobDataMap().get("rpcDir");
        doExecute(rpcDir);
    }

    public void doExecute(File rpcDir) {
        logger.info("开始执行.done文件删除任务！");
        File[] files = CacheMapFileUtils.searchFile(new FileNameSelector("done"), rpcDir);

        if (files != null) {
            for (File file : files) {
                if (file.lastModified() < System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000)) { // 3天
                    CacheMapFileUtils.removeFileName(file);
                }
            }
        }
        logger.info("执行.done文件删除任务结束！");
    }
}
