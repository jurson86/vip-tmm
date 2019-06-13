## 文件包功能说明
    tmm-client             生产端接入包；
    tmm-admin              监控管理web服务，整合了tmm-admin-web页面内容；
    tmm-admin-web          tmm-admin前端页面开发项目；
    tmm-rabbitmq-client    消费端接入包；


### 分支管理
 
<pre>
master
   在线最新版本代码；

release
   当前灰度压测最新代码；


feature-v1.0.0
   1.支持多账户监听同一个mq服务器。
   2.死信队列支持dlq-tmm死信队列的消息消费。
   3.支持消费者启动通知注册到tmm-admin。
</pre>











