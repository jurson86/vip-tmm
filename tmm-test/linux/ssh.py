import paramiko
import os
from config.config import MQHOST, MQPORT, MQLINUXUSER
import time


class SSH:
    # 保存ssh连接
    __ssh = None

    # 当前会话
    __session = None

    def __init__(self, path, host=MQHOST, port=MQPORT, user=MQLINUXUSER):
        # path = os.path.dirname(os.path.realpath(__file__)) + "\key"
        key = paramiko.RSAKey.from_private_key_file(path)
        SSH.__ssh = paramiko.SSHClient()
        # paramiko.util.log_to_file('paramiko.log')
        SSH.__ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())  # 通过公共方式进行认证 (不需要在known_hosts 文件中存在)
        # ssh.load_system_host_keys() #如通过known_hosts 方式进行认证可以用这个,如果known_hosts 文件未定义还需要定义 known_hosts
        try:
            SSH.__ssh.connect(hostname=host, username=user, pkey=key, port=port)  # 这里要 pkey passwordkey 密钥文件
            SSH.__session = SSH.__ssh.invoke_shell()
            time.sleep(0.1)
            # 自动切换到root 角色
            SSH.__session.send('sudo su - root \n')
            # 丢弃切换用户的命令
            buff = ''
            while not buff.endswith('# '):
                resp = SSH.__session.recv(9999)
                buff += bytes.decode(resp)

        except Exception:
            print("连接MQ服务器失败，请检查连接信息以及服务器信息,[config.config.MQHOST,MQPORT,MQLINUXUSER].....")
            raise Exception

    # 采用交互式执行命令
    def exec_command_by_root(self, command):
        # 发送命令到linux服务器
        SSH.__session.send(command)
        SSH.__session.send('\n')

        # 接受linux返回的字符
        buff = ''
        while not buff.endswith('# '):
            resp = SSH.__session.recv(9999)
            buff += bytes.decode(resp)

        #print(buff)
        return buff

    def close(self):
        try:
            SSH.__session.close()
            SSH.__ssh.close()
        except Exception:
            print("关闭MQ服务器失败，请检查连接信息以及服务器信息,[config.config.MQHOST,MQPORT,MQLINUXUSER].....")
            raise Exception


"""自测试用例"""
if __name__ == '__main__':
    print(os.path.dirname(os.path.realpath(__file__)))
    sshs = SSH(path = os.path.dirname(os.path.realpath(__file__)) + "\key")
    print(sshs.exec_command_by_root("cd /data/opt/test/tmm"))
    print(sshs.exec_command_by_root("ls"))
    print(sshs.exec_command_by_root("cd /data/opt/test/tmm \n ls"))

    sshs.close()