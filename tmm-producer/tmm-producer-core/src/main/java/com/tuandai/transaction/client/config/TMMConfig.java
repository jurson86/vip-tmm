package com.tuandai.transaction.client.config;

/**
 * TMM 基本配置参数
 */
public class TMMConfig {

    /**
     * Mq 的类型， 必填
     */
    protected String mqType;

    /**
     * 每批次发送处理日志条数
     */
    protected int batchSize = 2000;

    /**
     * 日志文件存放路径，选填字段，默认使用用户的服务部署目录
     */
    protected String rpcDirName = "";

    /**
     * 判定为check数据的时间间隔， 选填
     */
    protected long checkIntervalTime = 60000;

    /**
     * rpc文件的切割大小100 * 1024 *1024L = 104857600， 选填
     */
    protected long rpcSize = 104857600;

    /**
     * 发送mq线程的核心线程数， 选填
     */
    protected int sendMqCorePoolThreadNum = 5;

    /**
     * 发送mq线程的最大线程数， 选填
     */
    protected int sendMqMaxPoolThreadNum = 200;

    /**
     * 发送mq线程的队列大小， 5000， 选填
     */
    protected int sendMqQueueSize = 5000;

    /**
     * 服务名， 必填字段，服务的服务名（eurkea 上面注册的服务名，用于发现对服务进行调用）
     */
    protected String serverName;

    /**
     * 服务web容器的根路径
     */
    protected String prefixUrl;

    /**
     * mqMap 重试次数，超过此次数则进入err.log
     */
    protected Integer retryCount = 3;

    /**
     * 是否采用tmm 自己封装的消息体，兼容老版本，新版本使用false
     */
    protected boolean isTmmMessage = false;

    public boolean isTmmMessage() {
        return isTmmMessage;
    }

    public void setTmmMessage(boolean tmmMessage) {
        isTmmMessage = tmmMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
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
