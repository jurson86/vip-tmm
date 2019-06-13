package com.tuandai.transaction.rabbitmq;

import com.tuandai.transaction.client.consumer.annotation.TmmQueue;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class RabbitConfig {


    @Value("${u.rabbitmq.username}")
    private String uname;
    @Value("${u.rabbitmq.password}")
    private String upassword;
    @Value("${u.rabbitmq.vhost.provider}")
    private String uhost;
    @Value("${u.rabbitmq.addresses}")
    private String uaddress;

    @Value("${b.rabbitmq.username}")
    private String bname;
    @Value("${b.rabbitmq.password}")
    private String bpassword;
    @Value("${b.rabbitmq.vhost.provider}")
    private String bhost;
    @Value("${b.rabbitmq.addresses}")
    private String baddress;

    @Value("${tmm.queue}")
    private String queue;

    @Value("${tmm.exchange}")
    private String exchange;


    @Bean(name="uConnectFactory")
    @Primary
    public ConnectionFactory uConnectFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(uaddress);
        connectionFactory.setUsername(uname);
        connectionFactory.setPassword(upassword);
        connectionFactory.setVirtualHost(uhost);
        return connectionFactory;
    }

    @Bean(name="bConnectFactory")
    public ConnectionFactory bConnectFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(baddress);
        connectionFactory.setUsername(bname);
        connectionFactory.setPassword(bpassword);
        connectionFactory.setVirtualHost(bhost);
        return connectionFactory;
    }


    @Bean(name="uRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory uRabbitListenerContainerFactory(
            @Qualifier("uConnectFactory") ConnectionFactory connectionFactory,
            RabbitProperties config
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        RabbitProperties.Listener listenerConfig = config.getListener();
        factory.setAutoStartup(listenerConfig.isAutoStartup());
        if (listenerConfig.getAcknowledgeMode() != null) {
            factory.setAcknowledgeMode(listenerConfig.getAcknowledgeMode());
        }
        if (listenerConfig.getConcurrency() != null) {
            factory.setConcurrentConsumers(listenerConfig.getConcurrency());
        }
        if (listenerConfig.getMaxConcurrency() != null) {
            factory.setMaxConcurrentConsumers(listenerConfig.getMaxConcurrency());
        }
        if (listenerConfig.getPrefetch() != null) {
            factory.setPrefetchCount(listenerConfig.getPrefetch());
        }
        if (listenerConfig.getTransactionSize() != null) {
            factory.setTxSize(listenerConfig.getTransactionSize());
        }
        return factory;
    }

    @Bean(name="bRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory brabbitListenerContainerFactory(
            @Qualifier("bConnectFactory") ConnectionFactory connectionFactory,
            RabbitProperties config
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        RabbitProperties.Listener listenerConfig = config.getListener();
        factory.setAutoStartup(listenerConfig.isAutoStartup());
        if (listenerConfig.getAcknowledgeMode() != null) {
            factory.setAcknowledgeMode(listenerConfig.getAcknowledgeMode());
        }
        if (listenerConfig.getConcurrency() != null) {
            factory.setConcurrentConsumers(listenerConfig.getConcurrency());
        }
        if (listenerConfig.getMaxConcurrency() != null) {
            factory.setMaxConcurrentConsumers(listenerConfig.getMaxConcurrency());
        }
        if (listenerConfig.getPrefetch() != null) {
            factory.setPrefetchCount(listenerConfig.getPrefetch());
        }
        if (listenerConfig.getTransactionSize() != null) {
            factory.setTxSize(listenerConfig.getTransactionSize());
        }
        return factory;
    }


    @Bean(name="uAmqpAdmin")
    public AmqpAdmin amqpAdmin(@Qualifier("uConnectFactory") ConnectionFactory connectionFactory,
                               @Qualifier("queueu") Queue queue) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(false);
        admin.declareQueue(queue);
        return admin;
    }

    @Bean(name="bAmqpAdmin")
    public AmqpAdmin rpdAmqpAdmin(@Qualifier("bConnectFactory") ConnectionFactory connectionFactory,
                                  @Qualifier("queueb") Queue queue) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(false);
        admin.declareQueue(queue);
        return admin;
    }

    @Bean
    @TmmQueue
    public Queue queueb() {
        return new Queue(queue);
    }

    @Bean
    public FanoutExchange mychange() {
        return new FanoutExchange(exchange);
    }

    @Bean
    public Binding bindingDemoQu2AndMychange(@Qualifier("queueb") Queue queue, @Qualifier("mychange") FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }



    @Bean
    @TmmQueue(x_dead_letter_routing_key = "A")
    public Queue queueu() {
        return new Queue("helloer");
    }
}
