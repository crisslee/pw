package com.coomix.app.all.model.bean;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by think on 2017/2/21.
 */
public class TrackPoints implements Serializable {

    private static final long serialVersionUID = -7254921996165117718L;
    @SerializedName("pos")
    public ArrayList<TrackPoint> trackPoints;
    private long resEndTime;

    public void setTrackPoints(ArrayList<TrackPoint> trackPoints) {
        this.trackPoints = trackPoints;
    }

    public ArrayList<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    public void setResEndTime(long resEndTime) {
        this.resEndTime = resEndTime;
    }

    public long getResEndTime() {
        return resEndTime;
    }
}
