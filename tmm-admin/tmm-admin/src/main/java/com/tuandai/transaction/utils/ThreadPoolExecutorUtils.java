package com.tuandai.transaction.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolExecutorUtils {

    private final static Integer TASK_SIZE = 500;


    private final static ThreadPoolExecutor taskThreadPoolExecutor; // 通用的定时任务执行线程池

    static {
        // 统一命名
        ThreadFactoryBuilder tfb = new ThreadFactoryBuilder();
        ThreadFactory workerFactory = tfb.setNameFormat("task-worker-%d").build();

        taskThreadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(TASK_SIZE, workerFactory);
    }


    public static ThreadPoolExecutor getTaskThreadPoolExecutorUtils() {
        return taskThreadPoolExecutor;
    }


}
