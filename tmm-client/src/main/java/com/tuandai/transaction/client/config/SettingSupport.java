package com.tuandai.transaction.client.config;

import com.mchange.v2.util.CollectionUtils;
import com.tuandai.transaction.client.bo.MqType;
import com.tuandai.transaction.client.bo.RabbitAddress;
import com.tuandai.transaction.client.utils.CacheMapFileUtils;
import com.tuandai.transaction.client.utils.ConstantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;
import java.util.stream.Collectors;

public class SettingSupport {

    private static final Logger logger = LoggerFactory.getLogger(SettingSupport.class);

    private static Random random = new Random();

    // 随机序号最大值
    private final static Integer number = 100;

    private static FileLock lock = null;

    public static ApplicationContext context;

    public static SettingSupport setting;

    private static File local_rpc_dir = null;

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

    @Autowired
    private TMMRabbitProperties tmmRabbitMqProperties;

    public static String getPrefixUrl() {
        return SettingHandler.getPrefixUrl();
    }

    public static String getServerName() {
        return SettingHandler.getServerName();
    }

    public static long getRpcSize() {
        return SettingHandler.getRpcSize();
    }

    public static long getCheckIntervalTime() {
        return SettingHandler.getCheckIntervalTime();
    }

    public static Map<String, RabbitAddress> getRabbitAddressMap() {
        return SettingHandler.getRabbitAddressMap();
    }

    public static File getRpcDir() {
        // 单例模式
        return local_rpc_dir;
    }

    public static String getRpcDirCanonicalPath() {
        String str;
        try {
            str = local_rpc_dir.getCanonicalPath();
        } catch (Exception e) {
            throw new IllegalArgumentException("不能获得全路径名！");
        }
        return str;
    }

    public static MqType getMqType() {
        return SettingHandler.getMqType();
    }

    public static int getBatchSize() {
        return SettingHandler.getBatchSize();
    }

    public static int getSendMqCorePoolThreadNum() {
        return SettingHandler.getSendMqCorePoolThreadNum();
    }

    public static int getSendMqMaxPoolThreadNum() {
        return SettingHandler.getSendMqMaxPoolThreadNum();
    }

    public static int getSendMqQueueSize() {
        return SettingHandler.getSendMqQueueSize();
    }

    public static void init(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        setting = context.getBean(SettingSupport.class);
        try {
            // 定位目录
            locationDirPath();
        } catch (IllegalArgumentException e) {
            logger.error("locationDirPath error ...", e);
            throw new BootstrapException(e.getMessage());
        } catch (Exception e) {
            logger.error("locationDirPath error ...", e);
            throw new BootstrapException("locationDirPath error ...");
        }
    }

    // 销毁
    public static void close() throws IOException {
        // 释放锁
        if (lock != null && lock.isValid()) {
            lock.release();
        }
    }

    private static void locationDirPath() throws IOException {
        // 先初始化一个待生产数组
        Set<Integer> presetNumber = new HashSet<>(number);
        for (int i = 0; i < number; i++) {
            presetNumber.add(i);
        }

        // 记录遍历过的文件夹
        Set<String> fileDirCache = new HashSet<>();
        boolean isTryLock = false;
        String dirName = getServerName().toLowerCase().replace("-", "_") + "_";

        File locationFile = null;
        while (!isTryLock) {
            // 1.先筛选出当前服务名开头的目录文件 serviceName_0,serviceName_1 ....
            File[] candidateDirs = CacheMapFileUtils.searchFile(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(dirName);
                }
            }, SettingHandler.getRpcDir());

            // 当前时刻候选文件
            Map<String, File> candidateMap = new HashMap<>();
            Set<Integer> existedNumber = new HashSet<>(number);
            for (File file : candidateDirs) {
                String[] sets = file.getName().split("_");
                existedNumber.add(Integer.valueOf(sets[sets.length - 1]));
                if (!fileDirCache.contains(file.getCanonicalPath())) {
                    candidateMap.put(file.getCanonicalPath(), file);
                }
            }

            if (candidateMap.size() == 0) {
                // 2.没有该目录则创建该目录，创建规则为：serviceName_0,serviceName_1 ...,后面数组随机2位数
                presetNumber.removeAll(existedNumber);
                Iterator<Integer> it = presetNumber.iterator();
                if (!it.hasNext()) {
                    // 该实例数启动已经超过{number}，请设置更大范围的随机数生产范围
                    logger.error("please set random > {}  ...", number);
                    throw new IllegalArgumentException("please set random > nubmer ...");
                }
                Integer confirmNumber = it.next();
                String fileName = dirName + confirmNumber;
                File file = new File(SettingHandler.getRpcDir() + "/" + fileName);
                file.mkdirs();
                locationFile = file;
            } else {
                // 3.如果存在则随机取最新那个
                long lastModified = 0;
                File lastFile = null;
                for (Map.Entry<String, File> entry : candidateMap.entrySet()) {
                    File file = entry.getValue();
                    if (file.lastModified() > lastModified) {
                        lastModified = file.lastModified();
                        lastFile = file;
                    }
                }
                locationFile = lastFile;
            }

            String locationDir = locationFile.getCanonicalPath();
            /**
             * 记录该文件已经被尝试获取过，注意：这个地方可能会有逻辑瑕疵以及bug，假如另一个应用放弃锁，
             * 但这个应用已经判断过该锁则会去创建一个新的文件，最好的做法是把该缓存放到zk，或者redis上.
             * 但考虑到实际情况里面这很少，所以暂时忽略该情况
             */
            // 4.定位加锁文件，尝试加锁
            fileDirCache.add(locationDir);
            String checkPointFileLockName = locationDir + "/" + ConstantUtils.checkPointFileLock;
            File checkPointFileLock = new File(checkPointFileLockName);
            checkPointFileLock.createNewFile();
            RandomAccessFile randomAccessFile = new RandomAccessFile(checkPointFileLock, "rw");
            FileChannel fileChannel = randomAccessFile.getChannel();

            try {
                FileLock fileLock = fileChannel.tryLock();
                if (fileLock != null && fileLock.isValid()) {
                    // 该应用获取到本文件的锁
                    isTryLock = true;
                    local_rpc_dir = locationFile;
                    lock = fileLock;
                }
            } catch (Exception e) {
                logger.info("checkPointFileLock fail , try again...");
            }
        }
    }


    /**
     * Setting类的变量处理类
     */
    static class SettingHandler {

        public static String getPrefixUrl() {
            String prefixUrl = SettingSupport.setting.prefixUrl;
            logger.debug("加载prefixUrl:" + prefixUrl);
            return prefixUrl;
        }

        public static String getServerName() {
            String serverName = SettingSupport.setting.serverName;
            logger.debug("加载serverName:" + serverName);
            return serverName;
        }

        public static MqType getMqType() {
            MqType mqType = MqType.findByDes(SettingSupport.setting.mqType);
            logger.debug("加载Mqtype:" + mqType);
            return mqType;
        }

        public static int getBatchSize() {
            int batchSize = SettingSupport.setting.batchSize;
            logger.debug("加载batchSize:" + batchSize);
            return batchSize;
        }

        public static File getRpcDir() {
            String rpcDirName = SettingSupport.setting.rpcDirName;
            if (StringUtils.isEmpty(rpcDirName)) {
                rpcDirName = ConstantUtils.DEFAULT_RPC_PATH;
            }
            logger.debug("加载配置rpcDir:" + rpcDirName);

            File rpcDir = new File(rpcDirName);
            // 检查目录权限
            if (! rpcDir.exists()) {
                if (! rpcDir.mkdirs()) {
                    throw new IllegalArgumentException("begin.map.directory is not a directory");
                }
            } else if (!rpcDir.canWrite()) {
                throw new IllegalArgumentException("begin.map.directory can not write");
            }
            return rpcDir;
        }

        public static Map<String, RabbitAddress> getRabbitAddressMap() {
            TMMRabbitProperties tMMRabbitProperties = SettingSupport.setting.tmmRabbitMqProperties;
            Map<String, RabbitAddress> map = tMMRabbitProperties.getRabbitAddressMap();
            logger.debug("加载rabbitmq:" + map);
            return map;
        }

        public static long getCheckIntervalTime() {
            long time =  SettingSupport.setting.checkIntervalTime;
            logger.debug("加载CheckIntervalTime：" + time);
            return time;
        }

        public static long getRpcSize() {
            long rpcSize = SettingSupport.setting.rpcSize;
            logger.debug("加载rpcSize:" + rpcSize);
            return rpcSize;
        }

        public static int getSendMqCorePoolThreadNum() {
            int coreSize = SettingSupport.setting.sendMqCorePoolThreadNum;
            logger.debug("加载coreSize:" + coreSize);
            return coreSize;
        }

        public static int getSendMqMaxPoolThreadNum() {
            int maxSize = SettingSupport.setting.sendMqMaxPoolThreadNum;
            logger.debug("加载maxSize:" + maxSize);
            return maxSize;
        }

        public static int getSendMqQueueSize() {
            int queueSize = SettingSupport.setting.sendMqQueueSize;
            logger.debug("加载maxSize:" + queueSize);
            return queueSize;
        }
    }
}
