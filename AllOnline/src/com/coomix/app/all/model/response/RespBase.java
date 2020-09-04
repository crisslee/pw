package com.coomix.app.all.model.response;

import com.google.gson.annotations.Expose;

/**
 * Created by ly on 2017/5/19.
 */
public class RespBase {
    private static final long serialVersionUID = 1L;

    @Expose
    private boolean success;
    @Expose
    private int errcode;
    @Expose
    private String msg;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
