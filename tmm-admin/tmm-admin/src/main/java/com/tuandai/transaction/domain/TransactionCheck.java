package com.tuandai.transaction.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuandai.transaction.bo.MqType;

import java.util.Date;

public class TransactionCheck {

    private String uId;

    private Long pid;

    private String serviceName;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String messageTopic;

    private Integer messageState;

    private Integer messageSendThreshold;

    private Integer messageSendTimes;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date messageNextSendTime;

    private String presendBackUrl;

    private String presendBackMethod;

    private Integer presendBackThreshold;

    private Integer presendBackSendTimes;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date presendBackNextSendTime;

    private String message;

    private String dlqName;

    private String clusterIp;

    private Integer mqType = MqType.RABBIT.value();

    public String getDlqName() {
        return dlqName;
    }

    public void setDlqName(String dlqName) {
        this.dlqName = dlqName;
    }

    public String getClusterIp() {
        return clusterIp;
    }

    public void setClusterIp(String clusterIp) {
        this.clusterIp = clusterIp;
    }

    public Integer getMqType() {
        return mqType;
    }

    public void setMqType(Integer mqType) {
        this.mqType = mqType;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getMessageTopic() {
        return messageTopic;
    }

    public void setMessageTopic(String messageTopic) {
        this.messageTopic = messageTopic;
    }

    public Integer getMessageState() {
        return messageState;
    }

    public void setMessageState(Integer messageState) {
        this.messageState = messageState;
    }

    public Integer getMessageSendThreshold() {
        return messageSendThreshold;
    }

    public void setMessageSendThreshold(Integer messageSendThreshold) {
        this.messageSendThreshold = messageSendThreshold;
    }

    public Integer getMessageSendTimes() {
        return messageSendTimes;
    }

    public void setMessageSendTimes(Integer messageSendTimes) {
        this.messageSendTimes = messageSendTimes;
    }

    public Date getMessageNextSendTime() {
        return messageNextSendTime;
    }

    public void setMessageNextSendTime(Date messageNextSendTime) {
        this.messageNextSendTime = messageNextSendTime;
    }

    public String getPresendBackUrl() {
        return presendBackUrl;
    }

    public void setPresendBackUrl(String presendBackUrl) {
        this.presendBackUrl = presendBackUrl;
    }

    public String getPresendBackMethod() {
        return presendBackMethod;
    }

    public void setPresendBackMethod(String presendBackMethod) {
        this.presendBackMethod = presendBackMethod;
    }

    public Integer getPresendBackThreshold() {
        return presendBackThreshold;
    }

    public void setPresendBackThreshold(Integer presendBackThreshold) {
        this.presendBackThreshold = presendBackThreshold;
    }

    public Integer getPresendBackSendTimes() {
        return presendBackSendTimes;
    }

    public void setPresendBackSendTimes(Integer presendBackSendTimes) {
        this.presendBackSendTimes = presendBackSendTimes;
    }

    public Date getPresendBackNextSendTime() {
        return presendBackNextSendTime;
    }

    public void setPresendBackNextSendTime(Date presendBackNextSendTime) {
        this.presendBackNextSendTime = presendBackNextSendTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "TransactionCheck{" +
                "uId='" + uId + '\'' +
                ", pid=" + pid +
                ", serviceName='" + serviceName + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", messageTopic='" + messageTopic + '\'' +
                ", messageState=" + messageState +
                ", messageSendThreshold=" + messageSendThreshold +
                ", messageSendTimes=" + messageSendTimes +
                ", messageNextSendTime=" + messageNextSendTime +
                ", presendBackUrl='" + presendBackUrl + '\'' +
                ", presendBackMethod='" + presendBackMethod + '\'' +
                ", presendBackThreshold=" + presendBackThreshold +
                ", presendBackSendTimes=" + presendBackSendTimes +
                ", presendBackNextSendTime=" + presendBackNextSendTime +
                ", message='" + message + '\'' +
                ", dlqName='" + dlqName + '\'' +
                ", clusterIp='" + clusterIp + '\'' +
                ", mqType='" + mqType + '\'' +
                '}';
    }
}
