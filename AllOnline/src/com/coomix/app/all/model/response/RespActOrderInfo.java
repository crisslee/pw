package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.ActOrderInfo;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespActOrderInfo extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private ActOrderInfo data;

    public ActOrderInfo getData() {
        return data;
    }

    public void setData(ActOrderInfo data) {
        this.data = data;
    }
}
