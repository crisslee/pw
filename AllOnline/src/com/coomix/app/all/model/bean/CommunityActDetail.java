package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author shishenglong
 * @since 2016年8月17日 上午10:58:39
 */
public class CommunityActDetail implements Serializable {

    private static final long serialVersionUID = 35566551216889L;

    private int id;
    private String title;
    private long begtime;
    private long endtime;
    private long deadline;
    private long opentime;//活动开始报名时间
    private String cost;
    private String location;
    private String pic;// 宣传图
    private int applynum;
    private int maxnum;
    private int status;// 0:已结束, 1:已截止,2:已满员,3:报名中,4:, 5:预约活动
    private int signed;// 0:未报名,1:已报名
    private CommunityActContents content;
    private CommunityShare share;
    private ArrayList<ActCommitItem> commit_items; //报名时候需要提交的信息，根据这些信息绘制显示ui
    private ArrayList<ActDisplay> display;
    private ActPrice price;  // 价格的具体信息
    private ActPay pay; // 支付相关信息，只有当前用户已报名该活动才会返回
    private String begin_date_desc; //起始时间描述
    private String end_date_desc;//结束时间描述
    private int preapply;//是否预约
    private int preapplynum; //预约人数

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

    public long getOpentime() {
        return opentime;
    }

    public void setOpentime(long opentime) {
        this.opentime = opentime;
    }

    public String getLocation() {
        return location == null ? "" : location;
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

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public CommunityActContents getActDetailContent() {
        return content;
    }

    public void setActDetailContent(CommunityActContents content) {
        this.content = content;
    }

    public int getSigned() {
        return signed;
    }

    public void setSigned(int signed) {
        this.signed = signed;
    }

    public CommunityShare getShare() {
        return share;
    }

    public void setShare(CommunityShare share) {
        this.share = share;
    }

    public ArrayList<ActCommitItem> getCommit_items() {
        return commit_items;
    }

    public void setCommit_items(ArrayList<ActCommitItem> commit_items) {
        this.commit_items = commit_items;
    }

    public ArrayList<ActDisplay> getDisplay() {
        return display;
    }

    public void setDisplay(ArrayList<ActDisplay> display) {
        this.display = display;
    }

    public ActPay getPay() {
        return pay;
    }

    public void setPay(ActPay pay) {
        this.pay = pay;
    }

    public ActPrice getPrice() {
        return price;
    }

    public void setPrice(ActPrice price) {
        this.price = price;
    }

    public String getBegin_date_desc() {
        return begin_date_desc;
    }

    public void setBegin_date_desc(String begin_date_desc) {
        this.begin_date_desc = begin_date_desc;
    }

    public String getEnd_date_desc() {
        return end_date_desc;
    }

    public void setEnd_date_desc(String end_date_desc) {
        this.end_date_desc = end_date_desc;
    }

    public int getPreapply() {
        return preapply;
    }

    public void setPreapply(int preapply) {
        this.preapply = preapply;
    }

    public int getPreapplynum() {
        return preapplynum;
    }

    public void setPreapplynum(int preapplynum) {
        this.preapplynum = preapplynum;
    }

    @Override
    public String toString() {
        return "CommunityActDetail{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", begtime=" + begtime +
            ", endtime=" + endtime +
            ", deadline=" + deadline +
            ", cost='" + cost + '\'' +
            ", location='" + location + '\'' +
            ", pic='" + pic + '\'' +
            ", applynum=" + applynum +
            ", maxnum=" + maxnum +
            ", status=" + status +
            ", signed=" + signed +
            ", content=" + content +
            ", share=" + share +
            ", preapply=" + preapply +
            ", preapplynum=" + preapplynum +
            '}';
    }
}
