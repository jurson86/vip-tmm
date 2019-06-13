package com.tuandai.transaction.service;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.bo.MessageAck;
import com.tuandai.transaction.bo.MessageState;
import com.tuandai.transaction.config.Constants;
import com.tuandai.transaction.dao.TransactionCheckDao;
import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.mq.MqSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;

/**
 * check回调处理
 */

@Service
public class CheckThreadCallService {

    private static final Logger logger = LoggerFactory.getLogger(CheckThreadCallService.class);

    @Autowired
    private RestTemplate restTemplate;     // 负载均衡

    @Autowired
    private TransactionCheckDao transactionCheckDao;

    @Autowired
    private MqSendHelper mqSendHelper;

    @Autowired
    private ThresholdsTimeManage thresholdsTimeManage;

    public Boolean presendCallback(TransactionCheck tTransactionCheck) {

        HashMap<String, Object> httpbodyMap = new HashMap<String, Object>();
        httpbodyMap.put("uid", tTransactionCheck.getuId());
        httpbodyMap.put("messageTopic", tTransactionCheck.getMessageTopic());
        httpbodyMap.put("message", "{}");
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(Constants.HTTP_HEAD + tTransactionCheck.getServiceName() + tTransactionCheck.getPresendBackUrl(),
                    HttpMethod.POST, new HttpEntity<HashMap<String, Object>>(httpbodyMap, Constants.header), String.class);

            if (response.getStatusCode().equals(HttpStatus.OK)) {
                String body = response.getBody();
                logger.info("回调返回对象，response,body：" + body);
                if (!StringUtils.isEmpty(body)) {
                    MessageAck messageAck = JSONObject.parseObject(body, MessageAck.class);
                    logger.info("回调返回对象，messageAck：" + messageAck);
                    if (messageAck != null) {
                        // 回调成功
                        presendCallbackSuccess(tTransactionCheck, messageAck);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("check回调失败!" , e);
        }
        logger.debug("check回调失败, 调用presendCallbackFail()!");
        // 回调调用失败
        presendCallbackFail(tTransactionCheck);
        return false;
    }

    private void presendCallbackSuccess(TransactionCheck tTransactionCheck, MessageAck messageAck) {
        TransactionCheck updateTransactionCheck = new TransactionCheck();
        updateTransactionCheck.setPid(tTransactionCheck.getPid());
        updateTransactionCheck.setUpdateTime(new Date());
        updateTransactionCheck.setServiceName(messageAck.getServiceName());
        updateTransactionCheck.setMessageTopic(messageAck.getTopic());
        updateTransactionCheck.setMessage(messageAck.getMessage());
        updateTransactionCheck.setMessageState(MessageState.DONE.code());

        // 重置
        tTransactionCheck.setMessageTopic(messageAck.getTopic());
        tTransactionCheck.setMessage(messageAck.getMessage());
        boolean isSend = false;
        if (messageAck.getState() == MessageAck.SendState.COMMIT) { // 提交
            // 发送mq
            try {
                logger.info("presendCallbackSuccess 准备发送mq:" + tTransactionCheck);
                mqSendHelper.sendTranTopicMsg(tTransactionCheck);
                logger.info("presendCallbackSuccess 发送成功" );
                isSend = true;
            } catch (Exception e) {
                logger.error("发送check消息失败！！");

                Integer sendThreshold = tTransactionCheck.getMessageSendThreshold();

                if (sendThreshold <= 0) { // 重试次数达到最大
                    updateTransactionCheck.setMessageState(MessageState.SEND_DIED.code()); // 死亡
                } else {
                    int currentSends = tTransactionCheck.getMessageSendTimes() + 1;
                    updateTransactionCheck.setMessageSendTimes(currentSends);
                    updateTransactionCheck.setMessageNextSendTime(thresholdsTimeManage.createSendNextTime(currentSends));
                    updateTransactionCheck.setMessageSendThreshold(sendThreshold - 1);
                    updateTransactionCheck.setMessageState(MessageState.SEND.code());
                }
            }
        }
        if (isSend) {
            int currentPreSends = tTransactionCheck.getPresendBackSendTimes() + 1;
            updateTransactionCheck.setPresendBackSendTimes(currentPreSends);
            updateTransactionCheck.setPresendBackThreshold(tTransactionCheck.getPresendBackThreshold() - 1);
            updateTransactionCheck.setPresendBackNextSendTime(thresholdsTimeManage.createPreSendBackTime(currentPreSends));
        }
        // 更新数据库，数据状态 完成、或者发送状态
        transactionCheckDao.update(updateTransactionCheck);
    }

    private void presendCallbackFail(TransactionCheck tTransactionCheck) {
        TransactionCheck updateTransactionCheck = new TransactionCheck();
        updateTransactionCheck.setPid(tTransactionCheck.getPid());
        updateTransactionCheck.setUpdateTime(new Date());
        updateTransactionCheck.setMessageState(MessageState.PRESEND.code());
        Integer preSendThreshold = tTransactionCheck.getPresendBackThreshold();

        if (preSendThreshold <= 0) { // 重试次数到达最大次数
            updateTransactionCheck.setMessageState(MessageState.PER_DIED.code());
        } else {
            int preCurrentSendTimes = tTransactionCheck.getPresendBackSendTimes() + 1;
            updateTransactionCheck.setPresendBackSendTimes(preCurrentSendTimes);
            updateTransactionCheck.setPresendBackNextSendTime(thresholdsTimeManage.createPreSendBackTime(preCurrentSendTimes));
            updateTransactionCheck.setPresendBackThreshold(preSendThreshold - 1);
        }
        transactionCheckDao.update(updateTransactionCheck);
    }

}
