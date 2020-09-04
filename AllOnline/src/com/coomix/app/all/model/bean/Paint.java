package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by goome on 2017/7/11.
 */
public class Paint implements Serializable {
    private static final long serialVersionUID = 1L;

    private int cnt;
    private ArrayList<PeopleForAct> data = new ArrayList<PeopleForAct>();

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public ArrayList<PeopleForAct> getData() {
        return data;
    }

    public void setData(ArrayList<PeopleForAct> data) {
        this.data = data;
    }
}
