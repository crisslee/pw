package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class TrackPoint implements Serializable {

    private static final long serialVersionUID = -7610894460142550363L;
    public long gps_time;
    public double lng;
    public double lat;
    public int course;
    public int speed;
    public long startTime;
    public long endTime;
    public long stayTime;

    public boolean contains() {
        return false;
    }

    @Override
    public String toString() {
        return "TrackPoint{" +
            "gps_time=" + gps_time +
            ", lng=" + lng +
            ", lat=" + lat +
            ", course=" + course +
            ", speed=" + speed +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            ", stayTime=" + stayTime +
            '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TrackPoint)) {
            return false;
        }
        TrackPoint objTrackPoint = (TrackPoint) obj;
        if (this.gps_time == objTrackPoint.gps_time && this.lng == objTrackPoint.lng && this.lat == objTrackPoint.lat
            && this.course == objTrackPoint.course && this.speed == objTrackPoint.speed
            && this.startTime == objTrackPoint.startTime && this.endTime == objTrackPoint.endTime
            && this.stayTime == objTrackPoint.stayTime) {
            return true;
        }

        return super.equals(obj);
    }
}
