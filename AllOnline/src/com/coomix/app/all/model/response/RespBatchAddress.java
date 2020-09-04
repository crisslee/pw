package com.coomix.app.all.model.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespBatchAddress extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<BatchAddress> data;

    public ArrayList<BatchAddress> getData() {
        return data;
    }

    public void setData(ArrayList<BatchAddress> data) {
        this.data = data;
    }

    public class BatchAddress implements Serializable
    {
        private String imei;
        private double lat;
        private double lng;
        private String address;

        public String getImei() {
            return imei;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        public String getAddress() {
            return address;
        }
    }
}
