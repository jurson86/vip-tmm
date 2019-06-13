package com.tuandai.tran;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;

import com.tuandai.tran.config.InitailDBTables;


@SpringBootApplication
@EnableDiscoveryClient  // 消费者
@EnableEurekaClient 	// 生产者
//@ComponentScan(basePackages = {"com.tuandai.transaction"})
public class ProducerApplication {

	public static void main(String[] args) {		
		ApplicationContext applicationContext = SpringApplication.run(ProducerApplication.class, args);

		//初始化数据库表
		applicationContext.getBean(InitailDBTables.class).createTables();
	}
}
