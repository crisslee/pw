package com.coomix.app.all.model.bean;

/**
 * 由于设备现在10秒内可能上报N个点，所以需要保存每个点的回放速度
 */
public class TrackPointWithSpeed extends TrackPoint {

    public TrackPointWithSpeed(TrackPoint point){
        if(point != null){
            this.gps_time = point.gps_time;
            this.lng = point.lng;
            this.lat = point.lat;
            this.course = point.course;
            this.speed = point.speed;
            this.startTime = point.startTime;
            this.endTime = point.endTime;
            this.stayTime = point.stayTime;
        }
    }

    public int historySpeed;//回放速度
}
