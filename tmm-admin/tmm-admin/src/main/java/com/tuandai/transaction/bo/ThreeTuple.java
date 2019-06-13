package com.tuandai.transaction.bo;

public class ThreeTuple<A, B, C> extends TwoTuple<A, B> {

    public C c;

    public ThreeTuple(A a, B b, C c) {
        super(a, b);
        this.c = c;
    }

    public C getThree() {
        return c;
    }

    @Override
    public String toString() {
        return "ThreeTuple{" +
                "c=" + c +
                ", a=" + a +
                ", b=" + b +
                '}';
    }
}
