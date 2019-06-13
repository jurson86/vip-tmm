#!/usr/bin/env python
# -*- coding:utf-8 -*-
import thread
import urllib2
import json
import time
import random

# counst
es_host = "10.103.1.190"
es_port = "9200"

# http_put
def http_put(url,values):
    jdata = json.dumps(values)                  # 对数据进行JSON格式化编码
    request = urllib2.Request(url, jdata)
    request.add_header('Content-Type', 'application/json')
    request.get_method = lambda:'PUT'           # 设置HTTP的访问方式
    request = urllib2.urlopen(request)
    return request.read()



####################################################################


def es_put(_index,_type,_id,values):
    url="http://" + es_host + ":" + es_port + "/" + _index + "/" + _type + "/" + _id
    #values["@timestamp"]=time.strftime("%Y-%m-%dT%H:%M:%S.000+0800",time.localtime(time.time()))
    values["addDate"]=int(round(time.time() * 1000))
    http_put(url,values)



####################################################################

def start_tmm(sec):
    while True:
        time.sleep(sec)
        ri=random.randint(1,100)
        uid=str(int(round(time.time() * 1000))*10 + ri)
        _id=str(int(round(time.time() * 1000))*10 + ri)
        ptime=int(round(time.time() * 1000))
        val={}
        val["message"]={'type':'tmmProduce','producer':'producer-service-name1','ptime':ptime,'uid':uid ,'st':1}
        es_put("tmm-logs","source",_id,val)

        _id=str(int(round(time.time() * 1000))*10 + ri)
        ctime=ptime + random.randint(100,10000)
        val.clear()
        val["message"]={'type':'tmmConsume','producer':'producer-service-name1','ptime':ptime,'ctime':ctime ,'uid':uid ,'consumer':'consumer-service-name1'}
        es_put("tmm-logs","source",_id,val)

        _id=str(int(round(time.time() * 1000))*10 + ri)
        ctime=ptime + random.randint(100,10000)
        val.clear()
        val["message"]={'type':'tmmConsume','producer':'producer-service-name1','ptime':ptime,'ctime':ctime ,'uid':uid ,'consumer':'consumer-service-name2'}
        if ctime % 11 != 0 :
            es_put("tmm-logs","source",_id,val)

try:
    thread.start_new_thread(start_tmm,(1,))
except:
    print "ERROR:net unable to start thread tmm"
while 1:
    pass

