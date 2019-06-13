package com.tuandai.transaction.mq;

import com.tuandai.transaction.bo.MqClusterEvent;
import com.tuandai.transaction.bo.RabbitAddress;
import com.tuandai.transaction.mq.inf.MqClusterStrategyFactory;
import com.tuandai.transaction.mq.inf.RefreshMqCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;

/**
 * 当更新配置的时候调用此方法做集中处理
 */
@Component
public class RabbitClusterManage implements ApplicationListener<MqClusterEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RabbitClusterManage.class);

    @Autowired
    private Set<RefreshMqCluster> refreshMqClusters;

    @Autowired
    private MqClusterStrategyFactory mqClusterStrategyFactory;

    @Override
    public void onApplicationEvent(MqClusterEvent event) {
        if (!CollectionUtils.isEmpty(refreshMqClusters)) {
            for (RefreshMqCluster refreshMqCluster : refreshMqClusters) {
                refreshMqCluster.closeConnection(); // 断开所有连接
            }
        }
        // 删除数组缓存
        RabbitTemplateFactory.close();

        // 重新初始化init
        Map<String, RabbitAddress> rabbitAddressMap = null;
        try {
            rabbitAddressMap = mqClusterStrategyFactory
                    .createMqClusterStrategy().loadRabbitAddressMap();
        } catch (Exception e) {
            logger.error("rabbitmq初始化失败");
        }
        RabbitTemplateFactory.init(rabbitAddressMap);

        // 刷新, 通知所有需要刷新的地方
        if (!CollectionUtils.isEmpty(refreshMqClusters)) {
            for (RefreshMqCluster refreshMqCluster : refreshMqClusters) {
                refreshMqCluster.refreshConnection(); // 重启连接
            }
        }
    }
}
