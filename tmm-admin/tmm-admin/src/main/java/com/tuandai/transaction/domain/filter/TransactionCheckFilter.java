package com.tuandai.transaction.domain.filter;

import java.util.Date;
import java.util.List;

public class TransactionCheckFilter {

    // 消息状态
    private Integer messageState;

    // 最大预发送回调时间
    private Date endPresendBackNextSendTime;

    // 最大发送回调时间
    private Date endMessageNextSendTime;

    private Long pid;

    private List<String> serviceNames;

    private Date startUpdateTime;

    private Date endUpdateTime;

    private int isMessage = 1;

    private String dlqName;

    public String getDlqName() {
        return dlqName;
    }

    public void setDlqName(String dlqName) {
        this.dlqName = dlqName;
    }

    public int getIsMessage() {
        return isMessage;
    }

    public void setIsMessage(int isMessage) {
        this.isMessage = isMessage;
    }

    public Date getStartUpdateTime() {
        return startUpdateTime;
    }

    public void setStartUpdateTime(Date startUpdateTime) {
        this.startUpdateTime = startUpdateTime;
    }

    public Date getEndUpdateTime() {
        return endUpdateTime;
    }

    public void setEndUpdateTime(Date endUpdateTime) {
        this.endUpdateTime = endUpdateTime;
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }

    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
    }

    public Date getEndMessageNextSendTime() {
        return endMessageNextSendTime;
    }

    public void setEndMessageNextSendTime(Date endMessageNextSendTime) {
        this.endMessageNextSendTime = endMessageNextSendTime;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Date getEndPresendBackNextSendTime() {
        return endPresendBackNextSendTime;
    }

    public void setEndPresendBackNextSendTime(Date endPresendBackNextSendTime) {
        this.endPresendBackNextSendTime = endPresendBackNextSendTime;
    }

    public Integer getMessageState() {
        return messageState;
    }

    public void setMessageState(Integer messageState) {
        this.messageState = messageState;
    }
}
