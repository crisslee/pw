package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.DeviceDetailInfo;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespDeviceDetailInfo extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private DeviceDetailInfo data;

    public DeviceDetailInfo getData() {
        return data;
    }

    public void setData(DeviceDetailInfo data) {
        this.data = data;
    }

}
