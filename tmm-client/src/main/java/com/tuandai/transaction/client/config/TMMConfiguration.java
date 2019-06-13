package com.tuandai.transaction.client.config;

import com.tuandai.transaction.client.service.TMMServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@EnableConfigurationProperties({TMMRabbitProperties.class})
@ConditionalOnProperty(name = "spring.rabbitmq.tmm.producer.enabled", havingValue = "true")
public class TMMConfiguration {

    @Bean
    @DependsOn("settingSupport")
    public TMMServiceImpl createTMMClient() {
        return new TMMServiceImpl();
    }

    @Bean
    public SettingSupport settingSupport() {
        return new SettingSupport();
    }

}
