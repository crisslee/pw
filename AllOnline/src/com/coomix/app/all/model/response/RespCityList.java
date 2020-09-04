package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.City;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespCityList extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<City> data;

    public ArrayList<City> getData() {
        return data;
    }

    public void setData(ArrayList<City> data) {
        this.data = data;
    }

}
