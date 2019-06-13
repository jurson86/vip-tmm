package com.tuandai.transaction.client.consumer.annotation;

import java.lang.annotation.*;


/**
 * 名称：TMM 辅助注解
 *
 * 功能：默认绑定死信交换机 dle-tmm 和死信队列dlq-tmm
 *       tmm-admin通过监听死信队列，帮助业务方完成死信消息存储，以便消费者进一步处理死信消息
 *
 * 说明：进入死信消息有以下几种情况：1.客户端主动拒绝消息  2.客户端设置了队列大小，前面的消息  3.消息过期或者队列设置了过期时间
 *
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TmmQueue {

//    /**
//     * 设置消息过期时间，默认是6个小时
//     */
//    long messagettl() default Constants.x_message_ttl_value;
//
//    /**
//     * 设置队列的长度,请设置为 maxlength> 0的值，默认没有最大值
//     */
//    long maxLength() default 0;
//
//    /**
//     * 设置死信路由键，默认为队列名
//     */
//    String deadRoutingKey() default "";

}
