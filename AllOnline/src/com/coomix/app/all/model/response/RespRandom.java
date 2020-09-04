package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.Random;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/8/7.
 */
public class RespRandom extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private Random data;

    public Random getData() {
        return data;
    }

    public void setData(Random data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RespRandom{" +
            "data=" + data +
            '}';
    }
}
