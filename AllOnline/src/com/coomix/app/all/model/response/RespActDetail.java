package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.CommunityActDetail;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespActDetail extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private CommunityActDetail data;

    public CommunityActDetail getData() {
        return data;
    }

    public void setData(CommunityActDetail data) {
        this.data = data;
    }
}
