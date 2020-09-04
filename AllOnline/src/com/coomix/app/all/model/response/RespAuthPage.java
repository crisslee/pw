package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.AuthPages;

/**
 * Created by ly on 2018/1/8 10:04.
 */

public class RespAuthPage extends RespBase {
    private AuthPages data;

    public AuthPages getData() {
        return data;
    }

    public void setData(AuthPages data) {
        this.data = data;
    }
}
