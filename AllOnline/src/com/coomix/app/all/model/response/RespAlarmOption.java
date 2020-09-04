package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.PushSetting;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespAlarmOption extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private PushSetting data;

    public PushSetting getData() {
        return data;
    }

    public void setData(PushSetting data) {
        this.data = data;
    }
}
