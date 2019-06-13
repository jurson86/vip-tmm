from urllib import request, parse
import json
import time
from config.config import MONITOR_URL, ADMIN_URL, ADMIN_USER, ADMIN_PASSWORD


# 登录Tmm admin 获取token
def get_login_tmm_token():
    login_data = parse.urlencode([('userName', ADMIN_USER), ('passWord', ADMIN_PASSWORD)])
    req = request.Request(ADMIN_URL + '/login')
    req.add_header('Accept', 'application/json, text/plain, */*')
    req.add_header('Connection', 'keep-alive')
    req.add_header('Origin', ADMIN_URL)

    with request.urlopen(req, data=login_data.encode(encoding='utf-8')) as f:
        if f.status == 200:
            data = json.loads(f.read().decode('utf-8'))
            return data['data']['token']
        else:
            raise Exception("登录tmm-admin失败，请检查tmm-admin的配置信息或者检查tmm-admin是否存活.....")


"""
    tmm-admin 工具类
"""
class Admin:

    _token = get_login_tmm_token()

    """
    @:summary 获取mq集群配置列表
    @:return <type list>
    """
    def get_mq_cluster_setting_list(self):
        req = request.Request(ADMIN_URL + '/mq/query/list')
        req.add_header('token', self._token)
        with request.urlopen(req) as f:
            if f.status == 200:
                js = json.loads(f.read().decode('utf-8'))
                return js["data"]
            else:
                raise Exception("请检查 获取mq集群配置列表[/mq/query/list] 是否正确......")

    """
        @:summary 删除mq集群配置列表
    """
    def remove_mq_cluster_by_key(self, pid):
        req = request.Request(ADMIN_URL + '/mq/delete?pids=%s' % (pid,))
        req.add_header('token', self._token)
        with request.urlopen(req, data="".encode(encoding='utf-8')) as f:
            if f.status != 200:
                raise Exception("删除mq集群配置失败.....")

    """
    @:summary 添加mq集群配置列表
    @:param 
    """
    def add_mq_cluster(self, mq_cluster):
        del mq_cluster["createTime"], mq_cluster["updateTime"]
        body_value = json.JSONEncoder().encode(mq_cluster)
        req = request.Request(ADMIN_URL + '/mq/add')
        req.add_header('token',  self._token)
        req.add_header('Content-Type', 'application/json;charset=UTF-8')
        req.add_header('Connection', 'keep-alive')
        req.add_header('Origin', ADMIN_URL)
        with request.urlopen(req, data=body_value.encode(encoding='utf-8')) as f:
            if f.status != 200:
                raise Exception("添加mq集群配置失败.....")

    """
       @:summary 获取监控列表
       @:return  <type:dict>
    """
    def get_monitor_list(self):
        req = request.Request(ADMIN_URL + '/api/monitor/agent?last=true')
        with request.urlopen(req) as f:
            if f.status == 200:
                js = json.loads(f.read().decode('utf-8'))
                jsInfo = js["data"]
                for data in jsInfo:
                   if data["serviceName"] == "transaction-producer" and data["url"] == MONITOR_URL:
                       return json.JSONDecoder().decode(data["monitor"])
            else:
                raise Exception("获取监控列表失败.....")

    """
        @:summary 获取消息列表
        @:return  <type:list>获取异常消息列表 state  21: 异常消息
    """
    def get_message_list_by_state(self, state):
        req = request.Request(ADMIN_URL + '/api/messageList/state?state=%d' % (state, ))
        req.add_header('Origin', ADMIN_URL)
        req.add_header('token', self._token)
        with request.urlopen(req) as f:
            if f.status == 200:
                resp = json.loads(f.read().decode('utf-8'))
                return resp["data"]
            else:
                raise Exception("获取消息列表失败.....")

    """
    废弃消息
    """
    def discard_msg(self, pid):
        req = request.Request(ADMIN_URL + '/api/message/discard?pids=%s' % (pid,))
        req.add_header('Origin', ADMIN_URL)
        req.add_header('token', self._token)
        with request.urlopen(req, data="".encode(encoding='utf-8')) as f:
            if f.status != 200:
                raise Exception("废弃消息失败.....")

    """
    手工check 
    state: COMMIT(0, "提交"), CANCEL(1, "回滚");
    """
    def check_msg(self, pid, message, state):
        completionMessageVo = {"pids":[pid], "message":message, "state":state, "topic":"{\"customExchange\":false,\"exchange\":\"py_admin_exchange\",\"exchangeType\":\"fanout\",\"ip\":\"default\",\"vHost\":\"TMM\"}"}
        body_value = json.JSONEncoder().encode(completionMessageVo)
        req = request.Request(ADMIN_URL + '/api/message/completion')
        req.add_header('token', self._token)
        req.add_header('Content-Type', 'application/json;charset=UTF-8')
        req.add_header('Connection', 'keep-alive')
        req.add_header('Origin', ADMIN_URL)
        with request.urlopen(req, data=body_value.encode(encoding='utf-8')) as f:
            if f.status != 200:
                raise Exception("手动check消息失败.....")

    """
    消息重发
    """
    def rerend_msg(self, pid):
        req = request.Request(ADMIN_URL + '/api/message/resend?pids=%s' % (pid,))
        req.add_header('Origin', ADMIN_URL)
        req.add_header('token', self._token)
        with request.urlopen(req, data="".encode(encoding='utf-8')) as f:
            if f.status != 200:
                raise Exception("废弃消息失败.....")

    """
    消息清理
    """
    def clean_msg(self, pid):
        req = request.Request(ADMIN_URL + '/api/message/delete?pids=%s' % (pid,))
        req.add_header('Origin', ADMIN_URL)
        req.add_header('token', self._token)
        with request.urlopen(req, data="".encode(encoding='utf-8')) as f:
            if f.status != 200:
                raise Exception("清理消息失败.....")


    """
    查询死信消息
    """
    def queyDeadQueue(self):
        req = request.Request(ADMIN_URL + '/api/message/dlq/list')
        req.add_header('Origin', ADMIN_URL)
        req.add_header('token', self._token)
        with request.urlopen(req) as f:
            if f.status == 200:
                resp = json.loads(f.read().decode('utf-8'))
                return resp["data"]
            else:
                raise Exception("查询死信队列消息失败.....")

    """
        死信消息重发
    """
    def resend_dead_msg(self, queue):
        req = request.Request(ADMIN_URL + '/api/message/dlq/resend?queue=%s' % (queue,))
        req.add_header('token', self._token)
        with request.urlopen(req, data="".encode(encoding='utf-8')) as f:
            if f.status != 200:
                raise Exception("死信消息重发失败.....")