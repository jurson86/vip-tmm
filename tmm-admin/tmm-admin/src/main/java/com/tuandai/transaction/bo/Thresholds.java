package com.tuandai.transaction.bo;


/**
 * 阀值定义
 * @author DELL
 *
 */
public enum Thresholds {

    MAX_SEND(10), MAX_PRESEND(6), MAX_RESULTBACK(3),OVER(0);

    private int nCode ;

    private Thresholds( int _nCode) {
        this.nCode = _nCode;
    }

    public int code() {
        return this.nCode;
    }

    @Override
    public String toString() {
        return String.valueOf ( this.nCode );
    }
}
