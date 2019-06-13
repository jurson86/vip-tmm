package com.tuandai.transaction;

import com.tuandai.transaction.client.consumer.annotation.EnableTmmConsumer;
import com.tuandai.transaction.client.consumer.annotation.TmmQueue;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;

@SpringBootApplication
@EnableTmmConsumer
public class ConsumerApplication {

	public static void main(String[] args) {
		 SpringApplication.run(ConsumerApplication.class, args);
	}

}
