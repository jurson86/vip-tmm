package com.tuandai.transaction.client.service.inf;

import com.tuandai.transaction.client.bo.EventDefinition;

import java.util.Map;

/**
 * 事件注册器
 */
public interface EventDefinitionRegistry {

    // 添加
    void registerEventDefinition(String eventName, EventDefinition eventDefinition);

    // 删除
    EventDefinition removeEventDefinition(String eventName, EventDefinition.EventType eventType);

    // 获取
    EventDefinition getEventDefinition(String eventName, EventDefinition.EventType eventType);

    // 包含
    boolean containsEventDefinition(String eventName, EventDefinition.EventType eventType);

    // 持久化
    void persistentEventDefinition();

    // 加载磁盘数据
    void loadAllPersistentEventDefinition();

    // 萃取checkMap
    Map<String, EventDefinition> getCheckDefinitionMap();

    //
    Map<String, EventDefinition> getCheckMap();

    // 查询mqMap
    Map<String, EventDefinition> getMqMap();


    Map<String, EventDefinition> getBeginMap();

    Map<String, EventDefinition> getEndMap();

}
