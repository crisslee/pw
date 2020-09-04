package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.CommunityActs;
import com.google.gson.annotations.Expose;

/**
 * Created by ly on 2017/8/25.
 */

public class RespActList extends RespBase {
    public CommunityActs getData() {
        return data;
    }

    public void setData(CommunityActs data) {
        this.data = data;
    }

    @Expose
    private CommunityActs data;
}
