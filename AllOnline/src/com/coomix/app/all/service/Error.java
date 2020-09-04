package com.coomix.app.all.service;

import java.io.Serializable;

public class Error implements Serializable {
    private static final long serialVersionUID = 1630073513355731311L;

    /**
     * 网络返回固定错误--无错误
     */
    public static final int ERRCODE_NONE = 0;
    /**
     * 网络返回固定错误--红包创建金参数错误，和后端限制不一致
     */
    public static final int ERRCODE_REDPACKET_PARAM_INVALID = 10001;
    /**
     * 网络返回固定错误--提现时余额不足
     */
    public static final int ERRCODE_BALANCE_INSUFFICIENT = 10002;

    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Error{" +
            "code=" + code +
            ", msg=" + msg +
            '}';
    }
}