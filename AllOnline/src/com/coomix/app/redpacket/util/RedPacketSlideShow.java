package com.coomix.app.redpacket.util;

import java.io.Serializable;

/**
 * Created by ssl
 *
 * @since 2017/3/31.
 */
public class RedPacketSlideShow implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;  // 123,
    private int type;  // 0=原生，1=H5
    private String name;
    private String picurl;
    private String jumpurl; //H5的是url，原生的是id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicurl() {
        return picurl;
    }

    public void setPicurl(String picurl) {
        this.picurl = picurl;
    }

    public String getJumpurl() {
        return jumpurl;
    }

    public void setJumpurl(String jumpurl) {
        this.jumpurl = jumpurl;
    }
}
