package com.tuandai.transaction.bo;

public class Limiter {

    private Integer offset; // 开始行数

    private Integer size;   // 条数

    private String sortStr; // 排序规则，如“Id DESC”

    public Limiter() {
    }

    public Limiter(Integer offset, Integer size, String sortStr) {
        this.offset = offset;
        this.size = size;
        this.sortStr = sortStr;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortStr() {
        return sortStr;
    }

    public void setSortStr(String sortStr) {
        this.sortStr = sortStr;
    }
}
