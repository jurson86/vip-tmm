from abc import abstractmethod

from mysql.mysql_tools import Mysql
from rabbitmq.rabbit_tools import RabbitTools
import unittest
from linux.ssh import SSH
import urllib.request
import config.config
import uuid
import demjson
import os
import platform

class AbsTestBase(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls._mysql = Mysql()
        # mq_key--->Rabbitmq
        cls._mq_dict = RabbitTools.all_rabbit_dict
        cls.default_rabbit_key = RabbitTools.default_rabbit_key
        if (platform.system() == 'Windows'):
            path = os.path.dirname(os.path.realpath(__file__))[0:-9] + "linux\\key"
        elif(platform.system() == 'Linux'):
            path = os.path.dirname(os.path.realpath(__file__))[0:-9] + "linux/key"
        else:
            path = os.path.dirname(os.path.realpath(__file__))[0:-9] + "linux/key"
        cls._ssh = SSH(path)
        # 进去到mq服务器
        cls._ssh.exec_command_by_root("cd /sbin")

    @classmethod
    def tearDownClass(cls):
        # 归还连接
        cls._mysql.dispose()
        cls._ssh.close()

    @abstractmethod
    def queue_(self):
        pass

    @abstractmethod
    def exchange_(self):
        pass

    @abstractmethod
    def exchange_type_(self):
        pass

    def arguments_(self):
        return {}

    @abstractmethod
    def stop_ip_(self):
        pass

    def setUp(self):
        # 删除交换机和队列
        for mq in self._mq_dict.values():
            try:
                mq.queue_delete(self.queue_())
                mq.exchange_delete(self.exchange_())
            except Exception:
                pass
        self.stop_ip_()
        self.assertTrue(len(self._mq_dict) >= 0, "没有读取到集群MQ的配置")
        self.assertTrue(self.default_rabbit_key is not None, "producer没有默认的MQ集，请检查config.configMQHOST")
        for mq in self._mq_dict.values():
            mq.declare_exchange_queue_bind(self.exchange_(), self.exchange_type_(), self.queue_(), self.arguments_())

    def tearDown(self):
        self.stop_ip_()
        # 删除交换机和队列
        for mq in self._mq_dict.values():
            mq.queue_delete(self.queue_())
            mq.exchange_delete(self.exchange_())

    """
      @summary: 向producer 配置下的所有mq集群发送消息
      @:param: batch_size:每个集群发送消息数量, state 0 提交, 1取消, 2 模拟事务中断
    """
    def _send_all_mq_msg(self, batch_size, state=None):
        send_list = []
        for mq_key in self._mq_dict:
            send_list = send_list + self._send_mq_msg_by_key(batch_size, mq_key, state)
        return send_list

    """
        @summary: default_mq 发送消息
        @:return: 返回uuid<type:list>, state 0 提交, 1取消, 2 模拟事务中断
    """
    def _send_default_mq_msg(self, batch_size, state=None):
        return self._send_mq_msg_by_key(batch_size, self.default_rabbit_key, state)

    """
     @summary: 调用producer 批量（batch_size）发送 mq消息到mq集群（default_mq）
     @:return: 返回uuid<type:list>,state 0 提交, 1取消, 2 模拟事务中断
    """
    def _send_mq_msg_by_key(self, batch_size, mq_key, state=None):
        send_uids = []
        for n in range(batch_size):
            uid = str(uuid.uuid1())
            send_uids.append(uid)
            if state is None:
                self._send_NTrans_mq_msg_by_uid(mq_key, uid)
            else:
                self._send_Trans_mq_msg_by_uid(mq_key, uid, state)
        return send_uids

    """
     @summary: 非事务消息 向mq_key 的集群发送uid的消息
     @:return: 返回uuid<type:list>
    """
    def _send_NTrans_mq_msg_by_uid(self, mq_key, uid):
        req = urllib.request.Request(config.config.PRODUCER_URL.format(mq_key, uid, self.exchange_(), self.exchange_type_()))
        with urllib.request.urlopen(req) as response:
            #print(response.read())
            pass
    """
     @summary: 事务消息 向mq_key 的集群发送uid的消息
     @:param:  state state 0 提交, 1取消, 2 模拟事务中断
     @:return: 返回uuid<type:list>
    """
    def _send_Trans_mq_msg_by_uid(self, mq_key, uid, state):
        req = urllib.request.Request(config.config.TRANS_PRODUCER_UR.format(mq_key, uid, state, self.exchange_(), self.exchange_type_()))
        with urllib.request.urlopen(req) as response:
            #print(response.read())
            pass

    """
    @summary: 禁止指定IP访问默认MQ服务器
    """
    def _stop_rabbit_mq(self):
        # 关闭mq服务器 ./rabbitmqctl stop  直接停机很慢，所以改为使用黑白名单
        self._ssh.exec_command_by_root("iptables -I INPUT -s %s/32 -p tcp --dport 5672  -j DROP" % (self.stop_ip_(),))

    """
    @summary: 恢复指定IP访问默认MQ服务器   
    """
    def _start_rabbit_mq(self):
        # 重启mq服务器 ./rabbitmq-server -detached
        #self._ssh.exec_command_by_root("iptables -I INPUT -s %s/32 -p tcp --dport 5672  -j ACCEPT" % (self.stop_ip_(),))
        self._ssh.exec_command_by_root("iptables -F")

    """
     @summary: 拉取默认mq集群(config.config.MQHOST)的TestBase.QUEUE batch条消息
    """
    def _get_default_mq_msg(self, batch):
        return self._get_mq_msg_by_key(batch)

    """
         @summary: 拉取mq_key的mq集群(config.config.MQHOST)的TestBase.QUEUE batch条消息
    """
    def _get_mq_msg_by_key(self, batch, mq_key=None):
        mq_key = self.default_rabbit_key if mq_key is None else mq_key
        receive_uids = []
        mq = self._mq_dict[mq_key]
        for n in range(batch):
            msg = mq.basic_get(self.queue_())
            if msg[-1] is not None:
                txt = demjson.decode(msg[-1])
                receive_uids.append(txt["uid"])
        return receive_uids

    """
        
    """
    def _reject_default_msg(self, batch):
        mq_key = self.default_rabbit_key
        mq = self._mq_dict[mq_key]
        for n in range(batch):
            asd = mq.basic_reject(self.queue_())
            #print(asd)


    """
    @summary: 向 producer配置下的所有集群分别拉取batch条消息
    """
    def _get_all_mq_msg(self, batch):
        receive_uids = []
        for mq_key in self._mq_dict:
            receive_uids = receive_uids + self._get_mq_msg_by_key(batch, mq_key)
        return receive_uids


    """
    @summary: 断言两个非空列表 元素是否一样
    """
    def assertCompareList(self, list1, list2, msg):
        self.assertTrue(len(list1) != 0, "集合为空，请使用其他函数判断")
        self.assertTrue(len(list2) != 0, "集合为空，请使用其他函数判断")
        diff = [i for i in list1 if i not in list2]
        diff2 = [i for i in list2 if i not in list1]
        self.assertListEqual(diff, [], msg)
        self.assertListEqual(diff2, [], msg)


if __name__ == "__main__":
    path = os.path.dirname(os.path.realpath(__file__))[0:-9] + "linux\\key"
    print(path)