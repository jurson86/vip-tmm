import time
from test_case.admin_base import AdminBase
from admin.admin_tools import Admin


class AdminTestCase(AdminBase):

    # 订阅队列
    QUEUE_ = "py_admin_queue"
    # 订阅交换机
    EXCHANGE_ = "py_admin_exchange"
    # 交换机类型
    EXCHANGE_TYPE_ = "fanout"

    def __init__(self, methodName='runTest'):
        super(AdminTestCase, self).__init__(queue=self.QUEUE_,
                                       exchange=self.EXCHANGE_,
                                       exchange_type=self.EXCHANGE_TYPE_,
                                       methodName=methodName)
        self._admin = Admin()

    """
        3.1 异常消息正常流程。发送异常消息到tmm-admin，启动回调任务。能接受到发送的消息
    """
    def test_admin_1(self):
        # 1、通过默认mq 发送异常消息到tmm-admin
        batch = 1
        send_uids = self._send_default_mq_msg(batch, 2) # 有开始没结束消息
        self.assertTrue(len(send_uids) != 0, "admin发送失败....")
        time.sleep(35)
        # 2接受消息
        receive_uids = self._get_default_mq_msg(batch)
        self.assertTrue(len(send_uids) != 0, "admin接受消息失败....")
        """检查是否全部收到消息"""
        self.assertCompareList(send_uids, receive_uids, "异常消息正常流程。发送异常消息到tmm-admin，启动回调任务。能接受到发送的消息....")
        print("成功 ------ 3.1 异常消息正常流程。发送异常消息到tmm-admin，启动回调任务。能接受到发送的消息")


    """
       3.2 消息废弃
    """
    def test_admin_2(self):
        # 发送异常消息到admin
        exception_uid = self.send_exception_msg()
        time.sleep(15)
        # 读取异常消息
        sql_data = self.read_exception_msg_by_sql()
        pid = sql_data["pid"]
        # 废弃消息
        self._admin.discard_msg(pid)
        # 再次读取
        sql_data = self.read_exception_msg_by_sql()
        self.assertTrue(sql_data["message_state"] == 100, "消息废弃失败")
        print("成功 ------ 3.2 消息废弃")

    """
    3.3 手工check
    """
    def test_admin_3(self):
        # 发送异常消息到admin
        expection_uid = self.send_exception_msg()
        time.sleep(45)
        # 读取异常消息
        sql_data = self.read_exception_msg_by_sql()
        pid = sql_data["pid"]
        # 手工check
        self._admin.check_msg(pid, "py test check", 0)
        # 再次读取
        sql_data = self.read_exception_msg_by_sql()
        self.assertTrue(sql_data["message_state"] == 30, "消息手动check失败.....")
        print("成功 ------ 3.3 手工check")

    """
    3.4 死亡消息正常流程。mq down机，发送送状态的消息，变为死亡消息，重启mq服务器，触发重发能接受到消息。
    """
    def test_admin_4(self):
        # 向数据库模拟一条死亡消息
        pid = self._mysql.insert_one("INSERT INTO `trans`.`t_transaction_state` ( `uid`, `service_name`, `dlq_name`, `cluster_ip`, `mq_type`, `message_topic`, `message_state`, `message_send_threshold`, `message_send_times`, `message_next_send_time`, `presend_back_url`, `presend_back_method`, `presend_back_threshold`, `presend_back_send_times`, `presend_back_next_send_time`, `message`, `update_time`, `create_time`) VALUES ( '8104281e-bc7c-11e8-85d3-005056c00008', 'transaction-producer', NULL, '{\"ip\":\"10.100.11.73\",\"port\":5672,\"userName\":\"root\"}', '2', '{\"customExchange\":false,\"exchange\":\"py_admin_exchange\",\"exchangeType\":\"fanout\",\"ip\":\"default\",\"vHost\":\"TMM\"}', '22', '0', '1', '2018-09-20 10:31:01', '/test/api/check', 'POST', '6', %s, '2018-09-20 10:26:07', 'check 重发', '2018-09-20 10:31:05', '2018-09-20 10:26:07');", ("0",))
        self._mysql.end()
        # 重发
        self._admin.rerend_msg(pid)
        time.sleep(20)
        # 消息发送成功
        sql_data = self._mysql.get_one("SELECT * FROM t_transaction_state WHERE pid =%d" % (pid,))
        self.assertTrue(sql_data["message_state"] == 30, "消息手动check失败.....")
        print("成功 ------ 3.4 死亡消息正常流程。mq down机，发送送状态的消息，变为死亡消息，重启mq服务器，触发重发能接受到消息。")

    """
    3.5 消息清理。
    """
    def test_admin_5(self):
        # 读取完成消息
        sql_data = self._mysql.get_one("SELECT * FROM t_transaction_state WHERE message_state = 30 ORDER BY pid desc LIMIT 1")
        pid = sql_data["pid"]
        self._admin.clean_msg(pid)
        print("成功 ------ 3.5 消息清理。")

    """
    3.6 服务列表监控。发送1000条消息，监控producer各项指标是否正常。
    """
    def test_admin_6(self):
        batch = 40
        begin_result = self._admin.get_monitor_list()
        begin_rpc_size, begin_done_size = begin_result["rpc"], begin_result["done"]
        self._send_default_mq_msg(batch)
        time.sleep(30)
        end_result = self._admin.get_monitor_list()
        end_rpc_size, end_done_size = end_result["rpc"], end_result["done"]
        self.assertTrue(end_rpc_size > begin_rpc_size or end_done_size > begin_done_size, "mq集群配置测试失败....")
        print("成功 ------ 3.6 服务列表监控。发送1000条消息，监控producer各项指标是否正常。")

    """
     3.7.mq集群配置。添加删除mq集群，检查start队列的消费者数，间接判断是否监控该集群
    """
    def test_admin_7(self):
        # 获取mq配置列表
        begin_mq_list = self._admin.get_mq_cluster_setting_list()
        self.assertTrue(len(begin_mq_list) != 0, "tmm-admin 没有配置的mq集群....")
        mq_cluster = begin_mq_list[0]
        # 删除mq配置列表
        self._admin.remove_mq_cluster_by_key(mq_cluster["pid"])
        remove_mq_list = self._admin.get_mq_cluster_setting_list()
        self.assertTrue(len(remove_mq_list) == (len(begin_mq_list) - 1), "mq集群配置测试失败....")
        # 添加配置
        self._admin.add_mq_cluster(mq_cluster)
        end_mq_list = self._admin.get_mq_cluster_setting_list()

        self.assertTrue(len(end_mq_list) == len(begin_mq_list), "mq集群配置测试失败....")
        print("成功 ------ 3.7 mq集群配置。添加删除mq集群，检查start队列的消费者数，间接判断是否监控该集群")
