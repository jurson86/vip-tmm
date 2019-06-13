## 文件包功能说明
    tmm-client             生产端接入包；
    tmm-admin              监控管理web服务，整合了tmm-admin-web页面内容；
    tmm-admin-web          tmm-admin前端页面开发项目；
    tmm-rabbitmq-client    消费端接入包；


### 分支管理

注：

    1.1.4 版本需要通过如下方式加载tmm代码包：
          新增配置：   spring.rabbitmq.tmm.producer.enable=true
    1.1.3 版本需要通过如下方式加载tmm代码包：
          主函数添加： @ComponentScan(basePackages = {"com.tuandai.transaction"})


<pre>
master
   在线最新版本代码；

release
   当前灰度压测最新代码；


feature-1.1.4  
   tmm-admin 新增支撑多MQ集群；
   tmm-admin 新增权限管理功能；
   优化springboot 自动加载tmm代码包
   
feature-1.1.5 
   tmm-client 支持非事务可靠消息发送；
   tmm-client 添加必要配置信息，打印info级别日志；
   
feature-1.1.6
   tmm-client 支持多服务多实例同一个rpcpath路径部署
   修复了tmm-admin监控agent的没有扫描（1.1.4版本后修改导致）到接口的bug
   优化了tmm-client发送url前缀到tmm-admin，并优化了tmm-admin监控添加前缀的操作


</pre>










