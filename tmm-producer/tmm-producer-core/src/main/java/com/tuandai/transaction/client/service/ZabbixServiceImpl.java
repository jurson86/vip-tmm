package com.tuandai.transaction.client.service;

import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.utils.ConstantUtils;
import com.tuandai.transaction.client.utils.FileNameSelector;
import com.tuandai.transaction.client.service.inf.ZabbixService;
import com.tuandai.transaction.client.utils.CacheMapFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ZabbixServiceImpl implements ZabbixService {

    private static final Logger logger = LoggerFactory.getLogger(ZabbixServiceImpl.class);

    public ZabbixServiceImpl() {
    }

    @Override
    public long rpcCountMonitor() {
        File[] files = CacheMapFileUtils.searchFile(new FileNameSelector("rpc"), SettingSupport.getRpcDir());
        return  files == null ? 0: files.length;
    }

    @Override
    public long doneCountMonitor() {
        File[] files = CacheMapFileUtils.searchFile(new FileNameSelector("done"), SettingSupport.getRpcDir());
        return files == null ? 0: files.length;
    }

    @Override
    public long errorLogCountMonitor() {
        try {
            File file = new File(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.errorLogFile);
            if (!file.exists()) {
                return 0;
            }
            return Files.lines(Paths.get(file.getPath())).count();
        } catch (IOException e) {
            logger.error("tmm 统计errorLog 失败..");
        }
        return 0;
    }

    @Override
    public long errorMapCountMonitor() {
        try {
            File file = new File(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.errorMapFile);
            if (!file.exists()) {
                return 0;
            }
            return Files.lines(Paths.get(file.getPath())).count();
        } catch (IOException e) {
            logger.error("tmm 统计errorMap 失败..");
        }
        return 0;
    }

    @Override
    public Map<String, Long> allMonitor() {
        Map<String, Long> result = new HashMap<>();
        long done = doneCountMonitor();
        long rpc = rpcCountMonitor();
        long errorLog = errorLogCountMonitor();
        long errorMap = errorMapCountMonitor();
        result.put("rpc", rpc);
        result.put("done", done);
        result.put("error.log", errorLog);
        result.put("error.map", errorMap);
        return result;
    }

}
