#! /usr/bin/env python
# -*- coding: utf8 -*-
import sys
import time
import datetime
from datetime import timedelta
import json
sys.path.append("..")
from util import TimeUtil as tu
from es import ElasticObj as es_obj

class TmmAudit:
    def __init__(self,p_es_log,p_es_audit,p_tmm_producer,p_batch_size,p_begin_time):
        '''
        :param p_es_log: 操作ES对象 日志索引
        :param p_es_audit: 操作ES对象 稽核索引
        :param p_tmm_producer: 稽核生产者 eg: {"name":"md5-chanel","desc":"cg mq producer","consumers": {"queue1":"consumer1","queue2":"consumer2"}}
        :param p_batch_size: 稽核批处理大小 
        :param p_begin_time: 稽核偏移量开始时间  毫秒时间戳
        '''
        self.es_log = p_es_log
        self.es_audit = p_es_audit
        self.tmm_producer = p_tmm_producer
        self.tmm_consumers = p_tmm_producer["consumers"]
        self.batch_size = p_batch_size
        self.producer_point_time = p_begin_time
        self.consumer_point_time = p_begin_time

    def producer_audit(self):
        '''
        # 生产者日志稽核audit
        '''
        search_body={
            "query" : {
                "bool": {
                    "filter" : {
                        "range" : {
                            "addDate" : {
                                "gte" : self.producer_point_time
                            }
                        }
                    },
                    "must":[
                          { "match": { "message":"tmmProduce" }},
                          { "match": { "message": self.tmm_producer["name"] }}
                        ]
                }
            },
            "sort":{"addDate":"asc"},
            "size": self.batch_size
        }
        print(json.dumps(search_body))
        # 每次循环重置 查询索引
        self.reset_config()
        # 查询
        q_dict=self.es_log.get_data_by_body(search_body)
        hits_total = q_dict["hits"]["total"]
        print(hits_total)
        list_bulks = []
        if hits_total  > 0 :
            ## 逐条数据处理
            for v_dict in q_dict["hits"]["hits"]:
                for v_consumer in self.tmm_consumers.keys() :
                    v_mq_chanel = str(self.tmm_producer["desc"]) + " -> " + str(self.tmm_consumers[v_consumer])
                    v_message = json.loads(v_dict["_source"]["message"])
                    v_ptime = long(v_message["ptime"])
                    dateArray = datetime.datetime.utcfromtimestamp(float(v_ptime/1000)) + timedelta(hours=8)
                    v_produce_time = dateArray.strftime("%Y-%m-%dT%H:%M:%S.000+0800")
                    v_audit_data = {
                    "_op_type": "update",
                    "_index": self.es_audit.index_name,
                    "_type": self.es_audit.index_type,
                    "_id": v_message["producer"] + "=" + v_consumer + "=" + v_message["uid"],
                    "doc": {
                        "ptime": v_ptime,
                        "producer":v_message["producer"],
                        "mq_chanel": v_mq_chanel,
                        "produce_time": v_produce_time
                       },
                    "doc_as_upsert" : True
                    }
                    list_bulks.append(v_audit_data)
    
            # 生产者稽核数据插入
            self.es_audit.put_index_bulks(list_bulks)
            self.producer_point_time =  v_dict["_source"]["addDate"]


    # 消费者日志稽核audit
    def consumer_audit(self):
        search_body={
            "query" : {
                "bool": {
                    "filter" : {
                        "range" : {
                            "addDate" : {
                                "gte" : self.consumer_point_time,
                            }
                        }
                    },
                    "must":[
                          { "match": { "message":"tmmConsume" }},
                          { "match": { "message": self.tmm_producer["name"]  }}
                        ]
                }
            },
            "sort":{"addDate":"asc"},
            "size": self.batch_size
        }
        print(json.dumps(search_body))
        q_dict=self.es_log.get_data_by_body(search_body)
        # 每次循环后配置重置
        self.reset_config()
        ## 输出总量，监控是否有数据积压
        hits_total = q_dict["hits"]["total"]
        print(hits_total)
        list_bulks = []
        if hits_total  > 0 :
            ## 逐条数据处理
            for v_dict in q_dict["hits"]["hits"]:
                v_message = json.loads(v_dict["_source"]["message"])
                v_ptime = long(v_message["ptime"])
                v_ctime = long(v_message["ctime"])
                dateArray = datetime.datetime.utcfromtimestamp(float(v_ptime/1000)) + timedelta(hours=8)
                v_produce_time = dateArray.strftime("%Y-%m-%dT%H:%M:%S.000+0800")
                v_audit_data = {
                "_op_type": "update",
                "_index": self.es_audit.index_name,
                "_type": self.es_audit.index_type,
                "_id": v_message["producer"] + "=" + v_message["consumer"] + "=" + v_message["uid"],
                "doc": {
                        "ptime": v_ptime,
                        "ctime": v_ctime - v_ptime,
                        "producer":v_message["producer"],
                        "produce_time": v_produce_time
                    },
                "doc_as_upsert" : True
                }
                list_bulks.append(v_audit_data)

            # 消费者稽核数据插入
            self.es_audit.put_index_bulks(list_bulks)
            self.consumer_point_time =  v_dict["_source"]["addDate"]



    def reset_config(self):
        if str(self.producer_point_time).find("T") > 0:
            point_time_i = tu.get_utc_string_to_local_ts(self.producer_point_time)
            logs_index = "log4j-" + str(time.strftime("%Y%m%d", time.localtime(point_time_i)))  + "*"
            self.es_log.index_name = logs_index
            audit_index = "audit-tmm-" + str(time.strftime("%Y%m%d", time.localtime(point_time_i)))
            self.es_audit.index_name = audit_index
        else:
            point_time_i = int(self.producer_point_time)/1000
            logs_index = "log4j-" + str(time.strftime("%Y%m%d", time.localtime(point_time_i)))  + "*"
            self.es_log.index_name = logs_index
            audit_index = "audit-tmm-" + str(time.strftime("%Y%m%d", time.localtime(point_time_i)))
            self.es_audit.index_name = audit_index
        #print self.es_audit.index_name
        #print self.es_log.index_name


if __name__ == '__main__':
    #obj = TmmAudit()
    print "hello world!"
    print tu.get_ts()
    print tu.get_ms()
    print tu.get_day(tu.get_ts())
    print tu.get_day_cn(tu.get_ts())
    
    p_es_log = es_obj.ElasticObj("log4j","log4j","10.103.1.190")
    p_es_audit = es_obj.ElasticObj("audit-tmm","audit-tmm","10.103.1.190")
    p_tmm_producer = {"name":"producer-service-name1","desc":"mq producer","consumers":{"consumer-service-name1":"consumer1","consumer-service-name2":"consumer2"}}
    p_batch_size = 2000
    #p_begin_time = "%0.f" % int(tu.get_today_zero_time() * 1000)
    p_begin_time =  int(tu.get_today_zero_time() * 1000)
    ta = TmmAudit(p_es_log,p_es_audit,p_tmm_producer,p_batch_size,p_begin_time)
    ta.producer_audit()
    time.sleep(0.5)
    ta.consumer_audit()
    
