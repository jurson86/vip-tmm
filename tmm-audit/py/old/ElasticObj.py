#coding:utf8
import os
import time
from datetime import datetime
from elasticsearch import Elasticsearch
from elasticsearch.helpers import bulk

class ElasticObj:
    def __init__(self,index_name,index_type,ip="127.0.0.1",v_port=9200,user="no",pwd=""):
        '''
        :param index_name: 索引名称
        :param index_type: 索引类型
        '''
        self.index_name =index_name
        self.index_type = index_type
        if user == "no" :
            # 无用户名密码状态
            self.es = Elasticsearch([ip],port=v_port)
        else:
            #用户名密码状态
            self.es = Elasticsearch([ip],port=v_port,http_auth=(user, pwd))
        
    def get_index(self):
        return self.index_name

    def get_type(self):
        return self.index_type

    def set_index(self,index_name):
        self.index_name = index_name

    def set_type(self,index_type):
        self.index_type = index_type

    def create_index(self,index_mapping):
        '''
        :param ex: Elasticsearch对象
        :index_mapping  ex: 
                 {
                    "properties": {
                        "title": {
                            "type": "text",
                            "index": True,
                            "analyzer": "ik_max_word",
                            "search_analyzer": "ik_max_word"
                        },
                        "date": {
                            "type": "text",
                            "index": True
                        },
                        "source": {
                            "type": "text",
                            "index": "not_analyzed"
                        }
                    }
                }
        :return:
        '''
        #创建映射
        _index_mappings = {
            "mappings": {
                self.index_type: index_mapping
            }
        }
        if self.es.indices.exists(index=self.index_name) is not True:
            res = self.es.indices.create(index=self.index_name, body=_index_mappings)
            return res


    def put_index_datas(self,list_data):
        ACTIONS = []
        i = 1
        for line in list_data:
            action = {
                "_index": self.index_name,
                "_type": self.index_type,
                "_source": line
            }
            i += 1
            ACTIONS.append(action)
            # 批量处理
        success, _ = bulk(self.es, ACTIONS, index=self.index_name, raise_on_error=True)
        print('Performed %d actions' % success)
        return success

    def put_index_bulks(self,list_bulks):
        success, _ = bulk(self.es, list_bulks, index=self.index_name, raise_on_error=True)
        print('Performed %d actions' % success)
        return success

    def put_index_data(self,data):
        res = self.es.index(index=self.index_name, doc_type=self.index_type, body=data)
        return res

    def delete_index_data(self,_id):
        '''
        删除索引中的一条
        :param _id:
        :return:
        '''
        res = self.es.delete(index=self.index_name, doc_type=self.index_type, id=_id)
        print res

    def get_data_by_id(self,_id):
        res = self.es.get(index=self.index_name, doc_type=self.index_type,id=_id)
        return res

    def get_data_by_body(self,sbody):
        searched = self.es.search(index=self.index_name, doc_type=self.index_type, body=sbody)
        return searched

'''
test_map={
                    "properties": {
                        "title": {
                            "type": "text",
                            "index": True,
                        },
                        "date": {
                            "type": "text",
                            "index": True
                        },
                        "source": {
                            "type": "text",
                        }
                    }
                }

docs=[{"title1":"hello","source":"world"},{"title":"hello1","date":"2018","source":"world"},{"title":"hello2","date":"2018","source":"world"}]
sbody={
            "query": {
                "match_all": {}
            }
        }


update_info =[
{
    '_op_type': 'update',
    '_index': 'tmm',
    '_type': 'tmm',
    '_id': 1,
    'doc': {'age': 80}
},
{
    '_op_type': 'update',
    '_index': 'tmm',
    '_type': 'tmm',
    '_id': 3,
    'doc': {'age': 10}
},
{
    '_op_type': 'update',
    '_index': 'tmm',
    '_type': 'tmm',
    '_id': 2,
    'doc': {'age': 80}
}
]



if __name__ == '__main__':
    obj =ElasticObj("tmm","tmm","10.103.1.190")
    #obj.create_index(test_map)
    #obj.put_index_datas(docs)
    #ii = obj.get_data_by_body(sbody)
    ii = obj.put_index_bulks(update_info)
    print(ii)

'''

