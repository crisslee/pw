package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.Adver;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespAdver extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private String citycode;
    private ArrayList<Adver> data;

    public ArrayList<Adver> getData() {
        return data;
    }

    public void setData(ArrayList<Adver> data) {
        this.data = data;
    }

    public String getCitycode() {
        return citycode;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode;
    }
}
