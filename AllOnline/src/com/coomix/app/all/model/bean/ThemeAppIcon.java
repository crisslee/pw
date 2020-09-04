package com.coomix.app.all.model.bean;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/9/26.
 */
public class ThemeAppIcon implements Serializable {
    private static final long serialVersionUID = -259728987015570940L;
    public String url;
    @SerializedName("color_type")
    public int colorType;

    @Override
    public String toString() {
        return "ThemeAppIcon{" +
            "url='" + url + '\'' +
            ", colorType=" + colorType +
            '}';
    }
}
