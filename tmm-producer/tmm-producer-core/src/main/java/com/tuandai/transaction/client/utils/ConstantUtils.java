package com.tuandai.transaction.client.utils;

public class ConstantUtils {

    public static final String mapFile = "begin.map";

    public static final String mapCheckFile = "begin.map.check";

    public static final String mapEndFile = "end.map";

    public static final String mapMqFile = "mq.map";

    public static final String errorLogFile = "error.log";

    public static final String errorMapFile = "error.map";


    /**
     * 定位器文件名
     */
    public static String checkPointFileName = "checkpoint";

    public static String checkPointFileLock = "checkpoint.lock";

    public static final String DEFAULT_RPC_PATH = System.getProperty("user.dir") + "/rpcPath";

    public static final String TRACE_ID_NAME = "X-B3-TraceId";

    public static final String SPAN_ID_NAME = "X-B3-SpanId";

}
