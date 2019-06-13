package com.tuandai.transaction.controller;

import com.tuandai.transaction.bo.MessageAck;
import com.tuandai.transaction.bo.MessageState;
import com.tuandai.transaction.bo.MonitorAgentJson;
import com.tuandai.transaction.bo.QueueJson;
import com.tuandai.transaction.dao.DlqServerDao;
import com.tuandai.transaction.domain.DlqServer;
import com.tuandai.transaction.domain.RegistryAgent;
import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.service.inf.MonitorService;
import com.tuandai.transaction.service.inf.RabbitMqService;
import com.tuandai.transaction.service.inf.TransactionCheckService;
import com.tuandai.transaction.service.inf.UserInfoService;
import com.tuandai.transaction.utils.*;
import com.tuandai.transaction.vo.CompletionMessageVo;
import com.tuandai.transaction.vo.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private TransactionCheckService transactionCheckService;

    @Autowired
    private RabbitMqService rabbitMqService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private DlqServerDao dlqServerDao;

    @Autowired
    private UserInfoService userInfoService;

    public  List<String> getServiceNames(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new BaseException("token 不能为空！");
        }
        UserInfo userinfo = BaseController.currentUserInfo(token);

        if (userinfo == null) {
            throw new BaseException("不能查询到对应的用户信息！");
        }
        if(userinfo.getPid().equals(1)){ //防止admin 数据没有及时更新
            userinfo = userInfoService.findByUsername(userinfo.getUserName());
        }
        Set<String> pers = userinfo.getPermissionSet();
        if (CollectionUtils.isEmpty(pers)) {
            throw new BaseException("用户未分配相应的服务权限");
        }
        List<String> list = new ArrayList<>();
        list.addAll(pers);
        return list;
    }

    /**
     * 通过定时任务触发，不对外提供
     */
    @RequestMapping(value = "/preSendCallbackByTask", method = RequestMethod.GET)
    public void preSendCallbackByTask() {
        transactionCheckService.preSendCallbackByTask();
    }

    // 查询消息状态列表
    @RequestMapping(value = "/messageList/state", method = RequestMethod.GET)
    public Object queryMessageListByState(@RequestHeader(value = "token") String token,
                                          @RequestParam Integer state,
                                          @RequestParam(defaultValue = "0") long startTime,
                                          @RequestParam(defaultValue = "-1") long endTime,
                                          @RequestParam(required = false) String queueName) {

        logger.info("queryMessageListByState: {} ", state);
        Date stTime = startTime == 0 ? null : new Date(startTime);
        Date edTime = endTime == -1 ? null : new Date(endTime);
        List<String> serviceNames = getServiceNames(token);
        List<TransactionCheck> list = transactionCheckService.queryMessageByState(state, stTime, edTime,
                queueName, serviceNames);
        return new ResponseEntity<Result<List<TransactionCheck>>>(new Result<List<TransactionCheck>>(list),
                HttpStatus.OK);

    }

    // 查询消息详情
    @RequestMapping(value = "/message/detail/pid", method = RequestMethod.GET)
    public Object queryMessageById(@RequestHeader(value = "token") String token, @RequestParam Long pid) {
        logger.info("queryMessageById, pid:{} ", pid);
        List<String> serviceNames = getServiceNames(token);
        TransactionCheck transactionCheck = transactionCheckService.queryMessageById(pid, serviceNames);
        return new ResponseEntity<Result<TransactionCheck>>(new Result<TransactionCheck>(transactionCheck),
                HttpStatus.OK);

    }

    // 查询各个状态下的消息数量
    @RequestMapping(value = "/message/state/count", method = RequestMethod.GET)
    public Object queryCountByState() {
        logger.info("queryCountByState()");
        //List<String> serviceNames = getServiceNames(token);
        Map<MessageState, Long> map = transactionCheckService.queryCountMessageByState(null);
        return new ResponseEntity<Result<Map<MessageState, Long>>>(new Result<Map<MessageState, Long>>(map),
                HttpStatus.OK);

    }

    // 消息重发
    @RequestMapping(value = "/message/resend", method = RequestMethod.POST)
    public Object resend(@RequestHeader(value = "token") String token, @RequestParam String pids) {

        logger.info("resend: {} ", pids);
        List<String> serviceNames = getServiceNames(token);
        if (StringUtils.isEmpty(pids)) {
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        String res = "";
        String[] pidList = pids.split(",");
        for (String pid : pidList) {
            boolean result = false;
            try {
                result = transactionCheckService.resend(Long.valueOf(pid), serviceNames);
            } catch (Exception e) {
                logger.error("未知异常", e);
            }
            if (!result) {
                res = res + pid + ",";
            }
        }
        return new ResponseEntity<Result<String>>(new Result<String>(res), HttpStatus.OK);

    }

    // 消息废弃
    @RequestMapping(value = "/message/discard", method = RequestMethod.POST)
    public Object discard(@RequestHeader(value = "token") String token, @RequestParam String pids) {
        logger.info("discard: {} ", pids);
        List<String> serviceNames = getServiceNames(token);
        if (StringUtils.isEmpty(pids)) {
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        String res = "";
        String[] pidList = pids.split(",");
        for (String pid : pidList) {
            boolean result = false;
            try {
                result = transactionCheckService.discard(Long.valueOf(pid), serviceNames);
            } catch (Exception e) {
                logger.error("未知异常", e);
            }
            if (!result) {
                res = res + pid + ",";
            }
        }
        return new ResponseEntity<Result<String>>(new Result<String>(res), HttpStatus.OK);
    }

    // 消费者死信队列重发
    @RequestMapping(value = "/message/dlq/resend", method = RequestMethod.POST)
    public Object dlqResend(@RequestHeader(value = "token") String token, @RequestParam String queue) {
        logger.info("dlqResend: {} ", queue);
        List<String> serviceNames = getServiceNames(token);
        boolean result = rabbitMqService.dlqResend(queue, serviceNames);
        return new ResponseEntity<Result<Boolean>>(new Result<Boolean>(result), HttpStatus.OK);

    }

    // 消费者死信队列统计
    @RequestMapping(value = "/message/dlq/list", method = RequestMethod.GET)
    public Object dlqList(@RequestHeader(value = "token") String token) {
        logger.info("dlqList() ");
        List<String> serviceNames = getServiceNames(token);
        List<QueueJson> result = rabbitMqService.dlqList(serviceNames);
        return new ResponseEntity<Result<List<QueueJson>>>(new Result<List<QueueJson>>(result), HttpStatus.OK);
    }

    /**
     * 物理删除消息
     */
    @RequestMapping(value = "/message/delete", method = RequestMethod.POST)
    public Object deleteMessage(@RequestHeader(value = "token") String token, @RequestParam String pids) {
        logger.info("deleteMessage {}", pids);
        List<String> serviceNames = getServiceNames(token);
        String[] pidList = pids.split(",");
        boolean result = transactionCheckService.delete(CollectionUtils.arrayToList(pidList), serviceNames);
        return new ResponseEntity<Result<Boolean>>(new Result<Boolean>(result), HttpStatus.OK);

    }


    /**
     * 查询agent 服务器的监控信息
     */
    @RequestMapping(value = "/monitor/agent", method = RequestMethod.GET)
    public Object getMonitorAgent() {
        logger.info("getMonitorAgent()");
        List<MonitorAgentJson> list = monitorService.getAgentMonitor();
        return new ResponseEntity<Result<List<MonitorAgentJson>>>(new Result<List<MonitorAgentJson>>(list), HttpStatus.OK);

    }

    /**
     * 添加注册服务名
     */
    @RequestMapping(value = "/monitor/agent/add", method = RequestMethod.GET)
    public Object addMonitorAgent(@RequestParam String serviceName, @RequestParam(required = false) String prefixUrl) {

        logger.info("addMonitorAgent {} , {}", serviceName, prefixUrl);
        if (StringUtils.isEmpty(serviceName)) {
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        List<RegistryAgent> registryAgents = new ArrayList<>();
        RegistryAgent registryAgent = new RegistryAgent();
        registryAgent.setPrefixUrl(prefixUrl);
        registryAgent.setServiceName(serviceName);
        Date time = new Date(System.currentTimeMillis());
        registryAgent.setUpdateTime(time);
        registryAgent.setCreateTime(time);
        registryAgents.add(registryAgent);
        boolean result = monitorService.addRegistryAgent(registryAgents);
        return new ResponseEntity<Result<Boolean>>(new Result<Boolean>(result), HttpStatus.OK);

    }

    @RequestMapping(value = "/monitor/agent/delete", method = RequestMethod.GET)
    public Object deleteMonitorAgent(@RequestParam String serviceName) {

        logger.info("deleteMonitorAgent {}", serviceName);
        if (StringUtils.isEmpty(serviceName)) {
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        boolean result = monitorService.deleteRegistryAgent(serviceName);
        return new ResponseEntity<Result<Boolean>>(new Result<Boolean>(result), HttpStatus.OK);

    }

    // 添加死信和服务绑定关系
    @RequestMapping(value = "/add/dlq/service", method = RequestMethod.GET)
    public Object addServiceDlq(@RequestParam(required = true) String serviceName , @RequestParam(required = true) String dlqName) {
        logger.info("addServiceDlq {}, {}", serviceName, dlqName);
        if (org.apache.commons.lang3.StringUtils.isBlank(serviceName) || org.apache.commons.lang3.StringUtils.isBlank(dlqName)) {
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        boolean result = rabbitMqService.addDlqService(serviceName.trim(), dlqName.trim());
        if(!result){
            throw new BaseException("已经存在绑定关系");
        }
        return new ResponseEntity<Result<String>>(new Result<String>("新增成功"), HttpStatus.OK);
    }

    // 服务与死信队列的关系列表
    @RequestMapping(value = "/dlq/serviceQueList", method = RequestMethod.GET)
    public Object serviceQueList(@RequestParam String serviceName) {
        logger.info("serviceQueList {}, {}", serviceName);
        if (StringUtil.isBlank(serviceName)) {
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        List<DlqServer> list = dlqServerDao.queryDlqServerQueList(serviceName,null);
        return new ResponseEntity<Result<List<DlqServer>>>(new Result<List<DlqServer>>(list), HttpStatus.OK);
    }

    // 服务与死信队列的关系列表
    @RequestMapping(value = "/dlq/delServiceQue", method = RequestMethod.GET)
    public Object delServiceQue(@RequestParam(required = true) Long pid ) {
        logger.info("delServiceQue {}, {}", pid);

        if(pid==null){
            throw new ServiceException(BZStatusCode.INVALID_PARAMS_IS_NULL);
        }
        int result = dlqServerDao.deleteDlqServer(pid);
        return new ResponseEntity<Result<String>>(new Result<String>("删除成功"), HttpStatus.OK);
    }


//    {
//        "uid":"123213",
//            "message":"hello",
//            "serviceName":"serviceName",
//            "topic": "{\"customExchange\":true,\"exchange\":\"mychange\",\"exchangeType\":\"fanout\",\"vHost\":\"/tmm_vhost\"}",
//            "state": 1,
//            "pid": 1
//    }
    // 异常消息补全
    @RequestMapping(value = "/message/completion", method = RequestMethod.POST)
    public Response completionMessage(@RequestBody CompletionMessageVo completionMessageVo) {
        logger.info("completionMessage {}", completionMessageVo);
        boolean result = false;
        try {
            result = transactionCheckService.completionMessage(completionMessageVo);
        } catch (Exception e) {
            logger.error("completionMessage， e:" + e);
            return new Response("未知错误");
        }
        return Response.success(result ? "重发成功" : "重发失败，请检查参数重试");
    }

}