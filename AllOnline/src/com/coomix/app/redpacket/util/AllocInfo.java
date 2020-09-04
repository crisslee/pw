package com.coomix.app.redpacket.util;

import java.io.Serializable;

/**
 * Created by ssl on 2017/2/13.
 */
public class AllocInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MOST_LUCKY = 1;
    public static final int MOST_NOT_LUCKY = 3;

    private String uid;
    private String name;
    private String img;
    private int shares;
    private long recv_time; //
    private int flag; // 1=手气最佳
    private int extra_shares; //额外返回的金额
    private int recv_idx; //第几楼
    private int vtype;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public long getRecv_time() {
        return recv_time;
    }

    public void setRecv_time(long recv_time) {
        this.recv_time = recv_time;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getExtra_shares() {
        return extra_shares;
    }

    public void setExtra_shares(int extra_shares) {
        this.extra_shares = extra_shares;
    }

    public int getRecv_idx() {
        return recv_idx;
    }

    public void setRecv_idx(int recv_idx) {
        this.recv_idx = recv_idx;
    }

    public int getVtype() {
        return vtype;
    }

    public void setVtype(int vtype) {
        this.vtype = vtype;
    }
}
