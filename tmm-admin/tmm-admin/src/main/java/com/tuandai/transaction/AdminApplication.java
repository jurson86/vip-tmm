package com.tuandai.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient  // 消费者
@EnableEurekaClient 	// 生产者
@EnableScheduling
@EnableTransactionManagement // 开始事务
public class AdminApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(AdminApplication.class, args);
	}
}
