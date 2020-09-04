package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/11/6.
 */
public class StatisticsSummary implements Serializable {
    private static final long serialVersionUID = 878076595361314733L;
    public int totalmilestat;
    public int totaloutspeed;
    public int totalstop;

    @Override
    public String toString() {
        return "StatisticsSummary{" +
            "totalmilestat=" + totalmilestat +
            ", totaloutspeed=" + totaloutspeed +
            ", totalstop=" + totalstop +
            '}';
    }
}
