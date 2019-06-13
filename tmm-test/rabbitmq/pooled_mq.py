import queue
import pika
import time
import _thread

"""
  rabbitmq 连接池
"""


class PooledMQ:

    def do_create_connection(self):
        for index in range(self.__cached):
            try:
                credentials = pika.PlainCredentials(self.__username, self.__password)
                conn_params = pika.ConnectionParameters(host=self.__host, port=self.__port,
                                                        virtual_host=self.__virtual_host, credentials=credentials)
                conn_broker = pika.BlockingConnection(conn_params)
            except Exception:
                print("无法连接到mq服务器,请检查rabbitmq服务器是否启动， {}".format(self.__host))
                raise Exception
            self.__queue.put(conn_broker)

    def __init__(self, host, port, virtual_host, username, password, cached=2):
        self.__queue = queue.Queue()
        self.__cached = cached
        self.__host = host
        self.__port = port
        self.__virtual_host = virtual_host
        self.__username = username
        self.__password = password
        self.do_create_connection()

    def reset(self):
        size = self.__queue.qsize()
        for x in range(size):
            self.__queue.get()
        self.do_create_connection()

    def create_connection(self):
        return self.__queue.get()

    def close_connection(self, connection):
        return self.__queue.put(connection)



"""
===============================================
              以下为脚本自测试代码
===============================================
"""

def print_time(pooledMQ, delay):
    conn_broker = pooledMQ.get_connection()
    print("conn_broker:{} \n".format(id(conn_broker)))
    channel = conn_broker.channel()
    channel.exchange_declare(
        exchange="py_test",
        exchange_type='fanout',
        durable=True)
    properties = pika.BasicProperties()
    properties.content_type = "text/plain"
    channel.basic_publish(exchange="py_test", routing_key="", body="派生测试代码",
                          properties=None)


    time.sleep(delay)
    print("执行完成")
    pooledMQ.close_connection(conn_broker)


if __name__ == '__main__':
    pooledMQ = PooledMQ()
    _thread.start_new_thread( print_time, ( pooledMQ, 20, ) )
    _thread.start_new_thread( print_time, ( pooledMQ, 4, ) )
    time.sleep(1000)
