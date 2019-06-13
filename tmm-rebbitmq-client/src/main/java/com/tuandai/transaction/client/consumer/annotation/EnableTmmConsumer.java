package com.tuandai.transaction.client.consumer.annotation;


import com.tuandai.transaction.client.consumer.core.EnableTmmAutoConfigurationImport;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(EnableTmmAutoConfigurationImport.class)
public @interface EnableTmmConsumer {
}
