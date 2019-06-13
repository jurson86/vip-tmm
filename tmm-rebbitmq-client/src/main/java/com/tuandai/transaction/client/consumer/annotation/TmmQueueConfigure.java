package com.tuandai.transaction.client.consumer.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TmmQueueConfigure {

    /**
     * 申明为TMM 死信消息监控的队列
     * 返回值： 队列名称数组
     * 注意：当服务器上有同名队列且配置参数不一致的时候可能导致报code=406的错误，请先删除队列（删除时注意是否还有未消费的消息）
     */
    String[] queueNames() default {};

}
