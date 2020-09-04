package com.coomix.app.all.model.response;

import com.coomix.app.all.log.AppConfigs;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespAppConfig extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private AppConfigs data;

    public AppConfigs getData() {
        return data;
    }

    public void setData(AppConfigs data) {
        this.data = data;
    }

}
