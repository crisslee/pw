package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.Fence;
import com.google.gson.annotations.SerializedName;

/**
 * Created by ly on 2017/12/26 14:19.
 */

public class RespQueryFence extends RespBase {


    /**
     * data : {"id":12810205,"user_id":7430857,"shape_type":"1","alarm_type":"0","validate_flag":"1","create_time":"2017-12-26 17:00:42","phone_num":"","remark":"","shape_param":"39.914888,116.403881,3300"}
     */

    @SerializedName("data")
    private Fence data;

    public Fence getData() {
        if(data != null){
            data.getParamSingleInfo();
        }
        return data;
    }

    public void setData(Fence data) {
        this.data = data;
    }
}
