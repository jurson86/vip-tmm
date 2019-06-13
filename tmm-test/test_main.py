import unittest, os
from test_case.non_trans_test import NonTransCase
from test_case.trans_test import TransCase
from test_case.admin_test import AdminTestCase
from test_case.dead_queue_test import DeadQueueCase
from HtmlTestRunner import HTMLTestRunner
import send_email, platform
from BSTestRunner import BSTestRunner


# 构造测试集
# suite = unittest.TestSuite()
# # 一 非事务消息 测试用例
# non_trans_tests = [NonTrans('test_non_trans_1'), NonTrans('test_non_trans_2'), NonTrans('test_non_trans_3')]
# #
# # 二 事务消息 测试用例
# trans_tests = [Trans('test_trans_1'), Trans('test_trans_2'), Trans('test_trans_3'), Trans('test_trans_4')]
# # 三 admin  测试用例
# admin_tests = [AdminTestCase('test_admin_1'), AdminTestCase('test_admin_2'), AdminTestCase('test_admin_3'),
#                AdminTestCase('test_admin_4'), AdminTestCase('test_admin_5'), AdminTestCase('test_admin_6'),
#                AdminTestCase('test_admin_7')]
# # 四 死信 测试用例
# dead_tests = [DeadQueue('test_dead_queue_1'), DeadQueue('test_dead_queue_2')]
#
# suite.addTests(non_trans_tests + trans_tests + admin_tests + dead_tests)
# suite.addTests(trans_tests)
# suite.addTests(admin_tests)
# suite.addTests(dead_tests)

"""
 本次测试用例前提条件：
  1.message-producer启动并且能访问，并且在config中配置正确的ip地址和访问路径
  2.tmm-admin 启动并且能成功访问，并且在config中配置正确的ip地址和访问路径
  3.message-producer的配置中的mq集群需要能成功访问，并需要检查iptables是否限制本测试用例的ip地址访问（可能测试用例非正常中断，导致黑名单未删除）
  4.mqsql服务器能访问
  5.tmm-admin配置mq集群能访问
"""

def create_dir():
    isWind = False
    if (platform.system() == 'Windows'):
        path = os.path.dirname(os.path.realpath(__file__)) + "\\test_report"
        isWind = True
    else:
        path = os.path.dirname(os.path.realpath(__file__)) + "/test_report"
    is_exists = os.path.exists(path)
    if not is_exists:
        os.makedirs(path)
    file = path + "\HTMLReport.html" if isWind else path + "/HTMLReport.html"
    return file, path


if __name__ == '__main__':
    suite1 = unittest.TestLoader().loadTestsFromTestCase(NonTransCase)
    suite2 = unittest.TestLoader().loadTestsFromTestCase(TransCase)
    suite3 = unittest.TestLoader().loadTestsFromTestCase(AdminTestCase)
    suite4 = unittest.TestLoader().loadTestsFromTestCase(DeadQueueCase)
    suite = unittest.TestSuite([suite1, suite2, suite3, suite4])
    #unittest.TextTestRunner(verbosity=2).run(suite)


    #执行测试
    # runner = unittest.TextTestRunner()
    # runner.run(suite)
    file, path = create_dir()
    with open(file, "w") as f:
        runner = BSTestRunner(stream=f, title='tmm-test',
                              description='tmm-test', verbosity=2)
        # 执行测试
        runner.run(suite)
    #new_report = send_email.new_report(file)
    send_email.send_file(file)  # 发送测试报告
