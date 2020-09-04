package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.DevPassInfo;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019-11-28.
 */
public class RespDevPassInfo extends RespBase {
    private DevPassInfo data;

    public DevPassInfo getData() {
        return data;
    }

    public void setData(DevPassInfo data) {
        this.data = data;
    }
}
