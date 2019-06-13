package com.tuandai.transaction.client.service;

import com.tuandai.transaction.client.config.SettingSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value={"com.tuandai.transaction.client"})
public class App {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(App.class, args);
        System.out.println(SettingSupport.getRpcDir());
    }
}
