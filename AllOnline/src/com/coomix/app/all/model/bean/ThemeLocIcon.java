package com.coomix.app.all.model.bean;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/9/26.
 */
public class ThemeLocIcon implements Serializable {
    private static final long serialVersionUID = -5634481223973886161L;
    @SerializedName("theme_locicon_type")
    int locType;
    @SerializedName("icon_moving")
    public String moving;
    @SerializedName("icon_moving_fast")
    public String fast;
    @SerializedName("icon_overspeed")
    public String overSpeed;
    @SerializedName("icon_still")
    public String still;
    @SerializedName("icon_offline")
    public String offline;
    @SerializedName("icon_expired")
    public String expired;

    @Override
    public String toString() {
        return "ThemeLocIcon{" +
            "locType=" + locType +
            ", moving='" + moving + '\'' +
            ", fast='" + fast + '\'' +
            ", overSpeed='" + overSpeed + '\'' +
            ", still='" + still + '\'' +
            ", offline='" + offline + '\'' +
            ", expired='" + expired + '\'' +
            '}';
    }
}
