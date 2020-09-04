package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/3/12.
 */
public class ProductInfo implements Serializable {
    private static final long serialVersionUID = 3293621292921325110L;
    //总流量
    public float total_value;
    //使用流量
    public float cumulate_value;
    //剩余流量
    public float left_value;
}
