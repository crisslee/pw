package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class DeviceSetting implements Serializable {

    private static final long serialVersionUID = 8486506584026719451L;

    //电子围栏
    private Fence efence;
    // 区域围栏
    private AreaFence area;
    //超速
    private Overspeed overspeed;

    public Fence getEfence() {
        if(efence != null) {
            efence.getParamSingleInfo();
        }
        return efence;
    }

    public void setEfence(Fence efence) {
        this.efence = efence;
    }

    public AreaFence getArea() {
        return area;
    }

    public void setArea(AreaFence area) {
        this.area = area;
    }

    public Overspeed getOverspeed() {
        return overspeed;
    }

    public void setOverspeed(Overspeed overspeed) {
        this.overspeed = overspeed;
    }
}
