package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.Alarm;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespAlarmDetailList extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Alarm> data;

    public ArrayList<Alarm> getData() {
        return data;
    }

    public void setData(ArrayList<Alarm> data) {
        this.data = data;
    }

}
