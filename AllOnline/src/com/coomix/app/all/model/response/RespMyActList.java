package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.MyActivities;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespMyActList extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private MyActivities data;

    public MyActivities getData() {
        return data;
    }

    public void setData(MyActivities data) {
        this.data = data;
    }
}
