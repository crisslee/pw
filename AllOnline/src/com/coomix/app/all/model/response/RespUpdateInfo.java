package com.coomix.app.all.model.response;

import com.coomix.app.all.ui.update.GoomeUpdateInfo;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespUpdateInfo extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private GoomeUpdateInfo data;

    public GoomeUpdateInfo getData() {
        return data;
    }

    public void setData(GoomeUpdateInfo data) {
        this.data = data;
    }
}
