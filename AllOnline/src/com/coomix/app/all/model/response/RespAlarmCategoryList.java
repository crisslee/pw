package com.coomix.app.all.model.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespAlarmCategoryList extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<AlarmCategoryItem> data;

    public ArrayList<AlarmCategoryItem> getData() {
        return data;
    }

    public void setData(ArrayList<AlarmCategoryItem> data) {
        this.data = data;
    }

}
