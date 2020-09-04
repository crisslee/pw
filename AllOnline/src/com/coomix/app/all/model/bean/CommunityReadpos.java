package com.coomix.app.all.model.bean;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

public class CommunityReadpos implements Serializable {

    private static final long serialVersionUID = 420766697827290512L;
    @Expose
    private double pointer;
    @Expose
    private String id;
    @Expose
    private double maxpointer;
    @Expose
    private boolean end; //

    //推荐帖子或很热门帖子标识：recommend（推荐帖），hot（热门帖）
    @Expose
    private String type;

    /** 帖子当前页 */
    @Expose
    private int pageno;

    /** 第一页的查询时间 */
    @Expose
    private long querytime;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPointer() {
        return pointer;
    }

    public void setPointer(double pointer) {
        this.pointer = pointer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMaxpointer(double maxpointer) {
        this.maxpointer = maxpointer;
    }

    public double getMaxpointer() {
        return maxpointer;
    }

    public void setPageno(int pageno) {
        this.pageno = pageno;
    }

    public int getPageno() {
        return pageno;
    }

    public void setQuerytime(int querytime) {
        this.querytime = querytime;
    }

    public long getQuerytime() {
        return querytime;
    }

    @Override
    public String toString() {
        return "Readpos [pointer=" + pointer + ", id=" + id + ", maxpointer=" + maxpointer + "]";
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    /**
     * 目前仅评论和赞列表使用
     */
    public boolean getEnd() {
        return end;
    }
}
