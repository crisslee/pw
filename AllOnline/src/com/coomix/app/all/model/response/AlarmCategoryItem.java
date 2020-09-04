package com.coomix.app.all.model.response;

import java.io.Serializable;

/**
 * Created by ssl on 2018/8/17.
 */

public class AlarmCategoryItem implements Serializable{
    private static final long serialVersionUID = 1L;

    private int alarm_type_id;
    private String alarm_type;
    private int alarm_num;
    private long send_time;

    //以下参数是作为保存其他类型的（app只支持几种特定类型，其他的全部归为其他）
    private String alarm_type_alias;
    private int alarm_num_alias;
    private boolean isAlias;
    private String user_name;


    public int getAlarm_type_id() {
        return alarm_type_id;
    }

    public void setAlarm_type_id(int alarm_type_id) {
        this.alarm_type_id = alarm_type_id;
    }

    public String getAlarm_type() {
        return alarm_type;
    }

    public void setAlarm_type(String alarm_type) {
        this.alarm_type = alarm_type;
    }

    public int getAlarm_num() {
        return alarm_num;
    }

    public void setAlarm_num(int alarm_num) {
        this.alarm_num = alarm_num;
    }

    public long getSend_time() {
        return send_time;
    }

    public void setSend_time(long send_time) {
        this.send_time = send_time;
    }

    public String getAlarm_type_alias() {
        return alarm_type_alias;
    }

    public void setAlarm_type_alias(String alarm_type_alias) {
        this.alarm_type_alias = alarm_type_alias;
    }

    public int getAlarm_num_alias() {
        return alarm_num_alias;
    }

    public void setAlarm_num_alias(int alarm_num_alias) {
        this.alarm_num_alias = alarm_num_alias;
    }

    public boolean isAlias() {
        return isAlias;
    }

    public void setAlias(boolean alias) {
        isAlias = alias;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }
}
