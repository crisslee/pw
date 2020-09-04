package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.DeviceSetting;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespDeviceSetting extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private DeviceSetting data;

    public DeviceSetting getData() {
        return data;
    }

}
