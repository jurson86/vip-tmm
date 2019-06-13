package com.tuandai.tran.domain.filter;

import java.util.List;

public class TransactionCheckFilter {

    private List<String> msgIds;

    private Integer acceptCount;

    private Integer maxAcceptCount;

    private Integer minAcceptCount;

    public Integer getMinAcceptCount() {
        return minAcceptCount;
    }

    public void setMinAcceptCount(Integer minAcceptCount) {
        this.minAcceptCount = minAcceptCount;
    }

    public Integer getMaxAcceptCount() {
        return maxAcceptCount;
    }

    public void setMaxAcceptCount(Integer maxAcceptCount) {
        this.maxAcceptCount = maxAcceptCount;
    }

    public List<String> getMsgIds() {
		return msgIds;
	}

	public void setMsgIds(List<String> msgIds) {
		this.msgIds = msgIds;
	}

	public Integer getAcceptCount() {
        return acceptCount;
    }

    public void setAcceptCount(Integer acceptCount) {
        this.acceptCount = acceptCount;
    }
}
