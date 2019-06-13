#!/usr/bin/env python
# -*- coding:utf-8 -*-
import sys as i_sys, getopt as i_getopt
import thread as i_thread
import time as i_time
import threading as i_threading
i_sys.path.append(".")
from service import TmmAudit as i_tmm_audit
from config import Config as i_cfg
from util import TimeUtil as i_time_util
from es import ElasticObj as i_es_obj



#############################    MAIN  #######################################
def main(argv):
    try:
       opts, args = i_getopt.getopt(argv,"hs:t:g:",["size=","time=","group="])
    except i_getopt.GetoptError:
       print ("Usage: -s <batch_size>  -t <begin_time>  -g <tmm_producer_group> ")
       i_sys.exit(2)
    for opt, arg in opts:
       if opt == '-h':
           print ("Usage: -s <batch_size>  -t <begin_time> -g <tmm_producer_group>")
           i_sys.exit()
       elif opt in ("-s", "--size"):
           g_config.batch_size = arg
       elif opt in ("-t", "--time"):
           g_config.begin_time = arg
       elif opt in ("-g", "--group"):
           g_config.tmm_producers = g_config.tmm_groups[arg]

    if (len(g_config.tmm_producers) ==  0 ) :
       print ("invalid param: -g <tmm_producer_group>")
       print ("Usage: -s <batch_size>  -t <begin_time>  -g <tmm_producer_group> ")
       i_sys.exit()
    
    print 'batch size: ', g_config.batch_size
    print 'begin time: ', g_config.begin_time
    print 'tmm_producers: ', g_config.tmm_producers

def while_process_thread(p_tmm_producer):
    v_es_log = i_es_obj.ElasticObj("log4j","log4j",g_config.es_ip,g_config.es_port,g_config.es_user,g_config.es_pwd)
    v_es_audit = i_es_obj.ElasticObj("audit-tmm","audit-tmm",g_config.es_ip,g_config.es_port,g_config.es_user,g_config.es_pwd)
    v_tmm_producer = p_tmm_producer
    v_batch_size = g_config.batch_size
    v_begin_time =  int(i_time_util.get_ms() - (float(g_config.begin_time) * 60 * 60 * 1000))
    threading_local.audit = i_tmm_audit.TmmAudit(v_es_log,v_es_audit,v_tmm_producer,v_batch_size,v_begin_time)

    p_point_time = 0
    c_point_time = 0
    while (1) :
        try:
            i_time.sleep(5)
            threading_local.audit.producer_audit()
            i_time.sleep(0.5)
            threading_local.audit.consumer_audit()
            i_time.sleep(0.5)
            # 当前时间小于当前时间半小时， 或者无最新数据时，睡眠 60s
            if((p_point_time == threading_local.audit.producer_point_time  ) and (c_point_time == threading_local.audit.consumer_point_time))  :
                print("sleep 120s at : " + str(p_point_time))
                # 间隙时间重复处理
                i_time.sleep(120)
                v_hour = i_time.strftime('%H',i_time.localtime())
                v_day = i_time.strftime('%d',i_time.localtime())
                if str(threading_local.audit.producer_point_time).find("T") > 0:
                    v_pday = i_time.strftime('%d',i_time.localtime(i_time_util.get_utc_string_to_local_ts(threading_local.audit.producer_point_time)))
                    # 处理间隙时间
                    threading_local.audit.producer_point_time = i_time_util.get_ms_back_hours(g_config.repeat_time,(1000 * i_time_util.get_utc_string_to_local_ts(threading_local.audit.producer_point_time)))
                    threading_local.audit.consumer_point_time = i_time_util.get_ms_back_hours(g_config.repeat_time,(1000 * i_time_util.get_utc_string_to_local_ts(threading_local.audit.consumer_point_time)))
                else :
                    v_pday = i_time.strftime('%d',i_time.localtime(threading_local.audit.producer_point_time / 1000 ) )

                # 处理跨日期情况 变更新的索引
                if( v_hour >= 1 and v_day > v_pday):
                    time_utc = i_time.localtime()
                    today_time = int( i_time.mktime(i_time.strptime(i_time.strftime('%Y-%m-%d 00:00:00', time_utc),'%Y-%m-%d %H:%M:%S')) * 1000 )
                    print( str(v_day) + "==========================================" + str(v_pday) + "==="  + str(v_hour)  )
                    print time_utc
                    print today_time
                    threading_local.audit.producer_point_time = today_time 
                    threading_local.audit.consumer_point_time = today_time 

            # 控制睡眠时间
            p_point_time = threading_local.audit.producer_point_time
            c_point_time = threading_local.audit.consumer_point_time
        except Exception, e :
            print (str(e))
            i_time.sleep(10)


def start_audit_producer():
    for v_tmm_producer in g_config.tmm_producers :
        try:
            i_thread.start_new_thread(while_process_thread,(v_tmm_producer,))
        except Exception, e :
            print ("ERROR: unable to start thread : " + str(e))
    while 1:
        pass


if __name__ == '__main__':
    threading_local=i_threading.local()
    g_config = i_cfg.ConfigObj()
    main(i_sys.argv[1:])
    start_audit_producer()

