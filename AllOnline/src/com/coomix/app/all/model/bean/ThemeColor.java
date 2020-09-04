package com.coomix.app.all.model.bean;

import android.graphics.Color;
import android.text.TextUtils;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/9/26.
 */
public class ThemeColor implements Serializable {
    private static final long serialVersionUID = -2827580358175667675L;
    @SerializedName("theme_color_type")
    private int colorType;
    @SerializedName("color_whole")
    private String colorWhole;//主题颜色
    @SerializedName("color_untouchable_btn")
    private String colorUntouchableBtn;
    @SerializedName("color_navibar_text")
    private String colorNavibarText;
    @SerializedName("color_searchbar_bg")
    private String colorSearchBarBg;
    @SerializedName("icon_profile")
    private String profile;
    @SerializedName("icon_alarm")
    private String alarm;
    @SerializedName("icon_list")
    private String list;
    @SerializedName("icon_satellite")
    private String satellite;
    @SerializedName("icon_satellite_touched")
    private String satelliteTouched;
    @SerializedName("icon_street")
    private String street;
    @SerializedName("icon_account_info")
    private String accountInfo;
    @SerializedName("icon_sp_info")
    private String spInfo;
    @SerializedName("icon_activity")
    private String activity;
    @SerializedName("icon_charge")
    private String charge;
    @SerializedName("icon_card_charge")
    private String card_charge;
    @SerializedName("icon_un_lock")
    private String un_lock;
    @SerializedName("icon_barcode_adddev")
    private String barcode;
    @SerializedName("icon_setting")
    private String setting;
    @SerializedName("icon_magnify_glass")
    private String magnifyGlass;

    public int getColorType() {
        return colorType;
    }

    public int getColorWhole() {
        if (TextUtils.isEmpty(colorWhole)) {
            colorWhole = "#22262D";
        }
        try {
            return Color.parseColor(colorWhole);
        } catch (Exception e) {
            return Color.parseColor("#22262D");
        }
    }

    public int getColorUntouchableBtn() {
        if (TextUtils.isEmpty(colorUntouchableBtn)) {
            colorUntouchableBtn = "#697489";
        }
        try {
            return Color.parseColor(colorUntouchableBtn);
        } catch (Exception e) {
            return Color.parseColor("#697489");
        }
    }

    public int getColorNavibarText() {
        if (TextUtils.isEmpty(colorNavibarText)) {
            colorNavibarText = "#FFFFFF";
        }
        try {
            return Color.parseColor(colorNavibarText);
        } catch (Exception e) {
            return Color.parseColor("#FFFFFF");
        }
    }

    public int getColorSearchBarBg() {
        //搜索输入框的背景颜色
        if (TextUtils.isEmpty(colorSearchBarBg)) {
            colorSearchBarBg = "#FFFFFF";
        }
        try {
            return Color.parseColor(colorSearchBarBg);
        } catch (Exception e) {
            return Color.parseColor("#FFFFFF");
        }
    }

    public String getProfile() {
        return profile;
    }

    public String getAlarm() {
        return alarm;
    }

    public String getList() {
        return list;
    }

    public String getSatellite() {
        return satellite;
    }

    public String getSatelliteTouched() {
        return satelliteTouched;
    }

    public String getStreet() {
        return street;
    }

    public String getAccountInfo() {
        return accountInfo;
    }

    public String getSpInfo() {
        return spInfo;
    }

    public String getActivity() {
        return activity;
    }

    public String getCharge() {
        return charge;
    }

    public String getCard_charge() {
        return card_charge;
    }

    public String getUn_lock(){
        return un_lock;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getSetting() {
        return setting;
    }

    public String getMagnifyGlass() {
        return magnifyGlass;
    }

    @Override
    public String toString() {
        return "ThemeColor{" +
            "colorType=" + colorType +
            ", colorWhole='" + colorWhole + '\'' +
            ", colorUntouchableBtn='" + colorUntouchableBtn + '\'' +
            ", colorNavibarText='" + colorNavibarText + '\'' +
            ", colorSearchBarBg='" + colorSearchBarBg + '\'' +
            ", profile='" + profile + '\'' +
            ", alarm='" + alarm + '\'' +
            ", list='" + list + '\'' +
            ", satellite='" + satellite + '\'' +
            ", satelliteTouched='" + satelliteTouched + '\'' +
            ", street='" + street + '\'' +
            ", accountInfo='" + accountInfo + '\'' +
            ", spInfo='" + spInfo + '\'' +
            ", activity='" + activity + '\'' +
            ", charge='" + charge + '\'' +
            ", card_charge='" + card_charge + '\'' +
            ", barcode='" + barcode + '\'' +
            ", setting='" + setting + '\'' +
            ", magnifyGlass='" + magnifyGlass + '\'' +
            '}';
    }
}
