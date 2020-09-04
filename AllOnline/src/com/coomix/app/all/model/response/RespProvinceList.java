package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.Province;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespProvinceList extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Province> data;

    public ArrayList<Province> getData() {
        return data;
    }

    public void setData(ArrayList<Province> data) {
        this.data = data;
    }

}
