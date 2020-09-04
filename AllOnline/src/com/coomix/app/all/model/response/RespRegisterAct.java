package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.ActOrderInfo;
import com.google.gson.annotations.Expose;

/**
 * Created by ly on 2017/6/21.
 */

public class RespRegisterAct extends RespBase {
    @Expose
    private ActOrderInfo data;

    public ActOrderInfo getData() {
        return data;
    }

    public void setData(ActOrderInfo data) {
        this.data = data;
    }
}
