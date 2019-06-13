package com.tuandai.transaction.client.service.inf;

import java.util.Map;

/**
 * zabbix监控类
 */
public interface ZabbixService {

    /**
     * 监控 rpc文件个数
     */
    int rpcCountMonitor();

    /**
     * 监控done文件个数
     */
    int doneCountMonitor();


    Map<String, Integer> allMonitor();

}
