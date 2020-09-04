package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class PushSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    private int start_time;
    private int end_time;
    private boolean sound;
    private int soundcode;
    private boolean shake;
    private boolean push;
    private boolean lower_alarm;

    private ArrayList<PushSetting.AlarmType> alarm_type;

    public static class AlarmType implements Serializable{
        public int id;
        public String name;
        public boolean push;
    }

    public int getStart_time() {
        return start_time;
    }

    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }

    public int getEnd_time() {
        return end_time;
    }

    public void setEnd_time(int end_time) {
        this.end_time = end_time;
    }

    public boolean isSound() {
        return sound;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

    public boolean isShake() {
        return shake;
    }

    public void setShake(boolean shake) {
        this.shake = shake;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public ArrayList<AlarmType> getAlarm_type() {
        if(alarm_type == null){
            alarm_type = new ArrayList<AlarmType>();
        }
        return alarm_type;
    }

    public void setAlarm_type(ArrayList<AlarmType> alarm_type) {
        this.alarm_type = alarm_type;
    }

    public int getSoundcode() {
        return soundcode;
    }

    public void setSoundcode(int soundcode) {
        this.soundcode = soundcode;
    }

    public boolean isLower_alarm() {
        return lower_alarm;
    }

    public void setLower_alarm(boolean lower_alarm) {
        this.lower_alarm = lower_alarm;
    }
}
