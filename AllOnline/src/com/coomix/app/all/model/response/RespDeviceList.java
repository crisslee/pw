package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.DeviceInfo;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespDeviceList extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    //data为空的时候，返回默认地址作为首页选择点
    private DefaultPos defaultpos;

    private ArrayList<DeviceInfo> data;

    public ArrayList<DeviceInfo> getData() {
        return data;
    }

    public void setData(ArrayList<DeviceInfo> data) {
        this.data = data;
    }

    public DefaultPos getDefaultpos() {
        return defaultpos;
    }

    public void setDefaultpos(DefaultPos defaultpos) {
        this.defaultpos = defaultpos;
    }

    public class DefaultPos {
        public double lat;
        public double lng;
    }
}
