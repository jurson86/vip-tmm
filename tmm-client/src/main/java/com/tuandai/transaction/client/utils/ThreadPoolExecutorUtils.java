package com.tuandai.transaction.client.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tuandai.transaction.client.config.SettingSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorUtils {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolExecutorUtils.class);

    private static Integer MQ_CORE_POOL_SIZE;
    private static Integer MQ_MAX_POOL_SIZE;
    private static Integer MQ_QUEUE_SIZE;

    // 通用的定时任务执行线程池
    private final static ThreadPoolExecutor sendMqThreadPoolExecutor;

    static {
        MQ_CORE_POOL_SIZE = SettingSupport.getSendMqCorePoolThreadNum();
        MQ_MAX_POOL_SIZE = SettingSupport.getSendMqMaxPoolThreadNum();
        MQ_QUEUE_SIZE = SettingSupport.getSendMqQueueSize();

        sendMqThreadPoolExecutor = new ThreadPoolExecutor(MQ_CORE_POOL_SIZE, MQ_MAX_POOL_SIZE, 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(MQ_QUEUE_SIZE), new ThreadFactoryBuilder().setNameFormat("tmm-mq-send-%d").build());
    }

    public static ThreadPoolExecutor getSendMqThreadPoolExecutorUtils() {
        logger.debug("PoolSize:" +  sendMqThreadPoolExecutor.getPoolSize() +
                " ,ActiveCount:" + sendMqThreadPoolExecutor.getActiveCount() +
                " ,CorePoolSize:" + sendMqThreadPoolExecutor.getCorePoolSize() +
                " ,LargestPoolSize:" + sendMqThreadPoolExecutor.getLargestPoolSize() +
                " ,MaximumPoolSize:" +sendMqThreadPoolExecutor.getMaximumPoolSize() +
                " ,TaskCount:" + sendMqThreadPoolExecutor.getTaskCount());
        return sendMqThreadPoolExecutor;
    }

}
