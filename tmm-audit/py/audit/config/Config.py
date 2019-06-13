#coding:utf8
import os

class ConfigObj:
    def __init__(self,v_begin_time=2,v_batch_size=2000,v_repeat_time=0.1):
        # 稽核开始时间= 当前时间向前 - b_time  : 默认 2h
        self.begin_time = v_begin_time
        # 每次处理数据大小
        self.batch_size = v_batch_size
        # 最近重复处理时间
        self.repeat_time = v_repeat_time
        # 自动清理历史数据
        self.delete_time=24

        # 稽核关系  全局变量 存储各稽核日志的 时间偏移量
        self.tmm_producers = []
        self.tmm_groups = {
                           "test_group":[
                              {
                              "name":"364411a131eecab69cb45f747257e787",
                              "desc":"tmm_producer1",
                              "consumers":{
                                           "demoQu":"tmm_customer1",
                                           "demoQu3":"tmm_customer3"
                                          }
                              },
                              {
                              "name":"b8d5cb6ac852717a1709dad774f9115c",
                              "desc":"tmm_producer2",
                              "consumers":{
                                           "demoQu2":"tmm_customer2"
                                          }
                              }
                            ],
                         }
        # ES 配置
        self.es_ip="10.100.13.6"
        self.es_port=9200
        self.es_user="tmmdev"
        self.es_pwd="tmmdevtmmdev"
        # 稽核结果 索引数据结构
        self.audit_tmm_maping={
                    "properties": {
                        "producer": {
                            "type": "text",
                        },
                        "mq_chanel": {
                            "type": "text",
                        },
                        "ptime": {
                            "type": "long",
                        },
                        "ctime": {
                            "type": "long",
                            "index": True,
                        },
                        "produce_time": {
                            "type": "date",
                            "index": True,
                        }
                    }
                }




if __name__ == '__main__':
    config = ConfigObj()
    print config.tmm_producers

