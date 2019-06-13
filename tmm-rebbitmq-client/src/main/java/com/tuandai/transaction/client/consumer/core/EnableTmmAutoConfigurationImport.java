package com.tuandai.transaction.client.consumer.core;

import com.tuandai.transaction.client.consumer.annotation.EnableTmmConsumer;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

public class EnableTmmAutoConfigurationImport implements DeferredImportSelector, BeanClassLoaderAware {

    private ClassLoader classLoader;

    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        List<String> beanNames = SpringFactoriesLoader.loadFactoryNames(EnableTmmConsumer.class, classLoader);
        return beanNames.toArray(new String[beanNames.size()]);
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
