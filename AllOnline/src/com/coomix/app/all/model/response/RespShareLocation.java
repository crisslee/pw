package com.coomix.app.all.model.response;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespShareLocation extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
