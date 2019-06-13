package com.tuandai.transaction.client.config;

import com.tuandai.transaction.client.bo.MqType;
import com.tuandai.transaction.client.mq.inf.MqService;
import com.tuandai.transaction.client.service.inf.TMMEventListener;
import com.tuandai.transaction.client.utils.CacheMapFileUtils;
import com.tuandai.transaction.client.utils.CollectionUtils;
import com.tuandai.transaction.client.utils.ConstantUtils;
import com.tuandai.transaction.client.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;

public class SettingSupport {

    private static final Logger logger = LoggerFactory.getLogger(SettingSupport.class);

    private static TMMConfig tmmConfig;

    /**
     * 随机序号最大值, 文件夹序号在100以内产生
     */
    private final static Integer number = 100;

    /**
     * 文件锁，跨 jvm 文件锁
     */
    private static FileLock lock = null;

    // MqService服务
    private static List<MqService> mqServices = new ArrayList<>();

    // TMMEventListener
    private static List<TMMEventListener> tMMEventListeners = new ArrayList<>();

    // rpc 文件位置
    private static File local_rpc_dir = null;

    /**
     * SettingSupport 的 初始化方法， 所有的SettingSupport的静态变量必须要在调用了init 方法后才能使用
     */
    public static void init(TMMConfig tmmConfig) {
        // 设置配置环境静态变量
        SettingSupport.tmmConfig = tmmConfig;
        try {
            // 定位目录， 设置 local_rpc_dir 变量，并记录 文件锁 lock变量
            locationDirPath();
        } catch (IllegalArgumentException e) {
            logger.error("locationDirPath error ...", e);
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            logger.error("locationDirPath error ...", e);
            throw new IllegalArgumentException("locationDirPath error ...");
        }
        // 加载扩展类
        loadMqServices();
        loadTMMEventListeners();
    }

    public static TMMConfig getTmmConfig() {
        return tmmConfig;
    }

    /**
     * 销毁 文件锁
     */
    public static void close() throws IOException {
        // 释放锁
        if (lock != null && lock.isValid()) {
            lock.release();
        }
    }

    public static String getPrefixUrl() {
        return tmmConfig.prefixUrl;
    }

    public static long getRpcSize() {
        return tmmConfig.rpcSize;
    }

    public static long getCheckIntervalTime() {
        return tmmConfig.checkIntervalTime;
    }

    public static MqType getMqType() {
        return MqType.findByDes(tmmConfig.mqType);
    }

    public static int getBatchSize() {
        return tmmConfig.batchSize;
    }

    public static int getSendMqCorePoolThreadNum() {
        return tmmConfig.sendMqCorePoolThreadNum;
    }

    public static int getSendMqMaxPoolThreadNum() {
        return tmmConfig.sendMqMaxPoolThreadNum;
    }

    public static int getRetryCount() {
        return tmmConfig.retryCount;
    }

    public static int getSendMqQueueSize() {
        return tmmConfig.sendMqQueueSize;
    }

    /**
     *  最新版本统一显示设置 false 做值，目前使用true兼容老版本
     */
    public static boolean isTmmMessage() {
        return tmmConfig.isTmmMessage;
    }

    /**
     * 最终 tmm 自己确定的 rpc 目录路径（改路径是最终rpc 文件所在的目录）
     */
    public static File getRpcDir() {
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

    /**
     * 用户设置的 rpc 目录路径（这个路径不是最终rpc所在的路径，还有一次目录）
     * @return
     */
    public static File getPreRpcDir() {
        String rpcDirName = tmmConfig.rpcDirName;
        if (StringUtils.isEmpty(rpcDirName)) {
            rpcDirName = ConstantUtils.DEFAULT_RPC_PATH;
        }
        logger.debug("加载配置rpcDir:" + rpcDirName);

        File rpcDir = new File(rpcDirName);
        // 检查目录权限
        if (!rpcDir.exists()) {
            if (! rpcDir.mkdirs()) {
                throw new IllegalArgumentException("begin.map.directory is not a directory");
            }
        } else if (!rpcDir.canWrite()) {
            throw new IllegalArgumentException("begin.map.directory can not write");
        }
        return rpcDir;
    }

    public static List<MqService> getMqServices() {
        if (CollectionUtils.isEmpty(mqServices)) {
            throw new IllegalArgumentException("mqServices 为空, 请先加载 MqService 配置文件再调用....");
        }
        return mqServices;
    }

    public static List<TMMEventListener> getTMMEventListeners() {
        if (CollectionUtils.isEmpty(tMMEventListeners)) {
            throw new IllegalArgumentException("tMMEventListeners 为空, 请先加载 TMMEventListener 配置文件再调用....");
        }
        return tMMEventListeners;
    }

    private static void loadTMMEventListeners() {
        ServiceLoader<TMMEventListener> loadedParsers = ServiceLoader.load(TMMEventListener.class);
        Iterator<TMMEventListener> driversIterator = loadedParsers.iterator();
        try{
            while(driversIterator.hasNext()) {
                TMMEventListener tMMEventListener = driversIterator.next();
                tMMEventListeners.add(tMMEventListener);
            }
        } catch(Throwable t) {
            logger.error("tmm 加载MqService扩展类失败,请检查resources/META-INF/services/com.tuandai.transaction.client.mq.inf.TMMEventListener 文件");
        }
    }

    private static void loadMqServices() {
        ServiceLoader<MqService> loadedParsers = ServiceLoader.load(MqService.class);
        Iterator<MqService> driversIterator = loadedParsers.iterator();
        try{
            while(driversIterator.hasNext()) {
                MqService mqService = driversIterator.next();
                mqServices.add(mqService);
            }
        } catch(Throwable t) {
            logger.error("tmm 加载MqService扩展类失败,请检查resources/META-INF/services/com.tuandai.transaction.client.mq.inf.MqService 文件");
        }
    }

    public static String getServerName() {
        return tmmConfig.serverName;
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
            }, getPreRpcDir());

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
                File file = new File(getPreRpcDir() + "/" + fileName);
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

}
