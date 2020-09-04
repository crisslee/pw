package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.Notice;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019-10-18.
 */
public class RespNotice extends RespBase {
    private Notice data;

    public Notice getData() {
        return data;
    }

    public void setData(Notice data) {
        this.data = data;
    }
}
