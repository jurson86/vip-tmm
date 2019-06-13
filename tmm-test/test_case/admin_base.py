from test_case.abs_test import AbsTestBase
import config.config

class AdminBase(AbsTestBase):

    def __init__(self, queue, exchange, exchange_type, methodName='runTest'):
        super(AdminBase, self).__init__(methodName)
        self.queue = queue
        self.exchange = exchange
        self.exchange_type = exchange_type
        # 生产者的
        self.ip = config.config.ADMIN_IP

    def queue_(self):
        return self.queue

    def exchange_(self):
        return self.exchange

    def exchange_type_(self):
        return self.exchange_type

    def stop_ip_(self):
        return self.ip

    """
    发送异常消息
    """
    def send_exception_msg(self):
        self._mysql.delete("DELETE FROM t_transaction_state WHERE uid = '00000000'")
        # 提交事务
        self._mysql.end()
        # 发送特定消息 uid="00000000" 只有开始日志没有结束日志
        send_uids = ["00000000"]
        self._send_Trans_mq_msg_by_uid(self.default_rabbit_key, "00000000", 2)
        return send_uids

    """
        读取该异常消息
    """
    def read_exception_msg_by_sql(self):
        sqlData = self._mysql.get_one("SELECT * FROM t_transaction_state WHERE uid ='00000000'")
        self.assertTrue(sqlData is not None, "发送消息失败")
        return sqlData

