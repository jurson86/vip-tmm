package com.tuandai.transaction.utils;


import java.io.Serializable;


public class Response<T> implements Serializable {

    /**
     * 返回码
     */
    private Integer code;

    /**
     * 返回描述
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    private static int SUCCESS = 200;

    private static int ERROR  =1;


    public static Response success(){
        return new Response(SUCCESS,"success");
    }

    public static <T> Response success(T t){
        return new Response(SUCCESS, "success", t);
    }

    public static Response error(String error){
        return new Response(1, error);
    }

    public static Response error(Integer code, String error){
        return new Response(code, error);
    }

    public Response() {
    }

    public Response(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Response(T data) {
        this.code = SUCCESS;
        this.msg = "success";
        this.data = data;
    }

    public Response(String msg) {
        this.code = ERROR;
        this.msg = msg;
    }

    public Response(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Response{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
