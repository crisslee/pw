package com.coomix.app.all.model.response;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespPreapplyNum extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private Preapplynum data;

    public Preapplynum getData() {
        return data;
    }

    public void setData(Preapplynum data) {
        this.data = data;
    }

    public class Preapplynum{
        int num;
    }

}
