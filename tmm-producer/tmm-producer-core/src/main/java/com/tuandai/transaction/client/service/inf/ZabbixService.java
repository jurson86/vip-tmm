package com.tuandai.transaction.client.service.inf;

import java.util.Map;

/**
 * zabbix监控类
 */
public interface ZabbixService {

    /**
     * 监控 rpc文件个数
     */
    long rpcCountMonitor();

    /**
     * 监控done文件个数
     */
    long doneCountMonitor();

    /**
     * 监控 error.log 的文件行数
     */
    long errorLogCountMonitor();

    long errorMapCountMonitor();


    Map<String, Long> allMonitor();

}
