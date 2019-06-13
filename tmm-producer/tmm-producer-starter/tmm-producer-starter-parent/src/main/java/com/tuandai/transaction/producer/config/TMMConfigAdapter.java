package com.tuandai.transaction.producer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 适配器，因为老版本导致路径不统一，兼容老版本 ，做了一个适配器
 */
@ConfigurationProperties
public class TMMConfigAdapter {

    /**
     * 需要发送mq类型
     */
    @Value("${mq.type:rabbitmq}")
    private String mqType;

    /**
     * 每批次发送处理日志条数
     */
    @Value("${spring.tmmService.batch.size:2000}")
    private int batchSize;

    /**
     * 日志文件存放路径
     */
    @Value("${spring.tmmService.rpcPath:}")
    private String rpcDirName;

    /**
     * 判定为check数据的时间间隔
     */
    @Value("${spring.tmmService.check.interval.time:60000}")
    private long checkIntervalTime;

    /**
     * rpc文件的切割大小100 * 1024 *1024L = 104857600
     */
    @Value("${rpc.size:104857600}")
    private long rpcSize;

    /**
     * 发送mq线程的核心线程数
     */
    @Value("${send.core.thread.num:5}")
    private int sendMqCorePoolThreadNum;

    /**
     * 发送mq线程的最大线程数
     */
    @Value("${send.max.thread.num:200}")
    private int sendMqMaxPoolThreadNum;

    /**
     * 发送mq线程的队列大小
     */
    @Value("${send.queue.size:5000}")
    private int sendMqQueueSize;

    /**
     * 服务名
     */
    @Value("${spring.application.name}")
    private String serverName;

    @Value("${server.context-path:}")
    private String prefixUrl;

    @Value("${spring.rabbitmq.tmm.tmmMessage:false}")
    private String tmmMessage;

    public String getTmmMessage() {
        return tmmMessage;
    }

    public void setTmmMessage(String tmmMessage) {
        this.tmmMessage = tmmMessage;
    }

    public String getMqType() {
        return mqType;
    }

    public void setMqType(String mqType) {
        this.mqType = mqType;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getRpcDirName() {
        return rpcDirName;
    }

    public void setRpcDirName(String rpcDirName) {
        this.rpcDirName = rpcDirName;
    }

    public long getCheckIntervalTime() {
        return checkIntervalTime;
    }

    public void setCheckIntervalTime(long checkIntervalTime) {
        this.checkIntervalTime = checkIntervalTime;
    }

    public long getRpcSize() {
        return rpcSize;
    }

    public void setRpcSize(long rpcSize) {
        this.rpcSize = rpcSize;
    }

    public int getSendMqCorePoolThreadNum() {
        return sendMqCorePoolThreadNum;
    }

    public void setSendMqCorePoolThreadNum(int sendMqCorePoolThreadNum) {
        this.sendMqCorePoolThreadNum = sendMqCorePoolThreadNum;
    }

    public int getSendMqMaxPoolThreadNum() {
        return sendMqMaxPoolThreadNum;
    }

    public void setSendMqMaxPoolThreadNum(int sendMqMaxPoolThreadNum) {
        this.sendMqMaxPoolThreadNum = sendMqMaxPoolThreadNum;
    }

    public int getSendMqQueueSize() {
        return sendMqQueueSize;
    }

    public void setSendMqQueueSize(int sendMqQueueSize) {
        this.sendMqQueueSize = sendMqQueueSize;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getPrefixUrl() {
        return prefixUrl;
    }

    public void setPrefixUrl(String prefixUrl) {
        this.prefixUrl = prefixUrl;
    }
}
