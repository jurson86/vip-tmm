import time
from test_case.tmm_base import TmmBase
import config.config


"""
事务消息测试
"""
class TransCase(TmmBase):

    # 订阅队列
    QUEUE = "py_trans_queue"
    # 订阅交换机
    EXCHANGE = "py_exchange"
    # 交换机类型
    EXCHANGE_TYPE = "fanout"

    def __init__(self, methodName='runTest'):
        super(TransCase, self).__init__(queue=self.QUEUE,
                                    exchange=self.EXCHANGE,
                                    exchange_type=self.EXCHANGE_TYPE,
                                    ip=config.config.IP,
                                    methodName=methodName)

    """
    2.1 事务消息常规流程。发送3条提交消息（不同mq集群），3条取消消息（不同集群）。检查是否成功接收。
    """
    def test_trans_1(self):
        batch = 3
        # 发送3条提交消息
        send_uids = self._send_all_mq_msg(batch, 0)
        # 发送3条取消消息
        cancel_uids = self._send_all_mq_msg(batch, 1)
        self.assertTrue(len(cancel_uids) != 0, "事务消息取消消息发送失败....")

        time.sleep(1)
        # 接受消息
        recevie_uids = self._get_all_mq_msg(batch*2)

        """检查是否全部收到消息"""
        self.assertCompareList(send_uids, recevie_uids, "事务消息消费失败....")
        print("成功 ------ 2.1 事务消息常规流程。发送3条提交消息（不同mq集群），3条取消消息（不同集群）。检查是否成功接收。")


    """
    2.2 事务消息异常流程。发送消息过程中，mq服务器down机。发送完成后，重启服务器，查看是否接收成功。
    """

    def test_trans_2(self):
        batch = 3
        # 向默认服务器发送3条提交消息
        send_uids = self._send_default_mq_msg(batch, 0)
        # 服务器宕机
        self._stop_rabbit_mq()
        # 再次发送3条消息
        send_uids = send_uids + self._send_default_mq_msg(batch, 0)
        self.assertTrue(len(send_uids) != 0, "事务消息取消消息发送失败....")
        # 等待producer超时
        time.sleep(5)
        # 重启服务器
        self._start_rabbit_mq()
        # 等待producer重试
        time.sleep(10)

        # 接受消息
        receive_uids = self._get_default_mq_msg(batch*2)

        """检查是否全部收到消息"""
        self.assertCompareList(send_uids, receive_uids, "事务消息消费失败....")
        print("成功 ------ 2.2 事务消息异常流程。发送消息过程中，mq服务器down机。发送完成后，重启服务器，查看是否接收成功。")

    """
    2.3 事务消息异常流程。向producer配置的mq发送一条消息只有开始日志，没有结束日志。
    """
    def test_trans_3(self):
        batch = 1
        # 发送3条崩溃且提交的消息
        send_uids = self._send_all_mq_msg(batch, 2)
        self.assertTrue(len(send_uids) != 0, "事务消息提交消息发送失败....")

        # 等tmm-admin回调
        time.sleep(40)
        receive_uids = self._get_all_mq_msg(batch)
        self.assertTrue(len(receive_uids) != 0, "事务消息崩溃消息接受失败, 提示：检查调度中心是否启动....")

        """检查是否全部收到消息"""
        self.assertCompareList(send_uids, receive_uids, "事务消息消费失败,请检查tmm-admin是否监听了producer配置的所有mq....")
        print("成功 ------ 2.3 事务消息异常流程。向producer配置的mq发送一条消息只有开始日志，没有结束日志。")

    """
    2.4 事务消息异常流程。发送的消息只有开始日志，没有结束日志。通过tmm-admin回调。查看是否收到消息。
    """
    def test_trans_4(self):
        batch = 3
        # 向所有服务器发送3条提交消息
        send_uids = self._send_all_mq_msg(batch, 0)
        # 发送3条崩溃且提交的消息
        send_uids = send_uids + self._send_all_mq_msg(batch, 2)
        self.assertTrue(len(send_uids) != 0, "事务消息提交消息发送失败....")

        # 等tmm-admin回调
        time.sleep(40)

        receive_uids = self._get_all_mq_msg(batch*3)
        self.assertTrue(len(receive_uids) != 0, "事务消息崩溃消息接受失败....")

        """检查是否全部收到消息"""
        self.assertCompareList(send_uids, receive_uids, "事务消息消费失败,请检查tmm-admin是否监听了producer配置的所有mq或者检查调度中心是否启动....")
        print("成功 ------ 2.4 事务消息异常流程。发送的消息只有开始日志，没有结束日志。通过tmm-admin回调。查看是否收到消息。")


