package com.tuandai.transaction.mq.inf;

/**
 * 所有对mqCluster刷新感兴趣的都需要实现该接口
 */
public interface RefreshMqCluster {

    /**
     * 打开新的连接
     */
    void refreshConnection();

    /**
     * 关闭原有连接
     */
    void closeConnection();

}
