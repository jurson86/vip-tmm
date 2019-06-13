package com.tuandai.transaction.bo;

public class TwoTuple<A, B> {

    public A a;

    public B b;

    public TwoTuple() {
    }

    public TwoTuple(A a, B b){
        this.a = a;
        this.b = b;
    }

    public A getFirst() {
        return a;
    }

    public B getSecond() {
        return b;
    }

    @Override
    public String toString(){
        return "(" + a + ", " + b + ")";
    }

}
