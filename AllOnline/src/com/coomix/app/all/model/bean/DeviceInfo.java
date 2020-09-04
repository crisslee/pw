package com.coomix.app.all.model.bean;

import android.text.TextUtils;
import java.io.Serializable;
import java.text.DecimalFormat;

public class DeviceInfo implements Serializable {

    public static final int STATE_RUNNING = 0;
    public static final int STATE_DISABLE = 1;
    public static final int STATE_EXPIRE = 2;
    public static final int STATE_OFFLINE = 3;
    public static final int STATE_STOP = 4;

    //定位精度
    public static final int LOC_UNKNOWN = -1;
    public static final int LOC_GPS = 0;
    public static final int LOC_WIFI = 1;
    public static final int LOC_BASE = 2;
    public static final int SPEED_STOP = 0;
    public static final int SPEED_NORMAL = 1;
    public static final int SPEED_OVER80 = 2;
    public static final int SPEED_OVER120 = 3;
    public static final String TYPE_LOCK = "35";
    private static final long serialVersionUID = 1L;
    private String imei;
    private String name;
    private double lng;
    private double lat;
    /***定位时间 GPS定位时间 UTC 秒数(如果设备过期，值为0)*/
    private long gps_time;
    /***Gps数据的系统时间 UTC秒数 (如果设备过期，值为0)*/
    private long sys_time;
    /***心跳时间 UTC秒数(如果设备过期，值为0)*/
    private long heart_time;
    /****当前服务器时间 UTC秒数(如果设备过期，值为0)*/
    private long server_time;
    /****航向(正北方向为0度，顺时针方向增大。最大值360度)(如果设备过期，值为0)*/
    private int course;
    /****速度 (单位:km/h)(如果设备过期，基站定位，值为-1)*/
    private int speed;
    /****0:正常数据 1:设备未上线 2:设备已过期 3:离线 4:设备过期*/
    private int device_info_new;
    /***离线或者是静止时长*/
    private int seconds;
    /******1:开，0：关闭，-1;不支持****/
    private int acc;
    /****ACC开启/熄火时长*/
    private long acc_seconds;
    private String power;
    private String location;
    /****设防状态*/
    private int defence;
    /****-1:非录音设备 0:关闭录音状态 1:开启录音状态*/
    private int voice_status;
    /*****录音设备对应IM群ID 当voice_status为-1时，该字段无效*/
    private long voice_gid;
    private int group_id;
    /****用于辨别属于哪个账户的id*/
    private int customer_id;
    private String customer_name;
    /****用于显示分组名称*/
    private String customer_showname;
    private String dev_type;
    private String address;
    private String status;
    //智能录音
    private int smart_record;
    //省电模式
    private int trickle_power;
    //开启录音时间
    private long record_time;
    //录音持续时长
    private int record_len;
    //电池剩余时间
    private String battery_life;
    //定位精准度
    private int pos_accuracy;
    /** icon type */
    private String icon_type;

    public static int getSpeedStatus(int speed) {
        if (speed <= 0) {
            return SPEED_STOP;
        } else if (speed <= 80) {
            return SPEED_NORMAL;
        } else if (speed <= 120) {
            return SPEED_OVER80;
        } else {
            return SPEED_OVER120;
        }
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public long getGps_time() {
        return gps_time;
    }

    public void setGps_time(long gps_time) {
        this.gps_time = gps_time;
    }

    public long getSys_time() {
        return sys_time;
    }

    public void setSys_time(long sys_time) {
        this.sys_time = sys_time;
    }

    public long getHeart_time() {
        return heart_time;
    }

    public void setHeart_time(long heart_time) {
        this.heart_time = heart_time;
    }

    public long getServer_time() {
        return server_time;
    }

    public void setServer_time(long server_time) {
        this.server_time = server_time;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDevice_info_new() {
        return device_info_new;
    }

    public void setDevice_info_new(int device_info_new) {
        this.device_info_new = device_info_new;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getAcc() {
        return acc;
    }

    public void setAcc(int acc) {
        this.acc = acc;
    }

    public long getAcc_seconds() {
        return acc_seconds;
    }

    public void setAcc_seconds(long acc_seconds) {
        this.acc_seconds = acc_seconds;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getDefence() {
        return defence;
    }

    public void setDefence(int defence) {
        this.defence = defence;
    }

    public int getVoice_status() {
        return voice_status;
    }

    public void setVoice_status(int voice_status) {
        this.voice_status = voice_status;
    }

    public long getVoice_gid() {
        return voice_gid;
    }

    public void setVoice_gid(long voice_gid) {
        this.voice_gid = voice_gid;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCustomer_showname() {
        return customer_showname;
    }

    public void setCustomer_showname(String customer_showname) {
        this.customer_showname = customer_showname;
    }

    public String getDev_type() {
        return dev_type;
    }

    public void setDev_type(String dev_type) {
        this.dev_type = dev_type;
    }

    public boolean isWSerial() {
        return !TextUtils.isEmpty(dev_type) && (dev_type.startsWith("w") || dev_type.startsWith("W"));
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getState() {
        long a = server_time - gps_time;

        if (device_info_new == 3) {// 离线
            return STATE_OFFLINE;
        }
        if (device_info_new == 1) {// 未上线
            return STATE_DISABLE;
        }
        if (device_info_new == 2) {// 过期
            return STATE_EXPIRE;
        }

        if (device_info_new == 0) {// 运动
            return STATE_RUNNING;
        }

        if (device_info_new == 4) {// 静止
            return STATE_STOP;
        }
        if (a <= 35) {
            return STATE_RUNNING;
        } else {
            return STATE_STOP;
        }
    }

    public boolean isOnline() {
        return device_info_new == STATE_RUNNING || device_info_new == STATE_STOP;
    }

    public int getSpeedStatus() {
        return getSpeedStatus(speed);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DeviceInfo && this.imei != null && this.imei.equals(((DeviceInfo) obj).getImei());
    }

    public void setStatus(String status) {
        this.status = status;
    }

    //3字节表示gsm信号强度
    public int getGsmLevel() {
        if (status == null || status.length() < 6) {
            return -1;
        }
        String angle = status.substring(4, 6);
        return Integer.parseInt(angle, 16);
    }

    //6字节表示外电电量
    public int getBatteryLevel() {
        if (status == null || status.length() < 12) {
            return -1;
        }
        String battery = status.substring(10, 12);
        return Integer.parseInt(battery, 16);
    }

    //7，8字节表示外电电压
    //第7字节表示整数，第8字节表示小数
    public double getVoltage() {
        if (status == null || status.length() < 16) {
            return -1;
        }
        String seven = status.substring(12, 14);
        String eight = status.substring(14, 16);
        int is = Integer.parseInt(seven, 16);
        int ie = Integer.parseInt(eight, 16);
        String v = is + "." + ie;
        DecimalFormat df = new DecimalFormat("#.0");
        return Double.valueOf(df.format(Double.valueOf(v)));
    }

    //10字节表示定位器角度
    public int getInstallAngle(){
        //status是16进制，第10个字节表示定位器的安装角度。一个16进制的数是4位，所以两个16进制的数是8位等于一个字节
        if (status == null || status.length() < 20) {
            return -1;
        }
        String angle = status.substring(18, 20);
        return Integer.parseInt(angle, 16);
    }

    //12字节表示GPS信号强度
    public int getGpsLevel() {
        if (status == null || status.length() < 24) {
            return -1;
        }
        String angle = status.substring(22, 24);
        return Integer.parseInt(angle, 16);
    }

    //13字节表示定位器角度是否在有效时间内
    public int getValid() {
        if (status == null || status.length() < 26) {
            return -1;
        }
        String angle = status.substring(24, 26);
        return Integer.parseInt(angle, 16);
    }

    public int getSmart_record() {
        return smart_record;
    }

    public void setSmart_record(int smart_record) {
        this.smart_record = smart_record;
    }

    public int getTrickle_power() {
        return trickle_power;
    }

    public void setTrickle_power(int trickle_power) {
        this.trickle_power = trickle_power;
    }

    public long getRecord_time() {
        return record_time;
    }

    public void setRecord_time(long record_time) {
        this.record_time = record_time;
    }

    public int getRecord_len() {
        return record_len;
    }

    public void setRecord_len(int record_len) {
        this.record_len = record_len;
    }

    public String getBattery_life() {
        return battery_life;
    }

    public void setBattery_life(String battery_life) {
        this.battery_life = battery_life;
    }

    public int getPos_accuracy() {
        return pos_accuracy;
    }

    public void setPos_accuracy(int pos_accuracy) {
        this.pos_accuracy = pos_accuracy;
    }

    public String getIcon_type() {
        return icon_type;
    }

    public void setIcon_type(String icon_type) {
        this.icon_type = icon_type;
    }

    public void update(DeviceInfo obj) {
        if (!this.equals(obj)) {
            return;
        }
        this.name = obj.name;
        this.dev_type = obj.dev_type;
        this.device_info_new = obj.device_info_new;
        this.seconds = obj.seconds;
        this.gps_time = obj.gps_time;
        this.sys_time = obj.sys_time;
        this.heart_time = obj.heart_time;
        this.server_time = obj.server_time;
        this.lng = obj.lng;
        this.lat = obj.lat;
        this.course = obj.course;
        this.speed = obj.speed;
        this.status = obj.status;
        this.power = obj.power;
        this.location = obj.location;
        this.acc = obj.acc;
        this.acc_seconds = obj.acc_seconds;
        this.voice_status = obj.voice_status;
        this.voice_gid = obj.voice_gid;
        this.smart_record = obj.smart_record;
        this.trickle_power = obj.trickle_power;
        this.record_time = obj.record_time;
        this.record_len = obj.record_len;
        this.pos_accuracy = obj.pos_accuracy;
        this.battery_life = obj.battery_life;
        this.group_id = obj.group_id;
    }
}
