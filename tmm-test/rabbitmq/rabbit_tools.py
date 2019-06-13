import config.config
import pika
from rabbitmq.pooled_mq import PooledMQ
import urllib.request
import demjson
import functools

""" 
host=config.config.MQHOST, port=config.config.MQPORT,
                 virtual_host=config.config.MQVHOST, username=config.config.MQUSER,
                 password=config.config.MQPWD
"""





class RabbitMq:
    """连接池"""
    __pool = None

    def __init__(self, pool):
        self.__pool = pool

    # 断线重连AOP切面
    def listener(self):
        def log(func):
            @functools.wraps(func)
            def wrapper(*args, **kw):
                try:
                    f = func(*args, **kw)
                except Exception:
                    rabbitmq = args[0]
                    rabbitmq._RabbitMq__pool.reset()
                    f = func(*args, **kw)
                return f

            return wrapper
        return log

    def __doExecute(self, func):
        conn = self.__pool.create_connection()
        try:
            channel = conn.channel()
            result = func(channel)
        except Exception:
            print("rabbitmq 获取连接异常,回收连接...." + Exception)
        finally:
            self.__pool.close_connection(conn)
        return result

    """
     @summary: 申明交换机
     @return: boolean
    """
    @listener(object)
    def exchange_declare(self, exchange, exchange_type, durable):
        return self.__doExecute(
            lambda channel: channel.exchange_declare(exchange=exchange, exchange_type=exchange_type, durable=durable))

    """
      @summary: 发送消息
      @return: boolean
    """

    @listener(object)
    def basic_publish(self, exchange, routing_key, body):
        properties = pika.BasicProperties()
        properties.content_type = "text/plain"
        return self.__doExecute(lambda channel: channel.basic_publish(exchange=exchange, routing_key=routing_key, body=body,
                                            properties=properties))

    """
     @summary: 申明队列
     @return: boolean
    """

    @listener(object)
    def queue_declare(self, queue, durable, arguments={}):
        return self.__doExecute(lambda channel: channel.queue_declare(queue=queue, durable=durable, arguments=arguments))

    """
    @summary: 申明绑定
    @return: boolean
    """

    @listener(object)
    def queue_bind(self, queue, exchange, routing_key="", ):
        return self.__doExecute(lambda channel: channel.queue_bind(queue=queue, exchange=exchange, routing_key=routing_key))

    """
    @summary: 申明绑定交换机和队列
    @return: boolean
    """

    @listener(object)
    def declare_exchange_queue_bind(self, exchange, exchange_type, queue, arguments={}, routing_key=""):
        self.queue_declare(queue=queue, durable=True, arguments=arguments)
        self.exchange_declare(exchange=exchange, exchange_type=exchange_type, durable=True)
        self.queue_bind(queue=queue, exchange=exchange, routing_key=routing_key)

    """
    @summary: 删除队列
    @return: boolean
    """

    @listener(object)
    def queue_delete(self, queue):
        self.__doExecute(lambda channel: channel.queue_delete(queue))

    """
        @summary: 删除交换机
        @return: boolean
    """

    @listener(object)
    def exchange_delete(self, exchange):
        self.__doExecute(lambda channel: channel.exchange_delete(exchange))

    """
     @summary: get消息队列消息
     @return: 元组,消息内容
    """

    @listener(object)
    def basic_get(self, queue, no_ack=True):
        return self.__doExecute(lambda channel: channel.basic_get(queue, no_ack))

    """
         @summary: 拒绝消息
    """
    def basic_reject(self, queue):
        conn = self.__pool.create_connection()
        try:
            channel = conn.channel()
            msg = channel.basic_get(queue, False)
            delivery_tag = msg[0].delivery_tag
            result = channel.basic_reject(delivery_tag, False)
        except Exception:
            print("rabbitmq 获取连接异常,回收连接...." + Exception)
        finally:
            self.__pool.close_connection(conn)
        return result

class RabbitTools:
    # 保存了从producer配置中所有的连接信息 <type dict>
    # mq_key<type string>--->rabbitmq<type Rabbitmq>
    all_rabbit_dict = {}

    # 保存了默认的mq服务器配置config.config.MQHOST 的连接
    default_rabbit_key = None

    """
    根据配置信息创建和mq的连接
    """

    @staticmethod
    def __create_producer_mq_connection():
        result_dict = {}
        try:
            """拉取生产者的mq配置信息"""
            req = urllib.request.Request(config.config.PRODUCER_MQ_SETTING)
            with urllib.request.urlopen(req) as response:
                mqJson = demjson.decode(response.read())
                for mq in mqJson:
                    value = mqJson[mq]
                    tmp_dict = {'host': value['host'], 'port': value['port'],
                                'username': value['username'], 'password': value['password']}
                    result_dict[mq] = tmp_dict
        except Exception:
            print("拉取producer,mq集群配置列表失败，请检查producer是否启动或者config.config.URL参数.....")
            raise Exception
        return result_dict

    """
    通过producer配置信息创建和rabbitmq服务器的连接
    """

    @staticmethod
    def create_rabbitmq_by_producer():
        if len(RabbitTools.all_rabbit_dict) == 0:
            mq_dict = RabbitTools.__create_producer_mq_connection()
            for mq in mq_dict:  # 建立连接池
                connection_info = mq_dict[mq]
                host = connection_info['host']
                pooledmq = PooledMQ(host=host, port=connection_info['port'],
                                    virtual_host=config.config.MQVHOST, username=connection_info['username'],
                                    password=connection_info['password'])
                RabbitTools.all_rabbit_dict[mq] = RabbitMq(pooledmq)
                if host == config.config.MQHOST:
                    RabbitTools.default_rabbit_key = mq


# 初始化 RabbitTools， 其他地方可以直接使用 RabbitTools.all_rabbit_dict
RabbitTools.create_rabbitmq_by_producer()

if __name__ == '__main__':
    #
    pooledmq = PooledMQ(host="10.100.11.73", port=5672,
                        virtual_host="TMM", username="root",
                        password="root")
    rabbitMq = RabbitMq(pooledmq)
    rabbitMq.exchange_declare(exchange="ads", exchange_type="fanout", durable="true")


    # RabbitMq 测试
    pooledmq = PooledMQ(host="10.100.11.73", port=5672,
                        virtual_host="TMM", username="root",
                        password="root")
    print(id(pooledmq))
    rabbitMq = RabbitMq(pooledmq)
    print(rabbitMq.__dict__)
    sAS = rabbitMq.basic_get("py_nTrans_queue")
    print(sAS)

    # RabbitTools 测试
    #print(RabbitTools.all_rabbit_dict)
    #RabbitTools.all_rabbit_dict[RabbitTools.default_rabbit_key].basic_get("py_nTrans_queue")
