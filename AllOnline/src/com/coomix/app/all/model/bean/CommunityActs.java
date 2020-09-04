package com.coomix.app.all.model.bean;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author shishenglong
 * @since 2016年8月17日 上午10:54:17
 */
public class CommunityActs implements Serializable {

    private static final long serialVersionUID = 3441212126888L;
    @Expose
    private CommunityReadpos readpos;
    @Expose
    private ArrayList<CommunityAct> activity;
    @Expose
    private CommunityShare share;

    public CommunityReadpos getReadpos() {
        return readpos;
    }

    public void setReadpos(CommunityReadpos readpos) {
        this.readpos = readpos;
    }

    public ArrayList<CommunityAct> getActivityList() {
        if (activity == null) {
            activity = new ArrayList<CommunityAct>();
        }
        return activity;
    }

    public void setActivityList(ArrayList<CommunityAct> activity) {
        this.activity = activity;
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
