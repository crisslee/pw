package com.coomix.app.all.gpns;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import com.coomix.app.all.Constant;
import com.coomix.app.all.model.bean.Alarm;
import com.coomix.app.all.ui.alarm.AMapAlarmLocationActivity;
import com.coomix.app.all.ui.alarm.AlarmListActivity;
import com.coomix.app.all.ui.alarm.AlarmLocationParentActivity;
import com.coomix.app.all.ui.alarm.BAlarmLocationActivity;
import com.coomix.app.all.ui.alarm.GAlarmLocationActivity;
import com.coomix.app.all.ui.alarm.TAlarmLocationActivity;
import com.coomix.app.all.manager.SettingDataManager;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @time 2014-9-24
 * @author goome {@code} 用于消息推送到app 点击的时候跳入的临时Activity 并且该Activity具有发布广播的功能
 *         然后到达app的广播接收器 当接收到点击广播的时候 可以进入相关的页面 进行显示
 * 
 */
public class FilterActivity extends Activity {
    LocalBroadcastManager manager;
    private final String TAG = "FilterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = LocalBroadcastManager.getInstance(this);
        String gpnsMsg = getIntent().getStringExtra(Constant.GPNS_MSG);
        // 如果推送的消息是————{"content":"您的设备产生2条报警消息!","extras":{}}
        String gpnsMsg_same = getIntent().getStringExtra("same_msg");

        Intent i = new Intent();
        // 进行解析json格式 发送到页面进行显示
        if (!TextUtils.isEmpty(gpnsMsg) && (TextUtils.isEmpty(gpnsMsg_same) || !gpnsMsg.equals(gpnsMsg_same))) {
            int iMapType = SettingDataManager.getInstance(this).getMapTypeInt();
            if (iMapType == Constant.MAP_TYPE_GOOGLE) {
                // 谷歌
                i.setClass(this, GAlarmLocationActivity.class);
            } else if (iMapType == Constant.MAP_TYPE_AMAP) {
                // 高德地图
                i.setClass(this, AMapAlarmLocationActivity.class);
            } else if (iMapType == Constant.MAP_TYPE_TENCENT) {
                // 腾讯地图
                i.setClass(this, TAlarmLocationActivity.class);
            } else {
                // 百度
                i.setClass(this, BAlarmLocationActivity.class);
            }
            try {
                JSONObject msgJson = new JSONObject(gpnsMsg);
                if (null != msgJson) {
                    JSONObject extrasJson = msgJson.getJSONObject("extras");
                    if (null != extrasJson) {
                        if (extrasJson.has("type")) {
                            int type = extrasJson.optInt("type");
                            if (type == 1) {
                                if (extrasJson.has("alarm")) {
                                    String content = extrasJson.optString("alarm");
                                    if (!TextUtils.isEmpty(content)) {
                                        String[] arrays = content.split(",");
                                        if (arrays.length >= 7) {
                                            Alarm alarm = new Alarm();
                                            alarm.setDev_name(arrays[0]);
                                            alarm.setAlarm_type(arrays[1]);
                                            alarm.setAlarm_time(Long.parseLong(arrays[2]));
                                            if (arrays.length >= 8) {
                                                alarm.setSpeed(Integer.parseInt(arrays[7]));
                                            } else {
                                                alarm.setSpeed(0);
                                            }

                                            if (arrays.length >= 10) {
                                                alarm.setImei(arrays[9]);
                                            } else {
                                                alarm.setImei("");
                                            }

                                            if (SettingDataManager.getInstance(this).getMapTypeInt() == Constant.MAP_TYPE_BAIDU) {
                                                alarm.setLat(Double.parseDouble(arrays[5]));
                                                alarm.setLng(Double.parseDouble(arrays[6]));
                                            } else {
                                                alarm.setLat(Double.parseDouble(arrays[3]));
                                                alarm.setLng(Double.parseDouble(arrays[4]));
                                            }

                                            i.putExtra(AlarmLocationParentActivity.ALARM_DATA, alarm);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            this.startActivity(i);
                                            finish();
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                finish();
                return;
            }
        }

        i.setClass(this, AlarmListActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        this.startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager = null;
        }
    }
}
