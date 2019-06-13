package com.tuandai.transaction.client.utils;

public class ConstantUtils {

    public static final String HOST = "${spring.mq.host}";

    public static final String PORT = "${spring.mq.port}";

    public static final String ADDRESSES = "${spring.mq.addresses}";

    public static final String USER_NAME = "${spring.mq.username}";

    public static final String PASS_WORD = "${spring.mq.password}";


    public static final String CHECK_INTERVAL_TIME = "${spring.tmmService.check.interval.time}";

    public static final String mapFile = "begin.map";

    public static final String mapCheckFile = "begin.map.check";

    public static final String mapEndFile = "end.map";

    public static final String mapMqFile = "mq.map";


    public static final String monitor_time = "${spring.tmmService.monitor.interval.time}";

    /**
     * 定位器文件名
     */
    public static String checkPointFileName = "checkpoint";

    public static String checkPointFileLock = "checkpoint.lock";

    public static final String DEFAULT_RPC_PATH = System.getProperty("user.dir") + "/rpcPath";

    // RPC文件大小peizhi
    public static final String RPC_SIZE = "${rpc.size}";

    // MQType
    public static final String MQ_TYPE = "${mq.type}";

    // check交换机
    public static final String CHECK_EXCHANGE = "tmm_check";

    // start交换机
    public static final String START_EXCHANGE = "start_exchange";

    // check队列
    public static final String CHECK_QUEUE = "tmm-check-queue";

    // start队列
    public static final String START_QUEUE = "tmm-start-queue";

    // tmm vhost
    public static final String TMM_VHOST = "/tmm_vhost";

    // fanout 交换机类型
    public static final String FANOUT = "fanout";

}
