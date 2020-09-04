package com.coomix.app.all.model.bean;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/9/26.
 */
public class ThemeAll implements Serializable {
    private static final long serialVersionUID = -3322890878242075000L;
    @SerializedName("theme_color")
    private ThemeColor themeColor;
    @SerializedName("theme_loc_icon")
    private ThemeLocIcon locIcon;
    @SerializedName("theme_logo")
    private ThemeLogo logo;
    @SerializedName("theme_app_icon")
    private ThemeAppIcon appIcon;

    public ThemeColor getThemeColor() {
        if(themeColor == null){
            themeColor = new ThemeColor();
        }
        return themeColor;
    }

    public ThemeLocIcon getLocIcon() {
        if(locIcon == null){
            locIcon = new ThemeLocIcon();
        }
        return locIcon;
    }

    public ThemeLogo getLogo() {
        if(logo == null){
            logo = new ThemeLogo();
        }
        return logo;
    }

    public ThemeAppIcon getAppIcon() {
        if(appIcon == null){
            appIcon = new ThemeAppIcon();
        }
        return appIcon;
    }

    @Override
    public String toString() {
        return "ThemeAll{" +
            "themeColor=" + themeColor +
            ", locIcon=" + locIcon +
            ", logo=" + logo +
            ", appIcon=" + appIcon +
            '}';
    }
}
