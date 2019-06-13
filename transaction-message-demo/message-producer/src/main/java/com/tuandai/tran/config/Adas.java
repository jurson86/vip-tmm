package com.tuandai.tran.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Adas {

    @Bean
    public FanoutExchange createFab() {
        return new FanoutExchange("mychange");
    }


}
