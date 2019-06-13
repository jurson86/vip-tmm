package com.tuandai.transaction.client.consumer.config;

import com.tuandai.transaction.client.consumer.core.ConsumerStartListener;
import com.tuandai.transaction.client.consumer.core.DeadLetterProcessor;
import com.tuandai.transaction.client.consumer.core.TmmQueueConfigureRegistry;
import com.tuandai.transaction.client.consumer.utils.Constants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
//@EnableConfigurationProperties(TmmProperties.class)
public class TmmConfigure {

    @Value("${spring.application.name}")
    private String name;

    @Bean
    public TopicExchange createDle() {
        return new TopicExchange(Constants.DEAD_LETTER_EXCHANGE_NAME);
    }

    @Bean(name=Constants.dlq)
    public Queue createDlq() {
        return new Queue(Constants.dlq);
    }

    @Bean
    public Binding createDleBinding(@Qualifier(Constants.dlq) Queue queue, @Qualifier("createDle") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with("#");
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        // 设置默认值
        // 默认异常则拒绝，防止requeue，阻塞业务
        factory.setDefaultRequeueRejected(false);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean
    public DeadLetterProcessor createDeadLetterProcessor() {
        return new DeadLetterProcessor(name);
    }

    @Bean
    public ConsumerStartListener createConsumerStartProcessor(RabbitTemplate rabbitTemplate) {
        return new ConsumerStartListener(rabbitTemplate, name);
    }

    @Bean
    public TmmQueueConfigureRegistry createTmmQueueConfigureProcessor(DeadLetterProcessor deadLetterProcessor) {
        return new TmmQueueConfigureRegistry(name, deadLetterProcessor);
    }

}
