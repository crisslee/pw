package com.coomix.app.all.model.response;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespPlateInfo extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private PlateInfo data;

    public PlateInfo getData() {
        return data;
    }

    public void setData(PlateInfo data) {
        this.data = data;
    }

    public class PlateInfo implements Serializable {
        //识别的车牌号信息
        private String plate;

        public String getPlate() {
            return plate;
        }

        public void setPlate(String plate) {
            this.plate = plate;
        }
    }
}
