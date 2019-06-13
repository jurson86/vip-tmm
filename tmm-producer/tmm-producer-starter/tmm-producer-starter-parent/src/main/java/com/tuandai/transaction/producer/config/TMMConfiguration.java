package com.tuandai.transaction.producer.config;

import com.tuandai.transaction.client.config.TMMConfig;
import com.tuandai.transaction.client.service.TMMServiceImpl;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.ServiceLoader;

@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(name = "spring.rabbitmq.tmm.producer.enabled", havingValue = "true")
public class TMMConfiguration {

    @Bean
    public TMMServiceImpl createTMMClient(TMMConfig tmmConfig) {
        TMMServiceImpl tMMServiceImpl  = new TMMServiceImpl();
        TMMServiceImpl proxy = createTMMServiceImpl(tMMServiceImpl);
        proxy.start(tmmConfig);
        return proxy;
    }

    private TMMServiceImpl createTMMServiceImpl(TMMServiceImpl tMMServiceImpl) {
        // 启用代理对象
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetClass(TMMServiceImpl.class);
        proxyFactory.setTarget(tMMServiceImpl);
        // 使用cglib代理
        proxyFactory.setProxyTargetClass(true);

        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        NameMatchMethodPointcut nameMatchMethodPointcut = new NameMatchMethodPointcut();
        nameMatchMethodPointcut.setMappedNames(new String[]{"sendTransBeginToFlume", "sendTransEndToFlume", "sendNTrans"});
        advisor.setPointcut(nameMatchMethodPointcut);

        // spi
        ServiceLoader<MethodBeforeAdvice> loadedParsers = ServiceLoader.load(MethodBeforeAdvice.class);
        Iterator<MethodBeforeAdvice> driversIterator = loadedParsers.iterator();
        MethodBeforeAdvice tMMEventListener = null;
        try{
            while(driversIterator.hasNext()) {
                tMMEventListener = driversIterator.next();
            }
        } catch(Throwable t) {
            System.out.println("tmm 加载MqService扩展类失败,请检查resources/META-INF/services/com.tuandai.transaction.client.mq.inf.TMMEventListener 文件");
        }
        advisor.setAdvice(tMMEventListener);
        proxyFactory.addAdvisor(advisor);
        return  (TMMServiceImpl)proxyFactory.getProxy();
    }

}
