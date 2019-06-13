package com.tuandai.tran.domain;

import java.util.Date;

public class TransactionCheck {

    private String msgId;

    private String msg;

    private Integer acceptCount;

    private Date createTime;

    private Date updateTime;

    public TransactionCheck() {
    }

    public TransactionCheck(String msgId, String msg) {
        Date time = new Date();
        this.msgId = msgId;
        this.msg = msg;
        this.acceptCount = 0;
        this.createTime = time;
        this.updateTime = time;
    }

    public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}


    public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Integer getAcceptCount() {
        return acceptCount;
    }

    public void setAcceptCount(Integer acceptCount) {
        this.acceptCount = acceptCount;
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

    @Override
    public String toString() {
        return "{" +
                "'messageId':'" + msgId +
                "','message':'" + msg +
                "'}";
    }
}
