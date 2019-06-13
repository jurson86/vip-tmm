#!/usr/bin/env python
# -*- coding:utf-8 -*-
import sys, getopt
import thread
import threading
import urllib2
import json
import time
import datetime
import pytz
from datetime import timedelta
import random
from ElasticObj import ElasticObj


##########################   CONFIG    ############################

## 多线程局部变量
threading_local=threading.local()

## 输入参数
class ConfigPra:
    def __init__(self):
        # 稽核开始时间= 当前时间向前 - b_time  : 默认 2h
        self.b_time = 2
        # 每次处理数据大小
        self.batch_size=3000
        # 最近重复处理时间
        self.repeat_time=0.1
        # 自动清理历史数据
        self.delete_time=24
p_config = ConfigPra()


# 队列规则
class ConfigRule:
    def __init__(self):
        # 稽核关系  全局变量 存储各稽核日志的 时间偏移量
        self.tmm_producers = {
                              "364411a131eecab69cb45f747257e787":{
                              "consumers":{
                                           "demoQu":"tmm_customer1",
                                           "demoQu3":"tmm_customer3"
                                          },
                              "desc":"tmm_producer1"
                              },
                              "b8d5cb6ac852717a1709dad774f9115c":{
                              "consumers":{
                                           "demoQu2":"tmm_customer2"
                                          },
                              "desc":"tmm_producer2"
                              }
                            }
rule_config = ConfigRule()


class EsObjs:
    def __init__(self):
        # 业务日志
        self.tmm_logs=ElasticObj("log4j","log4j","10.100.13.6",9200,"tmmdev","tmmdevtmmdev")
        # 稽核结果
        self.tmm_audit_maping={
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
        self.tmm_audit=ElasticObj("audit-tmm","tmm-audit","10.100.13.6",9200,"tmmdev","tmmdevtmmdev")
        #self.tmm_audit=ElasticObj("audit-tmm","tmm-audit","10.103.1.190",9200)





####################################################################
#  获取东8区，当前时间
def get_cntime():
    time_utc = datetime.datetime.utcnow()
    time_now = (time_utc + datetime.timedelta(hours=8))
    return time_now

# 获取当天 0 点时间戳
def get_today_time():
    time_utc = time.localtime(time.time())
    today_time = time.mktime(time.strptime(time.strftime('%Y-%m-%d 00:00:00', time_utc),'%Y-%m-%d %H:%M:%S'))
    print ('get_today_time: %.0f' %(today_time))
    return int(today_time * 1000)

# 时间戳向前退后 小时数
def get_ahead_time(mktime,hours):
    b_time = int( mktime * 1000 - hours * 60 * 60 * 1000 )
    print ('get_ahead_time: %.0f' %(b_time))
    return b_time

# UTCS时间转换为时间戳 2016-07-31T16:00:00Z
'''
 UTC格式时间 : 2018-11-20T02:00:43.186
 东八区时间： 2018-11-20 10:00:43.186
'''
def utc_to_local(utc_time_str, utc_format='%Y-%m-%dT%H:%M:%S.%f'):
    local_tz = pytz.timezone('Asia/Chongqing')
    local_format = "%Y-%m-%d %H:%M:%S.%f"
    utc_dt = datetime.datetime.strptime(utc_time_str, utc_format)
    local_dt = utc_dt.replace(tzinfo=pytz.utc).astimezone(local_tz)
    # 毫秒级 时间串
    time_str = local_dt.strftime(local_format)
    # 秒 级时间戳
    return int(time.mktime(time.strptime(time_str,local_format)))


# 线程循环
def thread_run():
    try:
        thread.start_new_thread(start_tmm,(1,))
    except:
        print ("ERROR:net unable to start thread")
    while 1:
        pass


#################################################################

# 生产者日志稽核audit
def producer_check(p_tmm_producer,p_size):
    search_body={
            "query" : {
                "bool": {
                    "filter" : {
                        "range" : {
                            "addDate" : {
                                "gte" : threading_local.point_time
                            }
                        }
                    },
                    "must":[
                          { "match": { "message":"tmmProduce" }},
                          { "match": { "message": p_tmm_producer  }}
                        ]
                }
            },
            "sort":{"addDate":"asc"},
            "size": p_size
        }
    print ( search_body)
    q_dict=threading_local.es_objs.tmm_logs.get_data_by_body(search_body)
    # 每次循环后配置重置
    reset_config(p_tmm_producer)
    ## 输出总量，监控是否有数据积压
    hits_log = {}
    hits_log["total"] = q_dict["hits"]["total"]
    hits_log["service"] = p_tmm_producer
    hits_log["@timestamp"]=get_cntime().strftime("%Y-%m-%dT%H:%M:%S.000+0800")
    #print(hits_log)
    list_bulks = []
    #if hits_log["total"] * 2  > p_size :
    if hits_log["total"]  > 0 :
        ## 逐条数据处理
        for i_dict in q_dict["hits"]["hits"]:
            for i_consumer in rule_config.tmm_producers[p_tmm_producer]["consumers"].keys() :
                v_mq_chanel = str(rule_config.tmm_producers[p_tmm_producer]["desc"]) + " -> " + str(rule_config.tmm_producers[p_tmm_producer]["consumers"][i_consumer]) 
                v_message = json.loads(i_dict["_source"]["message"])
                v_ptime = long(v_message["ptime"])
                dateArray = datetime.datetime.utcfromtimestamp(float(v_ptime/1000)) + timedelta(hours=8)
                v_produce_time = dateArray.strftime("%Y-%m-%dT%H:%M:%S.000+0800")
                i_audit_data = {
                "_op_type": "update",
                "_index": threading_local.es_objs.tmm_audit.get_index(),
                "_type": threading_local.es_objs.tmm_audit.get_type(),
                "_id": v_message["producer"] + "=" + i_consumer + "=" + v_message["uid"], 
                "doc": {
                    "ptime": v_ptime,
                    "producer":v_message["producer"],
                    "mq_chanel": v_mq_chanel,
                    "produce_time": v_produce_time
                   },
                "doc_as_upsert" : True
                }
                list_bulks.append(i_audit_data)

        # 生产者稽核数据插入 
        threading_local.es_objs.tmm_audit.put_index_bulks(list_bulks)
        threading_local.point_time =  i_dict["_source"]["addDate"]


##############################################################################
# 消费者日志稽核audit
def consumer_check(p_tmm_producer,p_size):
    search_body={
            "query" : {
                "bool": {
                    "filter" : {
                        "range" : {
                            "addDate" : {
                                "gte" : threading_local.consumer_point_time,
                            }
                        }
                    },
                    "must":[
                          { "match": { "message":"tmmConsume" }},
                          { "match": { "message": p_tmm_producer  }}
                        ]
                }
            },
            "sort":{"addDate":"asc"},
            "size": p_size
        }

    print ( search_body)
    q_dict=threading_local.es_objs.tmm_logs.get_data_by_body(search_body)
    # 每次循环后配置重置
    reset_config(p_tmm_producer)
    ## 输出总量，监控是否有数据积压
    hits_log = {}
    hits_log["total"] = q_dict["hits"]["total"]
    hits_log["service"] = "consumers:" + p_tmm_producer  
    hits_log["@timestamp"]=get_cntime().strftime("%Y-%m-%dT%H:%M:%S.000+0800")
    #print(hits_log)
    list_bulks = []
    #if hits_log["total"] * 2 > p_size :
    if hits_log["total"]  > 0 :
        ## 逐条数据处理
        for i_dict in q_dict["hits"]["hits"]:
            v_message = json.loads(i_dict["_source"]["message"])
            v_ptime = long(v_message["ptime"])
            v_ctime = long(v_message["ctime"])
            dateArray = datetime.datetime.utcfromtimestamp(float(v_ptime/1000)) + timedelta(hours=8)
            v_produce_time = dateArray.strftime("%Y-%m-%dT%H:%M:%S.000+0800")
            i_audit_data = {
            "_op_type": "update",
            "_index": threading_local.es_objs.tmm_audit.get_index(),
            "_type": threading_local.es_objs.tmm_audit.get_type(),
            "_id": v_message["producer"] + "=" + v_message["consumer"] + "=" + v_message["uid"], 
            "doc": {
                    "ptime": v_ptime,
                    "ctime": v_ctime - v_ptime,
                    "producer":v_message["producer"],
                    "produce_time": v_produce_time
                   },
            "doc_as_upsert" : True
            }
            list_bulks.append(i_audit_data)

        # 消费者稽核数据插入
        threading_local.es_objs.tmm_audit.put_index_bulks(list_bulks)
        threading_local.consumer_point_time =  i_dict["_source"]["addDate"]

#######################################################################################
# 生产者日志 稽核循环控制
def while_process_check(p_tmm_producer,p_size):
    thread_config()
    reset_config(p_tmm_producer)
    # 当处理时间小于当前时间前0.5小时 时 继续执行
    p_point_time=0
    c_point_time=0
    while (1) :
        try:
            producer_check(p_tmm_producer,p_size)
            time.sleep(0.5)
            consumer_check(p_tmm_producer,p_size)
            time.sleep(0.5)
            # 当前时间小于当前时间半小时， 或者无最新数据时，睡眠 60s
            if((p_point_time == threading_local.point_time  ) and (c_point_time == threading_local.consumer_point_time))  :
                print("sleep 60s at : " + str(p_point_time))
                # 间隙时间重复处理
                time.sleep(60)
                if str(threading_local.point_time).find("T") > 0:
                    day1 = time.strftime('%d',time.localtime())
                    day2 = time.strftime('%d',time.localtime(utc_to_local(threading_local.point_time)))
                    # 处理跨日期情况 变更新的索引
                    if( day1 != day2):
                        print(str(day1) + "==========================================" + str(day2))
                        threading_local.point_time = get_today_time()
                        threading_local.consumer_point_time = get_today_time()
                    # 处理非跨日期情况 变更新的索引
                    else:
                        threading_local.point_time = get_ahead_time(utc_to_local(threading_local.point_time),p_config.repeat_time)
                        threading_local.consumer_point_time = get_ahead_time(utc_to_local(threading_local.consumer_point_time),p_config.repeat_time)
            # 控制睡眠时间
            p_point_time = threading_local.point_time
            c_point_time = threading_local.consumer_point_time
        except Exception, e :
            print (str(e))
            time.sleep(10)




#############################    MAIN  #######################################
def main(argv):
   try:
      opts, args = getopt.getopt(argv,"hs:t:",["size=","time="])
   except getopt.GetoptError:
      print ("-s <batch_size>  -t <begin_time> ")
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
         print ("-s <batch_size>  -t <begin_time> ")
         sys.exit()
      elif opt in ("-s", "--size"):
         p_config.batch_size = arg
      elif opt in ("-t", "--time"):
         p_config.b_time = arg
   print 'batch size: ', p_config.batch_size 
   print 'begin time: ', p_config.b_time 


def thread_config():
    ## 线程局部变量
    threading_local.point_time = get_ahead_time(time.time(),p_config.b_time)
    threading_local.consumer_point_time = get_ahead_time(time.time(),p_config.b_time)
    ## 初始化 
    threading_local.es_objs = EsObjs()
    threading_local.es_objs.tmm_audit.create_index(threading_local.es_objs.tmm_audit_maping)

def reset_config(p_tmm_producer):
    # 设置索引
    #print(p_tmm_producer + " : index time : " +  str(rule_config.tmm_producers[p_tmm_producer]["point_time"]))
    if str(threading_local.point_time).find("T") > 0:
        point_time_i = utc_to_local(threading_local.point_time)
        logs_index = "log4j-" + str(time.strftime("%Y%m%d", time.localtime(point_time_i)))
        threading_local.es_objs.tmm_logs.set_index(logs_index)
    else:
        point_time_i = long(threading_local.point_time)/1000
        logs_index = "log4j-" + str(time.strftime("%Y%m%d", time.localtime(point_time_i)))
        threading_local.es_objs.tmm_logs.set_index(logs_index)

def start_audit_producer():
    for p_tmm_producer in rule_config.tmm_producers.keys() :
        try:
            thread.start_new_thread(while_process_check,(p_tmm_producer,p_config.batch_size))
        except Exception, e :
            print ("ERROR: unable to start thread : " + str(e))
    while 1:
        pass


if __name__ == '__main__':
    main(sys.argv[1:])
    start_audit_producer()
