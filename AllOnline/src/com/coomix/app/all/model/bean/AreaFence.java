package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class AreaFence implements Serializable {

    private static final long serialVersionUID = -5777882719260133841L;

    public String id;
    public int validate_flag;
    private ShapeInfo shape_param;

    public ShapeInfo getShape_param() {
        if (shape_param == null) {
            shape_param = new ShapeInfo();
        }
        return shape_param;
    }

    public void setShape_param(ShapeInfo shape_param) {
        this.shape_param = shape_param;
    }

    public class ShapeInfo implements Serializable {
        public String province;
        public String city;
        public String district;
    }
}
