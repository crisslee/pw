package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by ly on 2017/6/17.
 */
public class ActRegister2Bean implements Serializable {

    public static final String TAG = "ActRegister2Bean";
    private static final long serialVersionUID = 8661729292300728432L;

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    private HashMap<String, String> data = new HashMap<>();
}
