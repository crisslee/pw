package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by think <br/>
 * 我的活动列表
 *
 * @since 2017/1/15.
 */
public class MyActivities implements Serializable {
    private static final long serialVersionUID = 3465756347563L;

    private Readpos readpos;

    private ArrayList<MyActivity> activity;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Readpos getReadpos() {
        return readpos;
    }

    public void setReadpos(Readpos readpos) {
        this.readpos = readpos;
    }

    public ArrayList<MyActivity> getActivity() {
        return activity;
    }

    public void setActivity(ArrayList<MyActivity> activity) {
        this.activity = activity;
    }
}
