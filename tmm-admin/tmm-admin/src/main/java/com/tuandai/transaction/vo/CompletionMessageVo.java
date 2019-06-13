package com.tuandai.transaction.vo;

import java.io.Serializable;
import java.util.List;

public class CompletionMessageVo implements Serializable {

    private List<Long> pids;

    private String message;

    private String topic;

    // COMMIT(0, "提交"), CANCEL(1, "回滚");
    private Integer state;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public List<Long> getPids() {
        return pids;
    }

    public void setPids(List<Long> pids) {
        this.pids = pids;
    }
}
