package com.tuandai.transaction.client.service;

import com.tuandai.transaction.client.config.SettingSupport;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 *  历史数据清理管理
 */
public class DatadirCleanupManager {

    private static final Logger logger = LoggerFactory.getLogger(DatadirCleanupManager.class);

    public DatadirCleanupManager() {
    }

    public void start() {

        // 创建scheduler
        Scheduler scheduler = null;
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();

            // 定义一个Trigger
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1") // 定义name/group
                    .startNow()// 一旦加入scheduler，立即生效
                    .withSchedule(simpleSchedule() // 使用SimpleTrigger
                            .withIntervalInSeconds(24 * 60 * 60) // 每隔一天执行一次 单位s
                            .repeatForever()) // 一直执行
                    .build();

            // 定义一个JobDetail
            JobDataMap map = new JobDataMap();
            map.put("rpcDir", SettingSupport.getRpcDir());
            JobDetail job = JobBuilder.newJob(DoneFileQuartz.class) // 定义Job类为HelloQuartz类，这是真正的执行逻辑所在
                    .withIdentity("job1", "group1") // 定义name/group
                    .usingJobData(map) // 定义属性
                    .build();

            // 加入这个调度
            scheduler.scheduleJob(job, trigger);

            // 启动之
            scheduler.start();
        } catch (SchedulerException e) {
            logger.error("启动定时器失败！！,{}",e.getMessage());
        }
    }

}
