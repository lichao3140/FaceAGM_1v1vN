package com.runvision.bean;

public class WebDataResultJson<T> {
    private int code;
    private String msg;
    private T data;

    public WebDataResultJson(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public WebDataResultJson(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
