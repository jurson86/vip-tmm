package com.tuandai.transaction.client.config;


import com.tuandai.transaction.client.bo.RabbitAddress;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ConfigurationProperties(prefix = "spring")
public class TMMRabbitProperties {

    /**
     *接受rabbitmq所有属性
     */
    private Map<String, String> rabbitmq = new HashMap<>();

    public Map<String, String> getRabbitmq() {
        return rabbitmq;
    }

    public void setRabbitmq(Map<String, String> rabbitmq) {
        this.rabbitmq = rabbitmq;
    }

    /**
     * 提供封装实体
     * ip -> RabbitAddress
     */
    public Map<String, RabbitAddress> getRabbitAddressMap() {
        Map<String, RabbitAddress> ipMap = new ConcurrentHashMap<>();

        // 如果host不为空
        for (Map.Entry<String, String> entry : rabbitmq.entrySet()) {
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

            if (propertyTail.equals("host")) {
                // 集群ip
                if (!ipMap.containsKey(propertyCountdownSecond)) {
                    ipMap.put(propertyCountdownSecond, new RabbitAddress(propertyValue));
                } else {
                    ipMap.get(propertyCountdownSecond).setIp(propertyValue);
                }
            } else if (propertyTail.equals("port")) {
                // port
                if (!ipMap.containsKey(propertyCountdownSecond)) {
                    RabbitAddress tmp = new RabbitAddress();
                    tmp.setPort(Integer.valueOf(propertyValue));
                    ipMap.put(propertyCountdownSecond, tmp);
                } else {
                    ipMap.get(propertyCountdownSecond).setPort(Integer.valueOf(propertyValue));
                }
            } else if (propertyTail.equals("username")) {
                // username
                if (!ipMap.containsKey(propertyCountdownSecond)) {
                    RabbitAddress tmp = new RabbitAddress();
                    tmp.setUserName(propertyValue);
                    ipMap.put(propertyCountdownSecond, tmp);
                } else {
                    ipMap.get(propertyCountdownSecond).setUserName(propertyValue);
                }
            } else if (propertyTail.equals("password")) {
                // password
                if (!ipMap.containsKey(propertyCountdownSecond)) {
                    RabbitAddress tmp = new RabbitAddress();
                    tmp.setPassword(propertyValue);
                    ipMap.put(propertyCountdownSecond, tmp);
                } else {
                    ipMap.get(propertyCountdownSecond).setPassword(propertyValue);
                }
            } else if (propertyTail.equals("addresses")) {
                // addresss
                String hosts[] = propertyValue.split(":");
                String addressesHost = hosts[0];
                String addressesPortStr = hosts[1];

                if (!ipMap.containsKey(propertyCountdownSecond)) {
                    RabbitAddress tmp = new RabbitAddress();
                    tmp.setPort(Integer.valueOf(addressesPortStr));
                    tmp.setIp(addressesHost);
                    ipMap.put(propertyCountdownSecond, tmp);
                } else {
                    RabbitAddress tmp = ipMap.get(propertyCountdownSecond);
                    tmp.setIp(addressesHost);
                    tmp.setPort(Integer.valueOf(addressesPortStr));
                }
            } else {
                continue;
            }
        }
//        for (Map.Entry<String, RabbitAddress> entry : ipMap.entrySet()) {
//            RabbitAddress value = entry.getValue();
//            resultMap.put(value.getIp(), entry.getValue());
//        }
        return ipMap;
    }

}
