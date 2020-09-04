package com.coomix.app.redpacket.util;

import java.io.Serializable;

/**
 * Created by ssl
 *
 * @since 2017/3/31.
 */
public class WinnerInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int uid;  // 123,
    private String name;
    private String img;
    private int lottery_id;
    private String prize_name;
    private String prize_pic;
    private long create_time;
    //奖品描述
    private String prize_desc;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLottery_id() {
        return lottery_id;
    }

    public void setLottery_id(int lottery_id) {
        this.lottery_id = lottery_id;
    }

    public String getPrize_name() {
        return prize_name;
    }

    public void setPrize_name(String prize_name) {
        this.prize_name = prize_name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getPrize_pic() {
        return prize_pic;
    }

    public void setPrize_pic(String prize_pic) {
        this.prize_pic = prize_pic;
    }

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public String getPrize_desc() {
        return prize_desc;
    }

    public void setPrize_desc(String prize_desc) {
        this.prize_desc = prize_desc;
    }
}
