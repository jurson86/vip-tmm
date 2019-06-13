package com.tuandai.transaction.client.service;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.config.SettingSupport;
import com.tuandai.transaction.client.service.inf.EventDefinitionRegistry;
import com.tuandai.transaction.client.utils.Assert;
import com.tuandai.transaction.client.utils.CacheMapFileUtils;
import com.tuandai.transaction.client.utils.CollectionUtils;
import com.tuandai.transaction.client.utils.ConstantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的事件注册器
 */
public class SimpleEventDefinitionRegistry implements EventDefinitionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SimpleEventDefinitionRegistry.class);

    /**
     *  保存了所有的开始处理事件
     */
    private final ConcurrentHashMap<String, EventDefinition> beginMap = new ConcurrentHashMap<String, EventDefinition>();

    /**
     *  保存了所有的结束处理事件
     */
    private final  ConcurrentHashMap<String, EventDefinition> endMap = new ConcurrentHashMap<String, EventDefinition>();

    /**
     *  保存了Mq
     */
    private final ConcurrentHashMap<String, EventDefinition> mqMap = new ConcurrentHashMap<String, EventDefinition>();

    /**
     * 保存需要check的事件
     */
    private final ConcurrentHashMap<String, EventDefinition> checkMap = new ConcurrentHashMap<String, EventDefinition>();

    /**
     * beginMap 临时持久化实例
     */
    private File _begin_tmp_file;

    /**
     * beginMap 持久化实例
     */
    private File _begin_file;

    /**
     * MQ map
     */
    private File _mq_file;

    /**
     * resource beginMap数据,记录上一次文件的数据，用于优化文件的读取和删除操作
     */
    private HashMap<String, EventDefinition> originalBeginMap = new HashMap<>();

    /**
     * resource endMap数据,记录上一次文件的数据，用于优化文件的读取和删除操作
     */
    private HashMap<String, EventDefinition> originalEndMap = new HashMap<>();

    /**
     * resource mq数据
     */
    private HashMap<String, EventDefinition> originalMqMap = new HashMap<>();

    /**
     * check 持久化实例
     */
    private File _check_file;

    /**
     * endMap 临时持久化实例
     */
    private File _end_tmp_file;

    private File _end_file;

    private File _mq_tmp_file;

    /**
     * 缓存当前mqMap 里面各个消息的重试次数
     */
    protected Map<String, Integer> retryMap = new ConcurrentHashMap<>();

    private File errorFile;

    public SimpleEventDefinitionRegistry() {
    }

    public void init() {
        this._begin_tmp_file = new File(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.mapFile + ".tmp");
        this._begin_file = new File(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.mapFile);
        this._check_file = new File(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.mapCheckFile);
        this._end_tmp_file = new File(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.mapEndFile + ".tmp");
        this._end_file = new File(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.mapEndFile);
        this._mq_tmp_file = new File(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.mapMqFile + ".tmp");
        this._mq_file = new File(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.mapMqFile);
        this.errorFile = new File(SettingSupport.getRpcDirCanonicalPath() + File.separatorChar + ConstantUtils.errorMapFile);
    }

    @Override
    public void registerEventDefinition(String eventName, EventDefinition eventDefinition) {
        Assert.hasText(eventName, "事件名必须不为空！");
        Assert.notNull(eventDefinition, "事件定义不能为空！");
        Assert.notNull(eventDefinition.getEventType(), "事件类型不能为空！");
        if (eventDefinition.getEventType() == EventDefinition.EventType.BEGIN) {
            beginMap.put(eventName, eventDefinition);
        } else if(eventDefinition.getEventType() == EventDefinition.EventType.END) {
            endMap.put(eventName, eventDefinition);
        } else if (eventDefinition.getEventType() == EventDefinition.EventType.MQ) {
            eventDefinition.setGoMapTime(System.currentTimeMillis());
            mqMap.put(eventName, eventDefinition);
            retryMap.put(eventName, 0);
        }
    }

    @Override
    public EventDefinition removeEventDefinition(String eventName, EventDefinition.EventType eventType) {
        Assert.hasText(eventName, "事件名必须不为空！");
        Assert.notNull(eventType, "事件类型不能为空！");
        if (eventType == EventDefinition.EventType.BEGIN) {
            return beginMap.remove(eventName);
        } else if (eventType == EventDefinition.EventType.END)  {
            return  endMap.remove(eventName);
        } else if (eventType == EventDefinition.EventType.MQ)  {
            retryMap.remove(eventName);
            return mqMap.remove(eventName);
        }
        return null;
    }

    @Override
    public EventDefinition getEventDefinition(String eventName, EventDefinition.EventType eventType) {
        Assert.hasText(eventName, "事件名必须不为空！");
        Assert.notNull(eventType, "事件类型不能为空！");
        EventDefinition bd = null;
        if (eventType == EventDefinition.EventType.BEGIN) {
            bd = beginMap.get(eventName);
        } else if (eventType == EventDefinition.EventType.END) {
            bd = endMap.get(eventName);
        } else if (eventType == EventDefinition.EventType.MQ) {
            bd = mqMap.get(eventName);
        }
        return bd;
    }

    @Override
    public boolean containsEventDefinition(String eventName, EventDefinition.EventType eventType) {
        Assert.hasText(eventName, "事件名必须不为空！");
        Assert.notNull(eventType, "事件类型不能为空！");
        if (eventType == EventDefinition.EventType.BEGIN) {
            return beginMap.containsKey(eventName);
        } else if (eventType == EventDefinition.EventType.END) {
            return endMap.containsKey(eventName);
        } else if (eventType == EventDefinition.EventType.MQ) {
            return mqMap.containsKey(eventName);
        }
        return false;
    }

    @Override
    public void persistentEventDefinition() {
        // 比较当前内存的beginMap和文件内的数据是否一致，一致则不需要记录
        if (!com.tuandai.transaction.client.utils.CollectionUtils.isMapEqual(beginMap, originalBeginMap)) {
            logger.debug("SimpleEventDefinitionRegistry持久化beginMap数据," + beginMap.keySet());
            // 持久化当前beginMap数据
            writeDataToFile(_begin_tmp_file, _begin_file, beginMap);
            originalBeginMap.clear();
            originalBeginMap.putAll(beginMap);
        }

        // 持久化check数据
        if (checkMap.size() != 0) {
            logger.debug("SimpleEventDefinitionRegistry持久化checkMap数据," + checkMap.keySet());
            CacheMapFileUtils.writeDataToFile(_check_file, true, new CacheMapFileUtils.WriteDataToFileProcess() {
                @Override
                public void process(OutputStreamWriter writer) throws Exception {
                    Iterator<Map.Entry<String, EventDefinition>> iterator = checkMap.entrySet().iterator();
                    while(iterator.hasNext()) {
                        Map.Entry<String, EventDefinition> entry = iterator.next();
                        EventDefinition eventDefinition = entry.getValue();
                        writer.append(JSONObject.toJSONString(eventDefinition) + "\n");
                    }
                }
            });
        }

        // 持久化结束map数据
        if (!com.tuandai.transaction.client.utils.CollectionUtils.isMapEqual(endMap, originalEndMap)) {
            logger.debug("SimpleEventDefinitionRegistry持久化endMap数据," + endMap.keySet());
            // 持久化endMap数据
            writeDataToFile(_end_tmp_file, _end_file, endMap);
            originalEndMap.clear();
            originalEndMap.putAll(endMap); //叠加原始数据
        }

        // FIXME: 这个地方可能有个问题，当持久化errorMap 成功，但还没来得及持久化mqMap的时候，断电，那么程序再次启动的时候可能会重复记录errorLog，
        // FIXME: 这目前来看没有问题，因为errorlog，只是起到一个记录日志的作用，而不是用来作为程序运行的数据文件，以后也不要从errorlog里面读取数据，因为他不可靠
        // 检查retryMap 里面超过阈值的值
        if (retryMap != null && retryMap.size() > 0) {
            Map<String, EventDefinition> errorMap = new HashMap<>(retryMap.size());
            for (Map.Entry<String, Integer> entry : retryMap.entrySet()) {
                Integer retryCount = entry.getValue();
                if (retryCount != null && retryCount > SettingSupport.getRetryCount()) {
                    // 记录 err.log
                    errorMap.put(entry.getKey(), mqMap.get(entry.getKey()));
                    // 删除mqMap里面的值
                    mqMap.remove(entry.getKey());
                }
            }
            if (errorMap.size() > 0) {
                logger.warn("mqMap 重试 超过阈值大小，记录到errorlog，并舍弃该消息, errorMap:{}", errorMap.keySet());
                // 持久化 errorMap
                CacheMapFileUtils.writeDataToFile(errorFile, true, new CacheMapFileUtils.WriteDataToFileProcess() {
                    @Override
                    protected void process(OutputStreamWriter writer) throws Exception {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Map.Entry<String, EventDefinition> entry : errorMap.entrySet()) {
                            EventDefinition errorLog = entry.getValue();
                            String str = JSONObject.toJSONString(errorLog);
                            stringBuilder.append(str).append("\n");
                        }
                        writer.append(stringBuilder.toString());
                    }
                });
                // 持久化成功，删除retryMap 里面的数据
                for (Map.Entry<String, EventDefinition> entry : errorMap.entrySet()) {
                    retryMap.remove(entry.getKey());
                }
            }
        }

        // 持久化 mq Map
        if (!com.tuandai.transaction.client.utils.CollectionUtils.isMapEqual(mqMap, originalMqMap)) {
            logger.debug("SimpleEventDefinitionRegistry持久化mqMap数据," + mqMap.keySet());
            // 持久化endMap数据
            writeDataToFile(_mq_tmp_file, _mq_file, mqMap);
            originalMqMap.clear();
            originalMqMap.putAll(mqMap);
        }

    }

    private void writeDataToFile(File tmp, File rename, Map<String, EventDefinition> map) {
        CacheMapFileUtils.writeDataToFile(tmp, false, new CacheMapFileUtils.WriteDataToFileProcess() {
            @Override
            public void process(OutputStreamWriter writer) throws Exception {
                Iterator<Map.Entry<String, EventDefinition>> iterator = map.entrySet().iterator();
                while(iterator.hasNext()) {
                    Map.Entry<String, EventDefinition> entry = iterator.next();
                    EventDefinition definition = entry.getValue();
                    writer.write(JSONObject.toJSONString(definition) + "\n");
                }
            }
            @Override
            protected void endProcess(File file) throws Exception {
                // 得到文件全路径名
                String renameFilePath = rename.getAbsolutePath();
                // 删除
                boolean sd = rename.delete();
                // 改名
                file.renameTo(new File(renameFilePath));
            }
        });
    }

    private void extractCheckMap() {
        Iterator<Map.Entry<String, EventDefinition>> iterator = beginMap.entrySet().iterator();
        long dt = System.currentTimeMillis();
        checkMap.clear();
        while(iterator.hasNext()) {
            Map.Entry<String, EventDefinition> entry = iterator.next();
            EventDefinition evalue = entry.getValue();
            logger.debug("extractCheckMap, evalue: " + evalue);
            if (isCheckLog(evalue, dt)) {
                logger.debug("extractCheckMap,isCheckLog, evalue: " + evalue);
                checkMap.put(entry.getKey(), entry.getValue());
                iterator.remove();
            }
        }
    }

    @Override
    public void loadAllPersistentEventDefinition() {
        // check beginMap 和endMap是否存在tmp的情况
        checkTmpMap();
        // 加载 begin、endMap数据, 并且记录当前循环的数据用于和下次循环做比较
        readDataToMap(_begin_file, beginMap, originalBeginMap);
        logger.debug("loadAllPersistentEventDefinition()，加载 _begin_file.name：" +
                (_begin_file == null ? null : _begin_file.getName()) + " ,beginMap:" + beginMap +
                " ,originalBeginMap:" + originalBeginMap);
        readDataToMap(_end_file, endMap, originalEndMap);
        logger.debug("loadAllPersistentEventDefinition()，加载 _end_file.name：" +
                (_end_file == null ? null : _end_file.getName()) + " ,beginMap:" + endMap +
                " ,originalEndMap:" + originalEndMap);
        readDataToMap(_mq_file, mqMap, originalMqMap);
        // 初始化 retryMap
        readDataToRetryMap(mqMap);
        logger.debug("loadAllPersistentEventDefinition()，加载 _mq_file.name：" +
                (_mq_file == null ? null : _mq_file.getName()) + " ,beginMap:" + mqMap +
                " ,originalMqMap:" + originalMqMap);
    }

    private void readDataToRetryMap(Map<String, EventDefinition> mqMap) {
        if (mqMap != null && mqMap.size() > 0) {
            for (String key : mqMap.keySet()) {
                retryMap.put(key, 0);
            }
        }
    }

    private void checkTmpMap() {
        try {
            // 查看beginMap是否存在
            if (!_begin_file.exists() && _begin_tmp_file.exists()) {
                _begin_tmp_file.renameTo(_begin_file);
                logger.debug("存在_begin_tmp_file，做更名操作！");
            }

            if (!_end_file.exists() && _end_tmp_file.exists()) {
                _end_tmp_file.renameTo(_end_file);
                logger.debug("存在 _end_tmp_file，做更名操作！");
            }
            if (!_mq_file.exists() && _mq_tmp_file.exists()) {
                _mq_tmp_file.renameTo(_mq_file);
                logger.debug("存在 _mq_tmp_file，做更名操作！");
            }
        } catch (Exception e) {
            logger.error("begin/end.map.tmp文件转化为begin/end.map文件失败!");
            throw new IllegalArgumentException("begin/end.map.tmp文件转化为begin/end.map文件失败!");
        }
    }

    @Override
    public Map<String, EventDefinition> getCheckDefinitionMap() {
        // 分离check数据
        extractCheckMap();
        return checkMap;
    }

    @Override
    public Map<String, EventDefinition> getCheckMap() {
        return checkMap;
    }

    @Override
    public Map<String, EventDefinition> getMqMap() {
        return mqMap;
    }


    @Override
    public Map<String, EventDefinition> getBeginMap() {
        return beginMap;
    }

    @Override
    public Map<String, EventDefinition> getEndMap() {
        return endMap;
    }

    private Boolean isCheckLog(EventDefinition evalue, long dt) {
        long time = SettingSupport.getCheckIntervalTime();
        return time < (dt - evalue.getGoMapTime()); //时间差小于指定时间，表示已经处理
    }

    private void readDataToMap(File file, Map<String, EventDefinition> map, Map<String, EventDefinition> sourceMap) {
        CacheMapFileUtils.readDataToMap(file, new CacheMapFileUtils.ReadDataToMapProcess() {
            Date dt = new Date();
            @Override
            protected boolean process(String str) throws Exception {
                EventDefinition event = JSONObject.parseObject(str, EventDefinition.class);
                event.setGoMapTime(dt.getTime());
                map.put(event.getUid(), event);
                sourceMap.put(event.getUid(), event);
                return true;
            }
        });
    }

}
