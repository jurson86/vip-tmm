package com.tuandai.transaction.client.service;

import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.utils.FileNameSelector;
import com.tuandai.transaction.client.service.inf.ZabbixService;
import com.tuandai.transaction.client.utils.CacheMapFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ZabbixServiceImpl implements ZabbixService {

    private static final Logger logger = LoggerFactory.getLogger(ZabbixServiceImpl.class);

    public ZabbixServiceImpl() {
    }

    @Override
    public int rpcCountMonitor() {
        File[] files = CacheMapFileUtils.searchFile(new FileNameSelector("rpc"), SettingSupport.getRpcDir());
        return  files == null ? 0: files.length;
    }

    @Override
    public int doneCountMonitor() {
        File[] files = CacheMapFileUtils.searchFile(new FileNameSelector("done"), SettingSupport.getRpcDir());
        return files == null ? 0: files.length;
    }

    @Override
    public Map<String, Integer> allMonitor() {
        Map<String, Integer> result = new HashMap<>();
        int done = doneCountMonitor();
        int rpc = rpcCountMonitor();
        result.put("rpc", rpc);
        result.put("done", done);
        return result;
    }

}
