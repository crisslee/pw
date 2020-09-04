package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.AreaFence;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespAreaFence extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private AreaFence data;

    public AreaFence getData() {
        return data;
    }

    public void setData(AreaFence data) {
        this.data = data;
    }

}
