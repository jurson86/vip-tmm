package com.tuandai.transaction.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

//    @Autowired
//    private TMMRabbitProperties tMMRabbitProperties;
//
//    // ----------------------------- check队列 ------------------------------------- //
//
//    @Bean
//    public FanoutExchange mychange() {
//        return  new FanoutExchange("tmm_check");
//    }
//
//    @Bean
//    public Queue createQueue() {
//        return new Queue("tmm-check-queue");
//    }
//
//    @Bean
//    public Binding bindingDemoQuAndMychange(@Qualifier("createQueue") Queue queue, @Qualifier("mychange") FanoutExchange fanoutExchange) {
//        return BindingBuilder.bind(queue).to(fanoutExchange);
//    }
//
//    // ----------------------------- start队列 ------------------------------------- //
//
//    @Bean
//    public FanoutExchange startExchange() {
//        return  new FanoutExchange("start_exchange");
//    }
//
//    @Bean
//    public Queue createStartQueue() {
//        return new Queue("tmm-start-queue");
//    }
//
//    @Bean
//    public Binding bindingStart(@Qualifier("createStartQueue") Queue queue, @Qualifier("startExchange") FanoutExchange fanoutExchange ) {
//        return BindingBuilder.bind(queue).to(fanoutExchange);
//    }

}
