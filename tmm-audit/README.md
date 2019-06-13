# TMM 消息稽核
    

## 简介 
<pre>
消息发送链路：
tmm-producer  ---->   MQ集群
                             | ---------> tmm-consumer1
                             | ---------> tmm-consumer2
                             | ---------> tmm-consumer3

通过对各链路上消息的跟踪，将消息发送记录与消费记录进行匹配，分析各链路消息是否正常处理完成。

</pre>


## 稽核日志收集格式 

<pre>
生产者日志： 
{
            "st": 1,
            "type": "tmmProduce",
            "ptime": 1542267888575,
            "producer": "producer-topic-md5",
            "uid": "15422678885844"
}


消费者日志：
{
            "uid": "15422678885844",
            "ptime": 1542267888575,
            "type": "tmmConsume",
            "producer": "producer-topic-md5",
            "consumer": "consumer-queue",
            "ctime": 1542267893342
}

收集到ES日志：
{
  "_index": "log4j-20181123",
  "_type": "log4j",
  "_id": "mVhOgQSa5Tl+OdU1wlxbCw==",
  "_score": null,
  "_source": {
    "hostName": "cs12-82-middleware",
    "serLoc": "DGDC",
    "logLevel": "INFO",
    "ip": "10.100.12.82",
    "message": "{\"type\":\"tmmConsume\",\"producer\":\"364411a131eecab69cb45f747257e787\",\"ptime\":1542944575683,\"ctime\":1542944575900,\"consumer\":\"demoQu\",\"uid\":\"69187620-3db3-4578-a847-008ce03c7b00\"}",
    "applicationId": "tmm_customer",
    "addDate": "2018-11-23T03:42:55.900"
  }
}
</pre>



## 代码结构

 - start.py  
 启动脚本： 
 python start.py    
 Usage: -s  batch_size  -t begin_time  -g tmm_producer_group
  - -h                        获取帮助信息  
  - -g tmm_producer_group   需要稽核的消息组，config里面配置的组
  - -s  batch_size     调整批处理数据量，默认：2000 【单批处理 2000 条 】   
  - -t  begin_time   调整开始稽核时间，默认：2 【当前时间回退2小时】

 - config  
  配置信息： 非专业人员请勿更改！     
  可调整配置说明：
<pre>
        # 稽核关系  全局变量 存储各稽核日志的 时间偏移量
        self.tmm_groups = {
                           "test_group":[
                              {
                              "name":"c8d6939a9d39078b43bd05ff6fdda214",
                              "desc":"tyfund-web-admin",
                              "consumers":{
                                           "tyfund_queue_monitor_prev":"tyfund  monitor prev"
                                          }
                              },
                              {
                              "name":"b8d5cb6ac852717a1709dad774f9115c",
                              "desc":"tmm_producer2",
                              "consumers":{
                                           "demoQu1":"tmm_customer1",
                                           "demoQu2":"tmm_customer2"
                                          }
                              }
                            ],
                         }

        # ES 配置
        self.es_ip="127.0.0.1"
        self.es_port=9200
        self.es_user="username"
        self.es_pwd="password"
        

</pre>

 - es   
   elasticsearch 对象
   
 - service    
    稽核服务的实现
    
 - util  
    常用工具集 
    
 - test  
   测试数据生成脚本


## 启动脚本
nohup  python -u  start.py -g  test_group  2>&1 1>/dev/null  &



## Grafana 报表配置
 - 配置数据源  
 Index name :  audit-tmm  
 Time field name: produce_time  
 

 - 最大消耗时长  [Singlestat]  
 Query:   _exists_:"ctime" AND ctime:[0 TO 10000]   
 Metric:   Max : ctime     
 Group by: producer_time    Interval: 20m   
 

 - 日消息总量  [Singlestat]  
 Query:   _exists_:"ctime"   
 Metric:   Max : count     
 Group by: producer_time    Interval: 1d   
 

 - 十分内耗时曲线  [Graph]  
 Query:  producer : "发送者TOPIC的MD5值"  
 Metric:   Average : ctime     
 Group by: producer_time    Interval: 10m   
 

 - 消息总量曲线  [Graph]  
 Query:  producer : "发送者TOPIC的MD5值"  
 Metric:   count     
 Group by: producer_time    Interval: 10m   
 

 - 未消费曲线  [Graph]  
 Query:  producer : "发送者TOPIC的MD5值" AND  !_exists_:"ctime"  
 Metric:   count     
 Group by: producer_time    Interval: 10m   
 

 - 生产者未录入曲线  [Graph]  
 Query:  producer : "发送者TOPIC的MD5值" AND  !_exists_:"mq_chanel"
 Metric:   count     
 Group by: producer_time    Interval: 10m   
 

 - 未消费列表  [Table]  
 Query:  !_exists_:"ctime"    
 Metric:   Row Document          Size: 100     

 - 生产者未录入列表  [Table]  
 Query:  !_exists_:"mq_chanel"   
 Metric:   Row Document          Size: 100     
 

 
