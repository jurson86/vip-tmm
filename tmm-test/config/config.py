import re

"""mysql配置信息"""
DBHOST = "10.100.11.75"
DBPORT = 3306
DBUSER = "root"
DBPWD = "root"
DBNAME = "trans"
DBCHAR = "utf8"



"""rabbitmq配置信息"""
# 测试使用的mq默认的ip地址
#MQHOST="10.100.11.73"
MQHOST="tmm.admin.com"
# 测试使用的默认vhost
MQVHOST="TMM"
# 因为需要重启mq服务器，所以需要设置默认mq服务器的连接信息
#mq 服务端口
MQPORT=22522
#mq服务器用户
MQLINUXUSER="test"
#MQ服务器秘钥，配置在linux下的key里面
#key=....


"""producer生产者配置"""
IP="10.100.14.13" # producer 的IP地址，请使用明确的IP地址，不要使用127.0.0.1 或者localhost
URL="http://"+IP+ ":8090/test/"
PRODUCER_URL=URL+"api/producer/no/trans?ipName={}&uid={}&exchange={}&exchangeType={}"
#查询生产者mq配置的url
PRODUCER_MQ_SETTING=URL+"api/mq/cluster/list"
#事务消息发送接口
TRANS_PRODUCER_UR = URL + "api/producer/trans?ipName={}&uid={}&state={}&exchange={}&exchangeType={}"
MONITOR_URL = URL + "monitor/tmm"


"""tmm-admin 配置"""
ADMIN_IP = "10.100.12.82"
ADMIN_URL = "http://"+ADMIN_IP + ":8080"
ADMIN_USER = "admin"
ADMIN_PASSWORD = "admin"


"""邮件报告收件人"""
EMAIL_RECEIVER=[
'fengpengyong@tuandai.com'
]






"""===================================== 配置请添加到如下，下面打印出所有的配置供核对 ========================================="""

print("""
               =================================================================
                              本次测试用例使用如下配置，请核对........
      """)
for var in dir():
    if re.match("^[^__]", var) and re.match("^[^re$]", var):
        print(str(var) + ":" + str(eval(var)))
print("""
               =================================================================
      """)

#__confirm = input("请确认以上配置... y/n：");
#if __confirm.upper() != "Y":
#    exit(1)
