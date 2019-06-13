package com.tuandai.transaction.service;

import com.tuandai.transaction.bo.Limiter;
import com.tuandai.transaction.bo.MessageAck;
import com.tuandai.transaction.bo.MessageState;
import com.tuandai.transaction.bo.Thresholds;
import com.tuandai.transaction.config.Constants;
import com.tuandai.transaction.dao.TransactionCheckDao;
import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.domain.filter.TransactionCheckFilter;
import com.tuandai.transaction.mq.MqSendHelper;
import com.tuandai.transaction.service.inf.TransactionCheckService;
import com.tuandai.transaction.utils.ServiceException;
import com.tuandai.transaction.utils.ThreadPoolExecutorUtils;
import com.tuandai.transaction.vo.CompletionMessageVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;

@Service
public class TransactionCheckServiceImpl implements TransactionCheckService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionCheckServiceImpl.class);


    @Autowired
    private TransactionCheckDao transactionCheckDao;

    @Autowired
    private CheckThreadCallService checkThreadCallService;

    @Autowired
    private MqSendHelper mqSendHelper;

    @Autowired
    private ThresholdsTimeManage thresholdsTimeManage;

    @Autowired
    private MonitorServiceImpl zabbixServiceImpl;

    private static Date PRESEND_CALLBACK_BYTASK_TIME = null;
    private static Date SEND_CALLBACK_BYTASK_TIME = null;

    @Override
    public void preSendCallbackByTask() {
        // 任务执行中不可重复调用
        if (isPreSendCallbackRuning()) {
            return;
        }

        // 读取未处理的数据
        TransactionCheckFilter filter = new TransactionCheckFilter();
        filter.setMessageState(MessageState.PRESEND.code());
        filter.setEndPresendBackNextSendTime(new Date());
        List<TransactionCheck> transactionCheckList = transactionCheckDao.
                queryTransactionCheckByFilter(filter, new Limiter(0, 10, "pid ASC"));

        if (CollectionUtils.isEmpty(transactionCheckList)) {
            logger.debug("PresendCallback list is null....");
            return;
        }

        ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getTaskThreadPoolExecutorUtils();

        List<Future<Boolean>> futureList = new ArrayList<>();
        for (TransactionCheck tTransactionCheck : transactionCheckList) {
            Future<Boolean> future = executor.submit(() -> {
                // 单个处理
                return checkThreadCallService.presendCallback(tTransactionCheck);
            });
            futureList.add(future);
        }

        for (Future<Boolean> future : futureList) {
            executor.execute(() -> {
                try {
                    // 设置超时
                    future.get(Constants.RESTFUL_MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.error("PresendCallback 任务异常中止 : {}", e.getMessage());
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    logger.error("PresendCallback 计算出现异常: {}", e.getMessage());
                } catch (TimeoutException e) {
                    logger.error("PresendCallback 超时异常: {}", e.getMessage());
                    // 超时后取消任务
                    future.cancel(true);
                }
            });
        }

    }

    @Override
    public void sendTask() throws ServiceException {
        // 任务执行中不可重复调用
        if (isSendCallbackRuning()) {
            return;
        }

        TransactionCheckFilter filter = new TransactionCheckFilter();
        filter.setMessageState(MessageState.SEND.code());
        filter.setEndMessageNextSendTime(new Date());
        List<TransactionCheck> transactionSendList = transactionCheckDao.
                queryTransactionCheckByFilter(filter, new Limiter(0, 1000, "pid ASC"));

        for (TransactionCheck transactionCheck : transactionSendList) {

            TransactionCheck newTransactionCheck = new TransactionCheck();
            newTransactionCheck.setPid(transactionCheck.getPid());
            newTransactionCheck.setUpdateTime(new Date());
            try {
                mqSendHelper.sendTranTopicMsg(transactionCheck);
                // 发送
                newTransactionCheck.setMessageState(MessageState.DONE.code());
            } catch (Exception e) {
                logger.error("发送check消息失败！！", e);

                // 发送的总次数
                Integer sendThreshold = transactionCheck.getMessageSendThreshold();

                if (sendThreshold <= 0) { // 重试次数达到最大
                    newTransactionCheck.setMessageState(MessageState.SEND_DIED.code()); // 死亡
                } else {
                    int currentSends = (newTransactionCheck.getMessageSendTimes() == null ? 0 : newTransactionCheck.getMessageSendTimes()) + 1;
                    newTransactionCheck.setMessageSendTimes(currentSends);
                    newTransactionCheck.setMessageNextSendTime(thresholdsTimeManage.createSendNextTime(currentSends));
                    newTransactionCheck.setMessageSendThreshold(sendThreshold - 1);
                    newTransactionCheck.setMessageState(MessageState.SEND.code());
                }
            }
            transactionCheckDao.update(newTransactionCheck);
        }
    }

    @Override
    public List<TransactionCheck> queryMessageByState(int state, Date startTime, Date endTime, String dlqName,
                                                      List<String> serviceNames) {
        TransactionCheckFilter filter = new TransactionCheckFilter();
        filter.setMessageState(state);
        filter.setStartUpdateTime(startTime);
        filter.setEndUpdateTime(endTime);
        filter.setDlqName(dlqName);
        filter.setServiceNames(serviceNames);
        filter.setIsMessage(0);
        return transactionCheckDao.queryTransactionCheckByFilter(filter,  new Limiter(0, 1000, "pid ASC"));
    }

    @Override
    public TransactionCheck queryMessageById(long pid, List<String> serviceNames) {
        return transactionCheckDao.queryTransactionCheckById(pid, serviceNames);
    }

    @Override
    public Map<MessageState, Long> queryCountMessageByState(List<String> serviceNames) {
        return zabbixServiceImpl.messageStateMonitor(serviceNames);
    }

    @Override
    public boolean resend(Long pid, List<String> serviceNames) {

        TransactionCheckFilter filter = new TransactionCheckFilter();
        filter.setPid(pid);
        filter.setServiceNames(serviceNames);
        List<TransactionCheck> list = transactionCheckDao.queryTransactionCheckByFilter(filter, new Limiter(0, 1000, "pid ASC"));

        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(100, "消息不存在");
        }
        TransactionCheck tmp = list.get(0);
        // 废弃消息不支持重发
        if (tmp.getMessageState() == MessageState.DISCARD.code()) {
            throw new ServiceException(101, "消息状态不合法");
        }

        TransactionCheck updateTransactionCheck = new TransactionCheck();
        updateTransactionCheck.setUpdateTime(new Date());
        updateTransactionCheck.setPid(pid);
        if (tmp.getMessageState() == MessageState.PER_DIED.code()) {
            updateTransactionCheck.setPresendBackThreshold(Thresholds.MAX_PRESEND.code());
            updateTransactionCheck.setMessageState(MessageState.PRESEND.code());
            updateTransactionCheck.setPresendBackNextSendTime(new Date());
        } else {
            updateTransactionCheck.setMessageSendThreshold(Thresholds.MAX_SEND.code());
            updateTransactionCheck.setMessageState(MessageState.SEND.code());
            updateTransactionCheck.setMessageNextSendTime(new Date());
        }
        transactionCheckDao.update(updateTransactionCheck);
        return true;
    }

    @Override
    public boolean discard(Long pid, List<String> serviceNames) {
        TransactionCheckFilter filter = new TransactionCheckFilter();
        filter.setPid(pid);
        filter.setServiceNames(serviceNames);
        List<TransactionCheck> list = transactionCheckDao.queryTransactionCheckByFilter(filter, new Limiter(0, 1000, "pid ASC"));

        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(100, "消息不存在");
        }

        TransactionCheck updateTransactionCheck = new TransactionCheck();
        updateTransactionCheck.setUpdateTime(new Date());
        updateTransactionCheck.setPid(pid);
        updateTransactionCheck.setMessageState(MessageState.DISCARD.code());
        transactionCheckDao.update(updateTransactionCheck);
        return true;
    }

    @Override
    public boolean delete(List<Long> pids, List<String> serviceNames) {
        if (CollectionUtils.isEmpty(pids)) {
            throw  new ServiceException(100, "非法参数,不能为空");
        }
        transactionCheckDao.delete(pids, serviceNames);
        return true;
    }

    @Override
    public boolean completionMessage(CompletionMessageVo completionMessageVo) throws IllegalAccessException {
        // 构造MessageAck
        List<Long> pids = completionMessageVo.getPids();
        List<TransactionCheck> transactionChecks = transactionCheckDao.queryTransactionCheckByIds(pids);
        boolean result = true;
        for (TransactionCheck transactionCheck : transactionChecks) {
            MessageAck messageAck = new MessageAck();
            messageAck.setUid(transactionCheck.getuId());
            messageAck.setTopic(completionMessageVo.getTopic());
            messageAck.setState(completionMessageVo.getState() == 1 ? MessageAck.SendState.CANCEL : MessageAck.SendState.COMMIT);
            messageAck.setServiceName(transactionCheck.getServiceName());
            messageAck.setMessage(completionMessageVo.getMessage());
            checkTransactionCheck(messageAck, transactionCheck.getPid());
            // 检查消息是否存在
            TransactionCheck checkTransaction = checkTransaction(transactionCheck.getPid(), messageAck);
           if (!completionTransactionCheck(checkTransaction, messageAck)) {
               result = false;
           }
        }
        return result;
    }

    private boolean completionTransactionCheck(TransactionCheck updateTransaction, MessageAck messageAck)
            throws IllegalAccessException {
        // 补全
        TransactionCheck updateTransactionCheck = new TransactionCheck();
        updateTransactionCheck.setPid(updateTransaction.getPid());
        updateTransactionCheck.setUpdateTime(new Date());
        updateTransactionCheck.setServiceName(messageAck.getServiceName());
        updateTransactionCheck.setMessageTopic(messageAck.getTopic());
        updateTransactionCheck.setMessage(messageAck.getMessage());
        updateTransactionCheck.setMessageState(MessageState.DONE.code());

        updateTransaction.setMessageTopic(messageAck.getTopic());
        updateTransaction.setMessage(messageAck.getMessage());
        boolean isSend = false;
        if (messageAck.getState() == MessageAck.SendState.COMMIT) {
            try {
                logger.info("completionTransactionCheck 准备发送mq:" + updateTransaction);
                mqSendHelper.sendTranTopicMsg(updateTransaction);
                logger.info("presendCallbackSuccess 发送成功" );
                isSend = true;
            } catch (Exception e) {
                logger.error("手动补发消息失败！");
                throw new IllegalArgumentException("补发消息失败，请检查所填参数是否正确.....");
            }
        } else {
            // 取消的消息也算完成
            isSend = true;
        }
        if (isSend) {
            // 更新数据库，数据状态 完成
            transactionCheckDao.update(updateTransactionCheck);
        }
        return isSend;
    }


    private TransactionCheck checkTransaction(Long pid, MessageAck messageAck) {
        String serviceName = messageAck.getServiceName();
        if (serviceName == null) {
            throw new IllegalArgumentException("completionMessage_serviceNames is null");
        }
        List<String> serviceNames = new ArrayList<>(1);
        serviceNames.add(serviceName);
        TransactionCheck check = transactionCheckDao.queryTransactionCheckById(pid, serviceNames);
        if (check == null) {
            throw new IllegalArgumentException("completionMessage_check is null");
        }
        // 检查状态是否是异常状态
        if (!check.getMessageState().equals(MessageState.PER_DIED.code())) {
            throw new IllegalArgumentException("completionMessage_MessageState is not MessageState.PER_DIED ...");
        }
        return check;
    }

    private void checkTransactionCheck(MessageAck messageAck, Long pid) {
        if (messageAck == null) {
            throw new IllegalArgumentException("completionMessage_messageAck is null");
        }
        if (pid == null) {
            throw new IllegalArgumentException("completionMessage_pid is null");
        }
        if (messageAck.getMessage() == null) {
            throw new IllegalArgumentException("completionMessage_transactionCheck_Message is null");
        }
        if (messageAck.getTopic() == null) {
            throw new IllegalArgumentException("completionMessage_transactionCheck_MessageTopic is null");
        }
        if (messageAck.getState() == null) {
            throw new IllegalArgumentException("completionMessage_transactionCheck_State is null");
        }
        if (messageAck.getUid() == null) {
            throw new IllegalArgumentException("completionMessage_transactionCheck_Uid is null");
        }
        if (messageAck.getServiceName() == null) {
            throw new IllegalArgumentException("completionMessage_transactionCheck_serviceName is null");
        }
    }


    private synchronized boolean isPreSendCallbackRuning() {
        if (null == PRESEND_CALLBACK_BYTASK_TIME) {
            PRESEND_CALLBACK_BYTASK_TIME = new Date();
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -1 * (7));
        if (calendar.getTime().before(PRESEND_CALLBACK_BYTASK_TIME)) {
            logger.debug("PresendCallback 正在执行中，任务不可重复调用 ......!");
            return true;
        }
        return false;
    }

    private synchronized boolean isSendCallbackRuning() {
        if (null == SEND_CALLBACK_BYTASK_TIME) {
            SEND_CALLBACK_BYTASK_TIME = new Date();
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -1 * (7));
        if (calendar.getTime().before(SEND_CALLBACK_BYTASK_TIME)) {
            logger.debug("SendCallback 正在执行中，任务不可重复调用 ......!");
            return true;
        }
        return false;
    }

}
