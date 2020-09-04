package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/11/6.
 */
public class StatisticsKey implements Serializable {
    private static final long serialVersionUID = -5991054339924848826L;
    public int imei;
    public int datetime;
    public int milestat;
    public int outspeed;
    public int outspeedlimit;
    public int stop;

    @Override
    public String toString() {
        return "StatisticsKey{" +
            "imei=" + imei +
            ", datetime=" + datetime +
            ", milestat=" + milestat +
            ", outspeed=" + outspeed +
            ", outspeedlimit=" + outspeedlimit +
            ", stop=" + stop +
            '}';
    }
}
