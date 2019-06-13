package com.tuandai.transaction.producer.config;


import com.tuandai.transaction.client.config.TMMConfig;
import com.tuandai.transaction.producer.bo.RocketAddress;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  严格来说 rocketmq的相关类和 配置文件、 factories 是在starter 里面，因为代码比较少，将配置文件和starter 合并
 */

@ConfigurationProperties(prefix = "spring")
public class TMMRocketProperties extends TMMConfig {

    /**
     *接受rocketmq所有属性
     */
    private Map<String, String> rocketmq = new HashMap<>();

    public Map<String, String> getRocketmq() {
        return rocketmq;
    }

    public void setRocketmq(Map<String, String> rocketmq) {
        this.rocketmq = rocketmq;
    }

    public Map<String, RocketAddress> getRockerAddressMap() {
        Map<String, RocketAddress> ipMap = new ConcurrentHashMap<>();
        // 生成对象
        for (Map.Entry<String, String> entry : rocketmq.entrySet()) {
            String propertyKey = entry.getKey();
            String propertyValue = entry.getValue();
            String[] propertys = propertyKey.split("\\.");
            if (propertys.length == 0) {
                continue;
            }
            // 最后一个字段
            String propertyTail = propertys[propertys.length - 1];
            // 倒数第二个字段
            String propertyCountdownSecond = propertys.length == 1 ? "default" : propertys[propertys.length - 2];
            if (propertyTail.equals("name-server")) {
                ipMap.put(propertyCountdownSecond, new RocketAddress(propertyValue));
            }
        }

        for (Map.Entry<String, String> entry : rocketmq.entrySet()) {
            String propertyKey = entry.getKey();
            String propertyValue = entry.getValue();
            String[] propertys = propertyKey.split("\\.");
            // 最后一个字段
            String propertyTail = propertys[propertys.length - 1];
            if (propertys.length == 0 || propertyTail.equals("name-server")) {
                continue;
            }

            // 倒数第三个字段
            String propertyCountdownSecond = propertys.length == 2 ? "default" : propertys[propertys.length - 3];

            if (propertyTail.equals("group")) {
                ipMap.get(propertyCountdownSecond).setGroup(propertyValue);
            } else if (propertyTail.equals("send-msg-timeout")) {
                ipMap.get(propertyCountdownSecond).setSendMsgTimeout(Integer.valueOf(propertyValue));
            }
        }

        return ipMap;
    }

}
