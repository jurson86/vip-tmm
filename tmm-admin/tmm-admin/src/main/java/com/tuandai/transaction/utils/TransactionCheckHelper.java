package com.tuandai.transaction.utils;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.bo.ClusterIp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionCheckHelper {

    private static final Logger logger = LoggerFactory.getLogger(TransactionCheckHelper.class);

    public static ClusterIp clusterIpStr2ClusterIp(String clusterIp) {
        if (clusterIp == null) {
            return null;
        }
        ClusterIp clusterIpJson = null;
        try {
            clusterIpJson = JSONObject.parseObject(clusterIp, ClusterIp.class);
        } catch (Exception e) {
            // 转化失败，老数据
            logger.warn("转化失败，老数据, clusterIp:" + clusterIp);
            clusterIpJson = new ClusterIp();
            clusterIpJson.setIp(clusterIp);
        }
        return clusterIpJson;
    }

}
