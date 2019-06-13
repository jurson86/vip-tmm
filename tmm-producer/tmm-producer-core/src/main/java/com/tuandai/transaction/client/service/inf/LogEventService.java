package com.tuandai.transaction.client.service.inf;

import com.tuandai.transaction.client.bo.EventDefinition;

import java.io.IOException;
import java.util.List;

public interface LogEventService {

    void writeLogEvent(EventDefinition eventDefinition);

    List<EventDefinition> readLogEvent(Integer size);

    // 持久化checkpoint的值
    boolean persistentCheckpoint() throws IOException;

    // 加载checkpoint
    void loadCheckpoint();

    // 重置checkpoint
    void resetCheckpoint();

    // 删除文件
    void removeFile(String fileName);

}
