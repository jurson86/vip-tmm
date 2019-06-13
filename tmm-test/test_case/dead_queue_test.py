import time
from test_case.admin_base import AdminBase
from admin.admin_tools import Admin

"""
死信队列测试
"""
class DeadQueueCase(AdminBase):

    # 申明死信队列
    def arguments_(self):
        return {"x-dead-letter-exchange":"dle-tmm", "x-message-ttl":21600000,
                "x-dead-letter-routing-key":"transaction-producer"}

    # 订阅队列
    QUEUE_ = "py_dead_queue"
    # 订阅交换机
    EXCHANGE_ = "py_dead_exchange"
    # 交换机类型
    EXCHANGE_TYPE_ = "fanout"

    def __init__(self, methodName='runTest'):
        super(DeadQueueCase, self).__init__(queue=self.QUEUE_,
                                            exchange=self.EXCHANGE_,
                                            exchange_type=self.EXCHANGE_TYPE_,
                                            methodName=methodName)
        self._admin = Admin()

    """
      4.1 测试死信拒绝，admin是否  收集死信消息
    """
    def test_dead_queue_1(self):
        # 清理数据库中可能影响的数据
        self._mysql.delete("DELETE FROM t_transaction_state WHERE dlq_name ='dlq--TMM--py_dead_exchange--py_dead_queue'")
        self._mysql.end()
        batch = 10
        send_uids = self._send_default_mq_msg(batch)
        # 等消息全发送到队列中
        time.sleep(5)
        # 拒绝消息
        self._reject_default_msg(batch)
        time.sleep(15)
        # 查看消息是否都到admin中
        deadList = self._admin.queyDeadQueue()
        check = None
        for dead in deadList:
            if dead["name"] == "dlq--TMM--py_dead_exchange--py_dead_queue":
                check = dead
        self.assertTrue(check is not None, "admin死信消息收集失败，请检查admin是否监控此mq集群....")
        self.assertTrue(check["messages"] == batch, "接受到的消息数和发出的消息不等....")
        print("成功 ------ 4.1  测试死信拒绝，admin是否  收集死信消息")

    """
         4.2 死信消息重发
    """
    def test_dead_queue_2(self):
        batch = 10
        # 检查死信
        deadList = self._admin.queyDeadQueue()
        check = None
        for dead in deadList:
            if dead["name"] == "dlq--TMM--py_dead_exchange--py_dead_queue":
                check = dead
        self.assertTrue(check is not None, "admin死信消息收集失败，请检查admin是否监控此mq集群....")
        # 重发
        self._admin.resend_dead_msg("dlq--TMM--py_dead_exchange--py_dead_queue")
        self.assertTrue(check["messages"] == batch, "接受到的消息数和发出的消息不等....")
        time.sleep(10)
        # 接受消息
        revice_uids = self._get_default_mq_msg(batch=batch)
        self.assertTrue(len(revice_uids) == batch, "接受到的消息数和发出的消息不等....")
        print("成功 ------ 4.2  死信消息重发")