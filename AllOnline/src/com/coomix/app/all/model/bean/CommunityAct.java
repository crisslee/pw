package com.coomix.app.all.model.bean;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

/**
 * @author shishenglong
 * @since 2016年8月17日 上午10:58:39
 */
public class CommunityAct implements Serializable {

    private static final long serialVersionUID = 3555551212126888L;

    /**
     * 0:已结束
     */
    public static final int ACT_FINISHED = 0;
    /**
     * 1:已截止
     */
    public static final int ACT_STOPPED = 1;
    /**
     * 2:已满员
     */
    public static final int ACT_FULL = 2;
    /**
     * 3:可报名
     */
    public static final int ACT_SIGNING = 3;
    /**
     * 4:已取消
     */
    public static final int ACT_CANCELED = 4;
    /**
     * 5:可报名
     */
    public static final int ACT_WAITING = 5;

    @Expose
    private int id;
    @Expose
    private String title;
    @Expose
    private long begtime;
    @Expose
    private long endtime;
    @Expose
    private long deadline;
    @Expose
    private String cost;
    @Expose
    private String location;
    @Expose
    private String pic;// 宣传图
    @Expose
    private int applynum;
    @Expose
    private int maxnum;
    @Expose
    private int status;// 0:已结束,1:已截止,2:已满员,3:可报名
    @Expose
    private CommunityShare share;

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getBegtime() {
        return begtime;
    }

    public void setBegtime(long bentime) {
        this.begtime = bentime;
    }

    public long getEndtime() {
        return endtime;
    }

    public void setEndtime(long endtime) {
        this.endtime = endtime;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getApplynum() {
        return applynum;
    }

    public void setApplynum(int applynum) {
        this.applynum = applynum;
    }

    public int getMaxnum() {
        return maxnum;
    }

    public void setMaxnum(int maxnum) {
        this.maxnum = maxnum;
    }

    public CommunityShare getShare() {
        return share;
    }

    public void setShare(CommunityShare share) {
        this.share = share;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}
