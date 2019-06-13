package com.tuandai.transaction.service.inf;

import com.tuandai.transaction.bo.MessageAck;
import com.tuandai.transaction.bo.MessageState;
import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.utils.ServiceException;
import com.tuandai.transaction.vo.CompletionMessageVo;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface TransactionCheckService {

    /**
     * 触发check操作
     */
    void preSendCallbackByTask()  throws ServiceException;

    /**
     * 触发Send操作
     */
    void sendTask() throws ServiceException;

    List<TransactionCheck> queryMessageByState(int state, Date startTime, Date endTime, String dlqName, List<String> serviceNames);

    TransactionCheck queryMessageById(long pid, List<String> serviceNames);

    Map<MessageState, Long> queryCountMessageByState(List<String> serviceNames);

    boolean resend(Long pid, List<String> serviceNames);

    boolean discard(Long pid, List<String> serviceNames);

    boolean delete(List<Long> pids, List<String> serviceNames);

    // 异常消息手工补全
    boolean completionMessage(CompletionMessageVo completionMessageVo) throws IllegalAccessException;

}
