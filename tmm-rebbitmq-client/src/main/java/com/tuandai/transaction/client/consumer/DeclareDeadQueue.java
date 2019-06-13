package com.tuandai.transaction.client.consumer;

import com.tuandai.transaction.client.consumer.utils.Constants;
import org.springframework.amqp.core.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;

@Deprecated
public class DeclareDeadQueue implements BeanDefinitionRegistryPostProcessor, BeanNameAware {

	private String inQueue;

	private String vHost;

	private String inExchange;

	private String beanName;

	public DeclareDeadQueue() {
	}

	public DeclareDeadQueue(String inQueue, String vHost, String inExchange) {
		this.inQueue = inQueue;
		this.vHost = vHost;
		this.inExchange = inExchange;
	}

	public String getInQueue() {
		return inQueue;
	}

	public void setInQueue(String inQueue) {
		this.inQueue = inQueue;
	}

	public String getvHost() {
		return vHost;
	}

	public void setvHost(String vHost) {
		this.vHost = vHost;
	}

	public String getInExchange() {
		return inExchange;
	}

	public void setInExchange(String inExchange) {
		this.inExchange = inExchange;
	}

	String getDeadQueue() {
		return Constants.DEAD_LETTER_PREFIX + vHost + Constants.DEAD_LETTER_SPLIT + inExchange
				+ Constants.DEAD_LETTER_SPLIT + inQueue;
	}

	private Queue incomingQueue() {
		return QueueBuilder.durable(inQueue).withArgument("x-dead-letter-exchange", Constants.DEAD_LETTER_EXCHANGE_NAME)
				.withArgument("x-dead-letter-routing-key", getDeadQueue())
				.withArgument("x-message-ttl", Constants.RETRY_DELAY).build();
	}

	// 使用exchangeKey将队列绑定到exchange上
	private Binding createBinding() {
		return BindingBuilder.bind(createDeadQueue()).to(createDeadExchange()).with(getDeadQueue());
	}

	private Queue createDeadQueue() {
		return QueueBuilder.durable(getDeadQueue()).build();
	}

	// 创建一个topicExchange
	private TopicExchange createDeadExchange() {
		return new TopicExchange(Constants.DEAD_LETTER_EXCHANGE_NAME);
	}

	public void setBeanName(String name) {
		beanName = name;
	}

	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		// 代替bean注解
		// 生成业务队列，并绑定业务交换机和ttl已经路由键
		RootBeanDefinition inQueueBeanDefinition = new RootBeanDefinition(Queue.class);
		inQueueBeanDefinition.setFactoryMethodName("incomingQueue");
		inQueueBeanDefinition.setFactoryBeanName(beanName);
		inQueueBeanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		registry.registerBeanDefinition(inQueue, inQueueBeanDefinition);

		// 生成绑定关系
		RootBeanDefinition bindingBeanDefinition = new RootBeanDefinition();
		bindingBeanDefinition.setFactoryMethodName("createBinding");
		bindingBeanDefinition.setFactoryBeanName(beanName);
		bindingBeanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		registry.registerBeanDefinition(beanName + "_b", bindingBeanDefinition);

		// 生成死信队列
		RootBeanDefinition createDeadQueueBeanDefinition = new RootBeanDefinition();
		createDeadQueueBeanDefinition.setFactoryMethodName("createDeadQueue");
		createDeadQueueBeanDefinition.setFactoryBeanName(beanName);
		createDeadQueueBeanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		registry.registerBeanDefinition(beanName + "_dq", createDeadQueueBeanDefinition);

		// 创建TMM自己的交换机
		RootBeanDefinition createDeadExchangeBeanDefinition = new RootBeanDefinition();
		createDeadExchangeBeanDefinition.setFactoryMethodName("createDeadExchange");
		createDeadExchangeBeanDefinition.setFactoryBeanName(beanName);
		createDeadExchangeBeanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		registry.registerBeanDefinition(beanName + "_te", createDeadExchangeBeanDefinition);

	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}
}