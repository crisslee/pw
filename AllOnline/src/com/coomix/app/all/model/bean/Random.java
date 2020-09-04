package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/8/7.
 */
public class Random implements Serializable {
    private static final long serialVersionUID = 1L;

    public long random;

    @Override
    public String toString() {
        return "Random{" +
            "random=" + random +
            '}';
    }
}
