package com.coomix.app.all.model.event;

/**
 * Created by ly on 2017/5/27.
 */
public class SwitchUserEvent {
    private boolean succ;
    private int errCode;
    private String msg;

    public SwitchUserEvent(boolean succ, int errCode, String msg) {
        this.succ = succ;
        this.errCode = errCode;
        this.msg = msg;
    }

    public boolean isSucc() {
        return succ;
    }

    public void setSucc(boolean succ) {
        this.succ = succ;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
