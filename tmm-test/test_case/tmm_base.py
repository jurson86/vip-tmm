from test_case.abs_test import AbsTestBase

"""
TMM producer 测试基类
"""
class TmmBase(AbsTestBase):

    def __init__(self, queue, exchange, exchange_type, ip, methodName='runTest'):
        super(TmmBase, self).__init__(methodName)
        self.queue = queue
        self.exchange = exchange
        self.exchange_type = exchange_type
        # 生产者的
        self.ip = ip

    def queue_(self):
        return self.queue

    def exchange_(self):
        return self.exchange

    def exchange_type_(self):
        return self.exchange_type

    def stop_ip_(self):
        return self.ip