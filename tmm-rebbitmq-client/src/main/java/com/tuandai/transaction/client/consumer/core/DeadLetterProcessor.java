package com.tuandai.transaction.client.consumer.core;

import com.tuandai.transaction.client.consumer.annotation.TmmQueueConfigure;
import com.tuandai.transaction.client.consumer.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.MethodMetadata;

import java.lang.reflect.Field;
import java.util.*;

public class DeadLetterProcessor implements BeanPostProcessor, BeanFactoryAware {

	private static final Logger logger = LoggerFactory.getLogger(DeadLetterProcessor.class);

	private BeanFactory beanFactory;

	private String applicationName;

	private Set<String> tmmQueueNames = new HashSet<>();

	public Set<String> getTmmQueueNames() {
		return tmmQueueNames;
	}

	public DeadLetterProcessor(String applicationName) {
		this.applicationName = applicationName;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		tmmQueueHandler(bean, beanName);
		tmmQueueConfigureHandler(bean);
		return bean;
	}

	private void tmmQueueConfigureHandler(Object bean) {
		// 找到TmmQueueConfigure注解类
		Class<?> targetClass = AopUtils.getTargetClass(bean);
		TmmQueueConfigure tmmQueueConfigure  = targetClass.getAnnotation(TmmQueueConfigure.class);
		if (tmmQueueConfigure != null) {
			// 提取bean的名字
			tmmQueueNames.addAll(Arrays.asList(tmmQueueConfigure.queueNames()));
		}

	}

	private void tmmQueueHandler(Object bean, String beanName) {
		// 将所有的Queue都添加死信绑定并校验是否要生成死信队列
		if (bean instanceof Queue && isCreateDle(beanName)) {
			logger.info("tmm TmmQueue 拦截Queue -> bean: " + bean + " beanName:" + beanName);
			Queue queue = (Queue) bean;
			DeadLetterHelper.checkArguments(queue);
			// 再次获取
			Map<String, Object> a = queue.getArguments();
			DeadLetterHelper.initArruments(a, applicationName);
			if (a != null) {
			}
		}
	}

	private boolean isCreateDle(String beanName) {
		if (beanFactory != null && beanFactory instanceof  BeanDefinitionRegistry) {
			BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry)beanFactory;
			BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanName);
			if (beanDefinition != null && beanDefinition instanceof AnnotatedBeanDefinition) {
				AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition)beanDefinition;
				MethodMetadata metadatas =  annotatedBeanDefinition.getFactoryMethodMetadata();
				Map<String, Object> metadata = metadatas.getAnnotationAttributes("com.tuandai.transaction.client.consumer.annotation.TmmQueue");
				// 提取注解的值
				if (metadata != null) {
					return true;
				}
			}
		}
		return false;
	}



	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

}