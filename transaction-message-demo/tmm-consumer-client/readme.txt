
spring.rabbitmq.host=127.0.0.1
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=admin

#激活重试方案
spring.rabbitmq.listener.retry.enabled=true
#第一次和第二次的间隔
spring.rabbitmq.listener.retry.initial-interval=2000
#最大重试次数
spring.rabbitmq.listener.retry.max-attempts=3
#重试间隔阀值
spring.rabbitmq.listener.retry.multiplier=2
#重试间隔最大时长
spring.rabbitmq.listener.retry.max-interval=5000
#vhost
spring.rabbitmq.virtual-host=myVhost
#设置为false时，exception会重新retry
spring.rabbitmq.listener.default-requeue-rejected=false
