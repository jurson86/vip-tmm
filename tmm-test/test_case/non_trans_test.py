#!/user/bin/env python

import time
from test_case.tmm_base import TmmBase
import config.config

class NonTransCase(TmmBase):

    # 订阅队列
    QUEUE_ = "py_non_trans_queue"
    # 订阅交换机
    EXCHANGE_ = "py_non_exchange"
    # 交换机类型
    EXCHANGE_TYPE_ = "fanout"

    def __init__(self, methodName='runTest'):
        super(NonTransCase, self).__init__(queue=self.QUEUE_,
                                       exchange=self.EXCHANGE_,
                                       exchange_type=self.EXCHANGE_TYPE_,
                                       ip=config.config.IP,
                                       methodName=methodName)

    """
    1.1 非事务消息的常规流程。发送1条(每个mq集群都发送)非事务消息，消费消息。检查是否成功接收。
    """

    def test_non_trans_1(self):
        send_uids = self._send_all_mq_msg(1)
        self.assertTrue(len(send_uids) != 0, "请检查send_uids")
        time.sleep(8)

        """消费消息"""
        receive_uids = self._get_all_mq_msg(1)
        self.assertTrue(len(receive_uids) != 0, "请检查receive_uids")

        """检查是否全部收到消息"""
        self.assertCompareList(send_uids, receive_uids, "非事务消息消费失败....")
        print("成功 ------ 1.1 非事务消息的常规流程。发送3条非事务消息，消费消息。检查是否成功接收。")

    """
    1.2 非事务消息异常流程。在发送100条消息的过程中，mq服务器down机，发送消息完成后，重启服务器。消费队列消息检查是否成功接收。
    """

    def test_non_trans_2(self):
        batch = 5
        # 选择默认配置的mq集群发送100条消息
        send_uids = self._send_default_mq_msg(batch)
        # 关闭mq服务器
        self._stop_rabbit_mq()
        send_uids = send_uids + self._send_default_mq_msg(batch)
        self.assertTrue(len(send_uids) != 0, "请检查send_uids")

        # 等待producer超时
        time.sleep(5)
        # 重启mq服务器
        self._start_rabbit_mq()
        # 等待服务器重启
        time.sleep(10)
        receive_uids = self._get_default_mq_msg(batch * 2)
        self.assertTrue(len(receive_uids) != 0, "请检查receive_uids")

        """检查是否全部收到消息"""
        self.assertCompareList(send_uids, receive_uids, "非事务消息消费失败....")
        print("成功 ------ 1.2 非事务消息异常流程。在发送10条消息的过程中，mq服务器down机，发送消息完成后，重启服务器。消费队列消息检查是否成功接收。")

    """
        1.3 非事务消息异常流程。在发送10条消息的过程中，mq服务器down机，发送消息完成后，重启服务器。消费队列消息检查是否成功接收。
    """

    def test_non_trans_3(self):
        batch = 5
        # 关闭服务器端口
        self._stop_rabbit_mq()
        # 发送一百条消息
        send_uids = self._send_default_mq_msg(batch)
        self.assertTrue(len(send_uids) != 0, "请检查send_uids")
        # 等待producer超时
        time.sleep(5)
        # 重启mq服务器
        self._start_rabbit_mq()
        # 等待服务器重启，10s 足够的时间让produer 发生消息
        time.sleep(10)
        receive_uids = self._get_default_mq_msg(batch)
        self.assertTrue(len(receive_uids) != 0, "请检查receive_uids")

        """检查是否全部收到消息"""
        self.assertCompareList(send_uids, receive_uids, "非事务消息消费失败....")
        print("成功 ------ 1.3 非事务消息异常流程。在发送10条消息的过程中，mq服务器down机，发送消息完成后，重启服务器。消费队列消息检查是否成功接收。。")
