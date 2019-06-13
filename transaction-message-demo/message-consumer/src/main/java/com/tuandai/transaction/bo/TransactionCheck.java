package com.tuandai.transaction.bo;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "transaction_check")
public class TransactionCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "msg_id")
    private String msgId;

    @Column(name = "producer_port")
    private String producerPort;

    @Column(name = "accept_count")
    private Integer acceptCount;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    public TransactionCheck(String msgId, String producerPort) {
        Date time = new Date();
        this.msgId = msgId;
        this.producerPort = producerPort;
        this.acceptCount = 0;
        this.createTime = time;
        this.updateTime = time;
    }

    public TransactionCheck() {
    }


    public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getProducerPort() {
        return producerPort;
    }

    public void setProducerPort(String producerPort) {
        this.producerPort = producerPort;
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
        return "TransactionCheck{" +
                "msgId=" + msgId +
                ", producerPort='" + producerPort + '\'' +
                ", acceptCount=" + acceptCount +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
