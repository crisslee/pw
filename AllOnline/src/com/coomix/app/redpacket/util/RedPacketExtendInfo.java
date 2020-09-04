package com.coomix.app.redpacket.util;

import com.coomix.app.all.service.AllOnlineApiClient;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Created by ssl
 *
 * @since 2017/2/13.
 */
public class RedPacketExtendInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    //私聊红包，对象id
    private String toid = "0";
    //群聊红包，群id
    private String groupid = "0";
    //社区红包，板块id
    @SerializedName("sid")
    private String sectionId;
    private String citycode;
    private String posmaptype = AllOnlineApiClient.MAP_TYPE_GOOGLE;
    private String lat;
    private String lng;
    private String loc_name;

    public String getToid() {
        return toid;
    }

    public void setToid(String toid) {
        this.toid = toid;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getCitycode() {
        return citycode;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLoc_name() {
        return loc_name;
    }

    public void setLoc_name(String loc_name) {
        this.loc_name = loc_name;
    }
}
