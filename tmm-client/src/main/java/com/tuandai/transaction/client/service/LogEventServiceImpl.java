package com.tuandai.transaction.client.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.utils.ConstantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.utils.FileNameSelector;
import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.service.inf.LogEventService;
import com.tuandai.transaction.client.utils.CacheMapFileUtils;

// 日志读写器
public class LogEventServiceImpl implements LogEventService {

    private static final Logger logger = LoggerFactory.getLogger(LogEventServiceImpl.class);
    /**
     * 最大文件长度
     */
    public static Long DEFAULT_MAX_FILE_SIZE = 100 * 1024 *1024L;

    /**
     * 文件结束符
     */
    public static final String RPC_FILE_END_MARK = "RPC_FILE_END";

    /**
     * 文件后缀
     */
    private static final String rpcFileExtension = ".rpc";

    /**
     * 日志事件记录文件
     */
    private static File file = null;

    /**
     * 定位器记录文件
     */
    private File checkPointFile = null;

    /**
     * 定位器指示记录
     */
    private CheckPoint checkPoint = null;

    /**
     *  上一次循环的checkpoint主要是为了防止多次读写文件
     */
    private CheckPoint originalCheckPoint = null;

    private static final String checkPointSplit = "\t";

    private volatile boolean isEnd = false;

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public LogEventServiceImpl() {
        checkPointFile =  new File(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.checkPointFileName);
        checkPoint = new CheckPoint();
        originalCheckPoint = new CheckPoint();
        DEFAULT_MAX_FILE_SIZE = SettingSupport.getRpcSize();
    }

    // 写日志，线程安全
    @Override
    public synchronized void writeLogEvent(EventDefinition eventDefinition) {
        // 定位文件，本次写的是哪个文件
        locationFile();
        CacheMapFileUtils.writeDataToFile(file, true, new CacheMapFileUtils.WriteDataToFileProcess() {
            @Override
            protected void process(OutputStreamWriter writer) throws Exception {
                // 写日志文件
                writer.append(JSONObject.toJSONString(eventDefinition) + "\n");
            }
        });
    }

    private void locationFile() {
        if (file == null) {
            File[] files = CacheMapFileUtils.searchFile(new FileNameSelector("rpc"), SettingSupport.getRpcDir());
            file = CacheMapFileUtils.locationLastModifyFile(files, true);
        }
        if (file != null && file.length() > DEFAULT_MAX_FILE_SIZE) {
            // 添加结束符
            CacheMapFileUtils.writeDataToFile(file, true, new CacheMapFileUtils.WriteDataToFileProcess() {
                @Override
                protected void process(OutputStreamWriter writer) throws Exception {
                    writer.append(RPC_FILE_END_MARK);
                }
            });
            file = null;
        }
        // 创建新文件
        if (file == null) {
            String rpcCurrentFileName = System.nanoTime() + rpcFileExtension;
            String fileName = SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + rpcCurrentFileName;
            file = CacheMapFileUtils.createNewFileName(fileName);
        }
    }


    @Override
    public List<EventDefinition> readLogEvent(Integer size) {
        // 读取checkpoint指向的文件内容, checkpoint 随内容增长
        return doReadLogEvent(size);
    }

    @Override
    public boolean persistentCheckpoint() throws IOException {
        if (isEnd) {
            logger.debug("LogEventServiceImpl 处理文件结束，更名为.done,文件名为:" + checkPoint.checkFile.getName());
            checkPoint.checkFile.renameTo(new File(checkPoint.checkFile.getCanonicalPath() + ".done"));
            // 定位新文件
            File tmp_file = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), SettingSupport.getRpcDir(),
                    false);
            logger.debug("LogEventServiceImpl 定位新的checkpointFile:" + (tmp_file == null ? "不存在" : tmp_file.getName()));
            checkPoint.checkFile = tmp_file;
            checkPoint.point = 0;
            originalCheckPoint.checkFile = tmp_file;
            originalCheckPoint.point = 0;
        }

        // 持久化checkpoint，如果内容没有被更改则无需持久化 或者  如果checkpoint文件不存在则创建空的checkpoint 文件，并且赋值
        if (!checkPoint.isEquals(originalCheckPoint) || !checkPointFile.exists()) {
            logger.debug("LogEventServiceImpl 持久化checkpointFile:" + (checkPoint.checkFile == null ? "NULL" :
                    checkPoint.checkFile.getCanonicalPath() + checkPointSplit + checkPoint.point));
            CacheMapFileUtils.writeDataToFile(checkPointFile, false, new CacheMapFileUtils.WriteDataToFileProcess() {
                @Override
                protected void process(OutputStreamWriter writer) throws Exception {
                    if (checkPoint.checkFile == null) {
                        return;
                    }
                    writer.write(checkPoint.checkFile.getCanonicalPath() + checkPointSplit + checkPoint.point); // XXX/XXX/XXX.rpc | 23232

                    // 更新原checkpoint的值
                    originalCheckPoint.checkFile = checkPoint.checkFile;
                    originalCheckPoint.point = checkPoint.point;
                }
            });
            return true;
        }
        return false;
    }

    // 读取Rpc（checkPoint.checkFile）文件
    private List<EventDefinition> doReadLogEvent(Integer size) {
        isEnd = false;
        List<EventDefinition> list = new ArrayList<>();
        // 文件不存在
        if (checkPoint.checkFile == null || !checkPoint.checkFile.exists()) {
            checkPoint.checkFile = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), SettingSupport.getRpcDir(), false);
            checkPoint.point = 0;
            if (checkPoint.checkFile == null) {
                return list;
            }
        }

        // 读取文件
        CacheMapFileUtils.readDataToMap(checkPoint.checkFile, new CacheMapFileUtils.ReadDataToMapProcess() {
            @Override
            protected boolean preProcess(BufferedReader bufferedReader) throws Exception {
                bufferedReader.skip((long) checkPoint.point);
                return true;
            }

            @Override
            protected boolean process(String keyValueStr) throws Exception {
                if (keyValueStr.equals(RPC_FILE_END_MARK)) {
                    isEnd = true;
                    return false;
                } else {
                    EventDefinition event = JSONObject.parseObject(keyValueStr, EventDefinition.class);
                    if (list.size() >= size) {
                        return false;
                    }
                    list.add(event);
                    // 更新字符数
                    checkPoint.point = checkPoint.point + keyValueStr.length() + 1;
                    return true;
                }
            }
        });
        return list;
    }

    // 加载checkpoint文件内容
    @Override
    public void loadCheckpoint() {
        try {
            // 读取checkPoint的值
            CacheMapFileUtils.readDataToMap(checkPointFile, new CacheMapFileUtils.ReadDataToMapProcess() {

                @Override
                protected boolean process(String keyValueStr) throws Exception {
                    if (!StringUtils.isEmpty(keyValueStr)) {
                        File checkFile = new File(keyValueStr.split(checkPointSplit)[0]);
                        Integer checkPointCount = Integer.valueOf(keyValueStr.split(checkPointSplit)[1]);
                        checkPoint.checkFile = checkFile;
                        checkPoint.point = checkPointCount;
                        originalCheckPoint.checkFile = checkFile;
                        originalCheckPoint.point = checkPointCount;
                        logger.debug("加载checkPoint，loadCheckpoint()，checkPoint.checkFile："
                                + (checkPoint.checkFile == null ?  null: checkPoint.checkFile.getName()) +
                                " ,checkPoint.point:" + checkPoint.point);
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            logger.error("不能获得全路径名！");
            throw new IllegalArgumentException("不能获得全路径名！", e);
        }
    }

    @Override
    public void resetCheckpoint() {
        checkPoint.checkFile = originalCheckPoint.checkFile;
        checkPoint.point = originalCheckPoint.point;
        logger.debug("发送消息遇到未知错误，整个循环重置，LogEventServiceImpl.resetCheckpoint()， originalCheckPoint.checkFile:"
                + (originalCheckPoint.checkFile == null ? null : originalCheckPoint.checkFile.getName()) +
                ", originalCheckPoint.point" + originalCheckPoint.point);
    }

    @Override
    public void removeFile(String fileName) {
        // fileName
        CacheMapFileUtils.removeFileName(fileName);
    }

    class CheckPoint {
        /**
         * 当前读取到的文件
         */
        public  File checkFile = null;

        /**
         * 行数
         */
        public  int point = 0;


        public boolean isEquals(CheckPoint other) {
            // 二者都是空，则相等
            if (this.checkFile == null && other.checkFile == null && this.point == 0 && other.point == 0) {
                return true;
            }

            // 当前为空，原来不为空则不等 或者 当前不为空，原来为空则不等
            if ((this.checkFile == null && other.checkFile != null) || (this.checkFile != null && other.checkFile == null)) {
                return false;
            }

            // 二者都不为空则比较二者的具体值
            return this.checkFile.equals(other.checkFile) && this.point == other.point;
        }

    }

    public CheckPoint getCheckPoint() {
        return checkPoint;
    }

    public void setCheckPoint(CheckPoint checkPoint) {
        this.checkPoint = checkPoint;
    }
}
