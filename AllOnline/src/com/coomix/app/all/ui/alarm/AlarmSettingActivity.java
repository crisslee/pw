package com.coomix.app.all.ui.alarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.model.bean.PushSetting;
import com.coomix.app.all.model.response.RespAlarmOption;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.GpnsUtil;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.framework.util.TimeUtil;
import com.umeng.analytics.MobclickAgent;
import io.reactivex.disposables.Disposable;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.simonvt.numberpicker.NumberPicker;

public class AlarmSettingActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {

    private TextView chooseAudioTv, noticePeriodTv, noticeTypeTv,beginTimeTv,endTimeTv;
    private View chooseAudioIv, noticePeriodIv;
    private RelativeLayout chooseAudioLayout, noticeBeginTimeLayout,noticeEndTimeLayout, noticeTypeLayout;
    private RelativeLayout receiveLayout, soundLayout, vibrationLayout, longVibrationLayout, noticePeriodLayout;
    private ToggleButton receiveCbx, soundCbx, vibrationCbx, longVibrationCbx, noticePeriodCbox;
    private View dl;
    private PushSetting pushSetting;
    private ProgressDialogEx dialog;

    private List<String> notificationList;
    private int indexNotification;

    private List<String> ringList;
    private Cursor cursor;
    private RingtoneManager rm;
    private Ringtone ring;
    private int indexRing;
    private int choosedWhich;

    private List<String> soundList;
    private List<String> soundNameList;
    private String nameSound;
    private int indexSound;
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private int choosedNotificationIndex;
    private int choosedRingIndex;
    private int choosedCoutomIndex;
    private int notificationType;

    private boolean isFirstNotification = true;
    private boolean isFirstRing = true;
    private boolean isFirstCoutom = true;

    private static final int DEFAULT_AM_8 = 8 * 60;
    private static final int DEFAULT_PM_10 = 22 * 60;

    private final int REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        rm = new RingtoneManager(this);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ev_function", "报警信息设置");
        MobclickAgent.onEvent(AlarmSettingActivity.this, "ev_function", map);

        setContentView(R.layout.activity_alarm_setting);
        PreferenceUtil.init(this);
        initViews();

        getAlarmOption();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        pushSetting = null;
    }

    protected void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.alarm_setting, 0, 0);
        actionbar.setLeftImageClickListener(view -> {
            //去掉右键，在返回或者回退保存类型
            setAlarmTypes();
        });

        dl = findViewById(R.id.dl);

        receiveCbx = (ToggleButton) findViewById(R.id.receivedCbx);
        receiveCbx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (pushSetting != null) {
                pushSetting.setPush(isChecked);
                if (isChecked) {
                    dl.setVisibility(View.VISIBLE);
                } else {
                    dl.setVisibility(View.GONE);
                }
            }
        });

        soundCbx = (ToggleButton) findViewById(R.id.audioCbx);
        soundCbx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (pushSetting != null) {
                pushSetting.setSound(isChecked);
                if (isChecked) {
                    //chooseAudioTv.setVisibility(View.VISIBLE);
                    chooseAudioIv.setVisibility(View.VISIBLE);
                    chooseAudioLayout.setVisibility(View.VISIBLE);
                } else {
                    //chooseAudioTv.setVisibility(View.GONE);
                    chooseAudioIv.setVisibility(View.GONE);
                    chooseAudioLayout.setVisibility(View.GONE);
                }
            }
        });

        vibrationCbx = (ToggleButton) findViewById(R.id.vibrationCbx);
        vibrationCbx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (pushSetting != null) {
                pushSetting.setShake(isChecked);
            }
        });

        longVibrationCbx= (ToggleButton) findViewById(R.id.toggleLongShock);
        longVibrationCbx.setChecked(PreferenceUtil.getBoolean(Constant.ALARM_LONG_SHAKE, false));
        longVibrationCbx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            longVibrationCbx.setChecked(isChecked);
            PreferenceUtil.commitBoolean(Constant.ALARM_LONG_SHAKE, isChecked);
        });

        receiveLayout = (RelativeLayout) findViewById(R.id.received_layout);
        receiveLayout.setOnClickListener(this);

        soundLayout = (RelativeLayout) findViewById(R.id.audio_layout);
        soundLayout.setOnClickListener(this);

        vibrationLayout = (RelativeLayout) findViewById(R.id.vibration_layout);
        vibrationLayout.setOnClickListener(this);
        longVibrationLayout = (RelativeLayout)findViewById(R.id.layoutLongShock);
        longVibrationLayout.setOnClickListener(this);

        noticePeriodCbox = (ToggleButton) findViewById(R.id.notice_period_cbox);
        noticePeriodCbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (pushSetting != null) {
                if (!isChecked) {
                    pushSetting.setStart_time(DEFAULT_AM_8);
                    pushSetting.setEnd_time(DEFAULT_PM_10);
                    noticeBeginTimeLayout.setVisibility(View.VISIBLE);
                    noticeEndTimeLayout.setVisibility(View.VISIBLE);
                    noticePeriodIv.setVisibility(View.VISIBLE);
                    beginTimeTv.setText(TimeUtil.TimeLong2String(pushSetting.getStart_time()));
                    endTimeTv.setText(TimeUtil.TimeLong2String(pushSetting.getEnd_time()));
                } else {
                    pushSetting.setStart_time(0);
                    pushSetting.setEnd_time(1439);
                    noticeBeginTimeLayout.setVisibility(View.GONE);
                    noticeEndTimeLayout.setVisibility(View.GONE);
                    noticePeriodIv.setVisibility(View.GONE);
                }
            }
        });

        chooseAudioTv = (TextView) findViewById(R.id.chooseAudioTv);
        chooseAudioIv = (View) findViewById(R.id.chooseAudioIv);
        chooseAudioLayout = (RelativeLayout) findViewById(R.id.choose_audio_layout);
        chooseAudioLayout.setOnClickListener(this);

        noticeTypeTv = (TextView) findViewById(R.id.noticeTypeTv);
        noticeTypeLayout = (RelativeLayout) findViewById(R.id.notice_type_layout);
        noticeTypeLayout.setOnClickListener(this);

        //全天提醒
        noticePeriodTv = (TextView) findViewById(R.id.noticePeriodTv);
        noticePeriodIv = (View) findViewById(R.id.noticePeriodIv); //divider
        noticePeriodLayout = (RelativeLayout) findViewById(R.id.notice_period_layout);
        noticePeriodLayout.setOnClickListener(this);

        noticeBeginTimeLayout = (RelativeLayout) findViewById(R.id.notice_begin_time_layout);
        noticeBeginTimeLayout.setOnClickListener(this);
        noticeEndTimeLayout = (RelativeLayout) findViewById(R.id.notice_end_time_layout);
        noticeEndTimeLayout.setOnClickListener(this);
        beginTimeTv = (TextView) findViewById(R.id.begin_time);
        endTimeTv = (TextView) findViewById(R.id.end_time);
    }

    public boolean[] getBooleanArray(String key, int length) {
        String value = PreferenceUtil.getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            boolean[] booleans = new boolean[value.length()];
            for (int i = 0, size = value.length(); i < size; i++) {
                booleans[i] = value.charAt(i) == '1';
            }
            return booleans;
        } else {
            boolean[] fail = new boolean[length];
            for (int i = 0; i < length; i++) {
                fail[i] = true;
            }
            return fail;
        }
    }

    public void commitBooleanArray(String key, boolean[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, size = values.length; i < size; i++) {
            sb.append(values[i] == true ? 1 : 0);
        }
        PreferenceUtil.commitString(key, sb.toString());
    }

    private void setAlarmTypes() {
        if (pushSetting == null) {
            finish();
            return;
        }
        setAlarmOption();
    }

    @Override
    public void onClick(View v) {
        if (v == chooseAudioLayout) {
            showChooseAudioDialog();
        } else if (v == noticeTypeLayout) {
            // 接收报警消息类型
            if (pushSetting != null) {
                Intent intent = new Intent(this, AlarmTypeListActivity.class);
                intent.putExtra(AlarmTypeListActivity.ALARM_OPTION, pushSetting);
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (v == noticePeriodLayout) {
            if (pushSetting != null) {
                if (noticePeriodCbox.isChecked()) {
                    noticePeriodCbox.setChecked(false);
                    pushSetting.setStart_time(DEFAULT_AM_8);
                    pushSetting.setEnd_time(DEFAULT_PM_10);
                    noticeBeginTimeLayout.setVisibility(View.VISIBLE);
                    noticeEndTimeLayout.setVisibility(View.VISIBLE);
                    noticePeriodIv.setVisibility(View.VISIBLE);
                } else {
                    noticePeriodCbox.setChecked(true);
                    pushSetting.setStart_time(0);
                    pushSetting.setEnd_time(1439);
                    noticeBeginTimeLayout.setVisibility(View.GONE);
                    noticeEndTimeLayout.setVisibility(View.GONE);
                    noticePeriodIv.setVisibility(View.GONE);
                }
            }
        } else if (v == receiveLayout) {
            if (pushSetting != null) {
                if (receiveCbx.isChecked()) {
                    receiveCbx.setChecked(false);
                    dl.setVisibility(View.GONE);
                } else {
                    receiveCbx.setChecked(true);
                    dl.setVisibility(View.VISIBLE);
                }
                pushSetting.setPush(receiveCbx.isChecked());
            }
        } else if (v == soundLayout) {
            if (pushSetting != null) {
                if (soundCbx.isChecked()) {
                    soundCbx.setChecked(false);
                    chooseAudioIv.setVisibility(View.GONE);
                    chooseAudioLayout.setVisibility(View.GONE);
                } else {
                    soundCbx.setChecked(true);
                    chooseAudioIv.setVisibility(View.VISIBLE);
                    chooseAudioLayout.setVisibility(View.VISIBLE);
                }
                pushSetting.setSound(soundCbx.isChecked());
            }
        } else if (v == vibrationLayout) {
            if (pushSetting != null) {
                vibrationCbx.setChecked(!vibrationCbx.isChecked());
                pushSetting.setShake(vibrationCbx.isChecked());
            }
        }else if (v == longVibrationLayout){
            longVibrationCbx.setChecked(!longVibrationCbx.isChecked());
            PreferenceUtil.commitBoolean(Constant.ALARM_LONG_SHAKE, longVibrationCbx.isChecked());
        } else if(v == noticeBeginTimeLayout) {
            timePicker(true,false,beginTimeTv,8,0);
        }else if(v == noticeEndTimeLayout) {
            timePicker(false,true,endTimeTv,22,0);
        }
    }

    private void initNumberPicker(NumberPicker picker, int value, int min, int max, String label) {
        picker.setLabel(label);
        picker.setMaxValue(max);
        picker.setMinValue(min);
        picker.setValue(value);
        picker.setEditTextFocusable(false);
        picker.setEditTextEnabled(false);
        picker.setFocusable(true);
        picker.setFocusableInTouchMode(true);
        picker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d%s", i, label);
            }
        });
    }

    /**
     * 时间选择
     */
    private void timePicker(boolean start, boolean end, final TextView tv, int curHour, int curMin) {
        String time = tv.getText().toString().trim();
        final Dialog dialog = new Dialog(AlarmSettingActivity.this, R.style.time_picker_dialog);
        dialog.show();

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); //设置宽度
        dialog.getWindow().setAttributes(lp);

        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.dialogstyle); // 添加动画
        window.setContentView(R.layout.dialog_settime_picker);

        TextView title = (TextView) window.findViewById(R.id.settime_title);
        if (start) {
            title.setText(getString(R.string.set_start_time));
        } else {
            title.setText(getString(R.string.set_end_time));
        }

        final NumberPicker hourNumberPicker = (NumberPicker) window.findViewById(R.id.np_hour);
        final NumberPicker minuteNumberPicker = (NumberPicker) window.findViewById(R.id.np_minute);

        initNumberPicker(hourNumberPicker, curHour, 0, 23, "");
        initNumberPicker(minuteNumberPicker, curMin, 0, 59, "");

        // 为确认按钮添加事件,执行退出应用操作
        window.findViewById(R.id.dialog_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = hourNumberPicker.getValue();
                int min = minuteNumberPicker.getValue();
                String time = String.format("%02d:%02d", hour, min);
                tv.setText(time);
                if (pushSetting != null) {
                    if (start) {
                        pushSetting.setStart_time(hour * 60 + min);
                    } else {
                        pushSetting.setEnd_time(hour * 60 + min);
                    }
                }
                dialog.dismiss();
            }
        });
        // 关闭alert对话框架
        window.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showChooseAudioDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View chooseAudioView = factory.inflate(R.layout.dialog_choose_audio, null);
        final RadioButton chooseNotification = (RadioButton) chooseAudioView.findViewById(R.id.chooseNotification);
        final RadioButton chooseRing = (RadioButton) chooseAudioView.findViewById(R.id.chooseRing);
        final RadioButton chooseCustom = (RadioButton) chooseAudioView.findViewById(R.id.chooseCustom);
        final RadioGroup chooseNotificationGroup = (RadioGroup) chooseAudioView
                .findViewById(R.id.chooseNotificationGroup);
        final RadioGroup chooseRingGroup = (RadioGroup) chooseAudioView.findViewById(R.id.chooseRingGroup);
        final RadioGroup chooseCustomGroup = (RadioGroup) chooseAudioView.findViewById(R.id.chooseCustomGroup);
        final TextView chooseCustomTip = (TextView) chooseAudioView.findViewById(R.id.chooseCustomTip);

        chooseNotificationGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Log.d("gpns", "choose notifacation：" + "checkedId=" + checkedId);
            choosedNotificationIndex = checkedId;
            if (rm != null) {
                rm.stopPreviousRingtone();
            }
            if (ring != null) {
                ring.stop();
            }
            /* 判断位置不为0则播放的条目为position-1 */
            if (choosedNotificationIndex != 0) {
                try {
                    rm = new RingtoneManager(AlarmSettingActivity.this);
                    rm.setType(RingtoneManager.TYPE_NOTIFICATION);
                    rm.setStopPreviousRingtone(true);
                    rm.getCursor();
                    rm.getRingtone(choosedNotificationIndex - 1).play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            /* position为0是跟随系统，先得到系统所使用的铃声，然后播放 */
            if (choosedNotificationIndex == 0) {
                Uri uri = RingtoneManager.getActualDefaultRingtoneUri(AlarmSettingActivity.this,
                        RingtoneManager.TYPE_NOTIFICATION);
                if (uri != null) {
                    ring = RingtoneManager.getRingtone(AlarmSettingActivity.this, uri);
                    if (ring != null) {
                        ring.play();
                    }
                }
            }
        });

        chooseRingGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Log.d("gpns", "choose ring：" + "checkedId=" + checkedId);
            choosedRingIndex = checkedId;
            rm.stopPreviousRingtone();
            if (ring != null) {
                ring.stop();
            }
            /* 判断位置不为0则播放的条目为position-1 */
            if (choosedRingIndex != 0) {
                try {
                    rm = new RingtoneManager(AlarmSettingActivity.this);
                    rm.setType(RingtoneManager.TYPE_ALARM);
                    rm.setStopPreviousRingtone(true);
                    rm.getCursor();
                    rm.getRingtone(choosedRingIndex - 1).play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            /* position为0是跟随系统，先得到系统所使用的铃声，然后播放 */
            if (choosedRingIndex == 0) {
                Uri uri = RingtoneManager.getActualDefaultRingtoneUri(AlarmSettingActivity.this,
                        RingtoneManager.TYPE_ALARM);
                if (uri != null) {
                    ring = RingtoneManager.getRingtone(AlarmSettingActivity.this, uri);
                    if (ring != null) {
                        ring.play();
                    }
                }
            }
        });

        chooseCustomGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Log.d("gpns", "choose custom：" + "checkedId=" + checkedId);
            choosedCoutomIndex = checkedId;
            if (rm != null) {
                rm.stopPreviousRingtone();
            }
            if (ring != null) {
                ring.stop();
            }
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getPath() + File.separator
                            + "CarOnlineSound" + File.separator + soundNameList.get(choosedCoutomIndex));
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } else if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getPath() + File.separator
                            + "CarOnlineSound" + File.separator + soundNameList.get(choosedCoutomIndex));
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        chooseNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (rm != null) {
                    rm.stopPreviousRingtone();
                }
                if (ring != null) {
                    ring.stop();
                }
                mMediaPlayer.reset();
                notificationType = 0;
                chooseNotificationGroup.setVisibility(View.VISIBLE);
                chooseRingGroup.setVisibility(View.GONE);
                chooseCustomGroup.setVisibility(View.GONE);
                chooseCustomTip.setVisibility(View.GONE);
                chooseRing.setChecked(false);
                chooseCustom.setChecked(false);
                getNotification();
                indexNotification = PreferenceUtil.getInt(Constant.PREFERENCE_CHOOSE_AUDIO, 0);
                // chooseNotificationGroup.removeAllViews();
                if (isFirstNotification) {
                    for (int i = 0; i < notificationList.size(); i++) {
                        final int choosedI = i;
                        RadioButton radioButton = new RadioButton(AlarmSettingActivity.this);
                        radioButton.setId(i);
                        radioButton.setText(notificationList.get(i));
                        chooseNotificationGroup.addView(radioButton);
                        radioButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (rm != null) {
                                    rm.stopPreviousRingtone();
                                }
                                if (ring != null) {
                                    ring.stop();
                                }
                                if (choosedI != 0) {
                                    try {
                                        rm = new RingtoneManager(AlarmSettingActivity.this);
                                        rm.setType(RingtoneManager.TYPE_NOTIFICATION);
                                        rm.setStopPreviousRingtone(true);
                                        rm.getCursor();
                                        rm.getRingtone(choosedI - 1).play();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                /* position为0是跟随系统，先得到系统所使用的铃声，然后播放 */
                                if (choosedI == 0) {
                                    Uri uri = RingtoneManager.getActualDefaultRingtoneUri(AlarmSettingActivity.this,
                                            RingtoneManager.TYPE_NOTIFICATION);
                                    if (uri != null) {
                                        ring = RingtoneManager.getRingtone(AlarmSettingActivity.this, uri);
                                        if (ring != null) {
                                            ring.play();
                                        }
                                    }
                                }
                            }
                        });
                    }
                    chooseNotificationGroup.check(indexNotification);
                    isFirstNotification = false;
                }
            }
        });

        chooseRing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (rm != null) {
                    rm.stopPreviousRingtone();
                }
                if (ring != null) {
                    ring.stop();
                }
                mMediaPlayer.reset();
                notificationType = 1;
                chooseNotificationGroup.setVisibility(View.GONE);
                chooseRingGroup.setVisibility(View.VISIBLE);
                chooseCustomGroup.setVisibility(View.GONE);
                chooseCustomTip.setVisibility(View.GONE);
                chooseNotification.setChecked(false);
                chooseCustom.setChecked(false);
                getRing();
                indexRing = PreferenceUtil.getInt(Constant.PREFERENCE_CHOOSE_RING, 0);
                // chooseRingGroup.removeAllViews();
                if (isFirstRing) {
                    for (int i = 0; i < ringList.size(); i++) {
                        final int choosedI = i;
                        RadioButton radioButton = new RadioButton(AlarmSettingActivity.this);
                        radioButton.setId(i);
                        radioButton.setText(ringList.get(i));
                        chooseRingGroup.addView(radioButton);
                        radioButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (rm != null) {
                                    rm.stopPreviousRingtone();
                                }
                                if (ring != null) {
                                    ring.stop();
                                }
                                if (choosedI != 0) {
                                    try {
                                        rm = new RingtoneManager(AlarmSettingActivity.this);
                                        rm.setType(RingtoneManager.TYPE_ALARM);
                                        rm.setStopPreviousRingtone(true);
                                        rm.getCursor();
                                        rm.getRingtone(choosedI - 1).play();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                /* position为0是跟随系统，先得到系统所使用的铃声，然后播放 */
                                if (choosedI == 0) {
                                    Uri uri = RingtoneManager.getActualDefaultRingtoneUri(AlarmSettingActivity.this,
                                            RingtoneManager.TYPE_ALARM);
                                    ring = RingtoneManager.getRingtone(AlarmSettingActivity.this, uri);
                                    if (ring != null) {
                                        ring.play();
                                    }
                                }
                            }
                        });
                    }
                    chooseRingGroup.check(indexRing);
                    isFirstRing = false;
                }
            }
        });

        chooseCustom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (rm != null) {
                    rm.stopPreviousRingtone();
                }
                if (ring != null) {
                    ring.stop();
                }
                mMediaPlayer.reset();
                chooseNotification.setChecked(false);
                chooseRing.setChecked(false);
                getSound();
                if (soundList != null && soundList.size() != 0) {
                    notificationType = 2;
                    chooseNotificationGroup.setVisibility(View.GONE);
                    chooseRingGroup.setVisibility(View.GONE);
                    chooseCustomGroup.setVisibility(View.VISIBLE);
                    chooseCustomTip.setVisibility(View.GONE);
                    nameSound = PreferenceUtil.getString(Constant.PREFERENCE_CHOOSE_SOUND, soundNameList.get(0));
                    indexSound = 0;
                    for (int i = 0; i < soundNameList.size(); i++) {
                        if (nameSound.equals(soundNameList.get(i))) {
                            indexSound = i;
                            break;
                        }
                    }
                    // chooseCustomGroup.removeAllViews();
                    if (isFirstCoutom) {
                        for (int i = 0; i < soundList.size(); i++) {
                            final int choosedI = i;
                            RadioButton radioButton = new RadioButton(AlarmSettingActivity.this);
                            radioButton.setId(i);
                            radioButton.setText(soundList.get(i));
                            chooseCustomGroup.addView(radioButton);
                            radioButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (rm != null) {
                                        rm.stopPreviousRingtone();
                                    }
                                    if (ring != null) {
                                        ring.stop();
                                    }
                                    try {
                                        if (mMediaPlayer.isPlaying()) {
                                            mMediaPlayer.reset();
                                            mMediaPlayer.setDataSource(
                                                    Environment.getExternalStorageDirectory().getPath()
                                                            + File.separator + "CarOnlineSound" + File.separator
                                                            + soundNameList.get(choosedI));
                                            mMediaPlayer.prepare();
                                            mMediaPlayer.start();
                                        } else if (!mMediaPlayer.isPlaying()) {
                                            mMediaPlayer.setDataSource(
                                                    Environment.getExternalStorageDirectory().getPath()
                                                            + File.separator + "CarOnlineSound" + File.separator
                                                            + soundNameList.get(choosedI));
                                            mMediaPlayer.prepare();
                                            mMediaPlayer.start();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        chooseCustomGroup.check(indexSound);
                        isFirstCoutom = false;
                    }
                } else {
                    chooseNotificationGroup.setVisibility(View.GONE);
                    chooseRingGroup.setVisibility(View.GONE);
                    chooseCustomGroup.setVisibility(View.GONE);
                    chooseCustomTip.setVisibility(View.VISIBLE);
                }
            }
        });

        notificationType = PreferenceUtil.getInt(Constant.PREFERENCE_NOTIFICATION_TYPE, 0);
        if (notificationType == 0) {
            chooseNotification.setChecked(true);
        } else if (notificationType == 1) {
            chooseRing.setChecked(true);
        } else if (notificationType == 2) {
            chooseCustom.setChecked(true);
        }

        new AlertDialog.Builder(this).setTitle(R.string.set_audio_type).setView(chooseAudioView)
                .setPositiveButton(R.string.ok, (dialog1, whichButton) -> {
                    PreferenceUtil.commitInt(Constant.PREFERENCE_NOTIFICATION_TYPE, notificationType);
                    int position = 0;
                    if (notificationType == 0) {
                        position = choosedNotificationIndex;
                        PreferenceUtil.commitInt(Constant.PREFERENCE_CHOOSE_AUDIO, choosedNotificationIndex);
                    } else if (notificationType == 1) {
                        position = choosedRingIndex;
                        PreferenceUtil.commitInt(Constant.PREFERENCE_CHOOSE_RING, choosedRingIndex);
                    } else if (notificationType == 2) {
                        position = choosedCoutomIndex;
                        if (soundNameList != null && soundNameList.size() > 0 && choosedCoutomIndex >= 0 && choosedCoutomIndex < soundNameList.size()) {
                            PreferenceUtil.commitString(Constant.PREFERENCE_CHOOSE_SOUND,
                                    soundNameList.get(choosedCoutomIndex));
                        }
                    }
                    GpnsUtil.setGpnsNotiSound(AlarmSettingActivity.this, notificationType, position);

                    if (rm != null) {
                        rm.stopPreviousRingtone();
                    }
                    if (ring != null) {
                        ring.stop();
                    }
                    mMediaPlayer.reset();

                    isFirstNotification = true;
                    isFirstRing = true;
                    isFirstCoutom = true;
                }).setNegativeButton(R.string.cancel, (dialog12, whichButton) -> {
                    try {
                        if (rm != null) {
                            rm.stopPreviousRingtone();
                        }
                        if (ring != null) {
                            ring.stop();
                        }
                        mMediaPlayer.reset();

                        isFirstNotification = true;
                        isFirstRing = true;
                        isFirstCoutom = true;

                        Field field = dialog12.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog12, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).create().show();
    }

    private void getSound() {
        String soundFilePath = getSdSoundDir(this);
        if (!StringUtil.isTrimEmpty(soundFilePath)) {
            File mfile = new File(soundFilePath);
            File[] files = mfile.listFiles();
            if (files != null && files.length != 0) {
                getFileName(files);
            } else {
                return;
            }
        } else {
            showToast(getString(R.string.sdcard_not_mounted));
            return;
        }

    }

    private void getFileName(File[] files) {
        if (files != null) {
            // 先判断目录是否为空，否则会报空指针
            soundNameList = new ArrayList<String>();
            soundList = new ArrayList<String>();
            for (File file : files) {
                if (file != null && file.isFile()) {
                    String fileName = file.getName();
                    soundNameList.add(fileName);
                    String s = fileName;
                    if (fileName != null && fileName.contains(".")) {
                        s = fileName.substring(0, fileName.lastIndexOf(".")).toString();
                    }
                    soundList.add(s);
                }
            }
        }
    }

    /**
     * 获取声音目录
     */
    private String getSdSoundDir(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File fExternalStorageDirectory = Environment.getExternalStorageDirectory();
            File soundDir = new File(fExternalStorageDirectory, "CarOnlineSound");
            boolean result = false;
            if (!soundDir.exists()) {
                result = soundDir.mkdir();
            }
            return soundDir.toString();
        } else {
            return "";
        }
    }

    private void soundDialog(int title, String[] listArray, int choosedPreference) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setSingleChoiceItems(listArray, choosedPreference, (dialog1, which) -> {
            choosedWhich = which;
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getPath() + File.separator
                            + "CarOnlineSound" + File.separator + soundNameList.get(choosedWhich));
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } else if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getPath() + File.separator
                            + "CarOnlineSound" + File.separator + soundNameList.get(choosedWhich));
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                }
            } catch (Exception e) {
                e.printStackTrace(); // To change body of catch statement
                // use File | Settings | File
                // Templates.
            }
        });
        builder.setPositiveButton(R.string.ok, (dialog12, which) -> {
            which = choosedWhich;
            PreferenceUtil.commitString(Constant.PREFERENCE_CHOOSE_SOUND, soundNameList.get(which));
            mMediaPlayer.reset();
        });
        builder.setNegativeButton(R.string.no, (dialog13, which) -> {
            mMediaPlayer.reset();
        });
        builder.create().show();
    }

    public void getNotification() {
        /* 新建一个arraylist来接收从系统中获取的短信铃声数据 */
        notificationList = new ArrayList<String>();
        /* 添加“跟随系统”选项 */
        notificationList.add(getString(R.string.alarm_setting_list_default));
        /* 获取RingtoneManager */
        rm = new RingtoneManager(this);
        /* 指定获取类型为短信铃声 */
        rm.setType(RingtoneManager.TYPE_NOTIFICATION);
        /* 创建游标 */
        cursor = rm.getCursor();
        /* 游标移动到第一位，如果有下一项，则添加到ringlist中 */
        if (cursor.moveToFirst()) {
            do { // 游标获取RingtoneManager的列inde x
                notificationList.add(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX));
            } while (cursor.moveToNext());
        }
    }

    public void getRing() {
        /* 新建一个arraylist来接收从系统中获取的短信铃声数据 */
        ringList = new ArrayList<String>();
        /* 添加“跟随系统”选项 */
        ringList.add("跟随系统");
        /* 获取RingtoneManager */
        rm = new RingtoneManager(this);
        /* 指定获取类型为短信铃声 */
        rm.setType(RingtoneManager.TYPE_ALARM);
        /* 创建游标 */
        cursor = rm.getCursor();
        /* 游标移动到第一位，如果有下一项，则添加到ringlist中 */
        if (cursor.moveToFirst()) {
            do { // 游标获取RingtoneManager的列inde x
                ringList.add(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX));
            } while (cursor.moveToNext());
        }
    }

    private void simpleDialog(int title, String[] listArray, int choosedPreference) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setSingleChoiceItems(listArray, choosedPreference, (dialog1, which) -> {
            choosedWhich = which;
            /* 判断位置不为0则播放的条目为position-1 */
            if (choosedWhich != 0) {
                try {

                    RingtoneManager rm1 = new RingtoneManager(AlarmSettingActivity.this);
                    rm1.setType(RingtoneManager.TYPE_ALL);
                    rm1.getCursor();
                    rm1.getRingtone(choosedWhich - 1).play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            /* position为0是跟随系统，先得到系统所使用的铃声，然后播放 */
            if (choosedWhich == 0) {

                Uri uri = RingtoneManager.getActualDefaultRingtoneUri(AlarmSettingActivity.this,
                        RingtoneManager.TYPE_NOTIFICATION);
                if (uri != null) {
                    Ringtone ring1 = RingtoneManager.getRingtone(AlarmSettingActivity.this, uri);
                    if (ring1 != null) {
                        ring1.play();
                    }
                }
            }

        });
        builder.setPositiveButton(R.string.ok, (dialog12, which) -> {
            which = choosedWhich;
            if (which > 0 && which < ringList.size()) {
                PreferenceUtil.commitInt(Constant.PREFERENCE_CHOOSE_AUDIO, which);
            }
        });
        builder.setNegativeButton(R.string.no, (dialog13, which) -> {
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if(data.hasExtra(AlarmTypeListActivity.ALARM_OPTION)) {
                pushSetting = (PushSetting) data.getExtras().getSerializable(AlarmTypeListActivity.ALARM_OPTION);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    private void getAlarmOption() {
        showLoading(getString(R.string.please_wait));
        Disposable d = DataEngine.getAllMainApi()
            .getAlarmOption(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount,
                AllOnlineApp.sToken.access_token, "", AllOnlineApp.sChannelID)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespAlarmOption>() {
                @Override
                public void onNext(RespAlarmOption respAlarmOption) {
                    hideLoading();
                    pushSetting = respAlarmOption.getData();
                    if (pushSetting == null) {
                        return;
                    }
                    receiveCbx.setChecked(pushSetting.isPush());
                    if (receiveCbx.isChecked()) {
                        dl.setVisibility(View.VISIBLE);
                    } else {
                        dl.setVisibility(View.GONE);
                    }
                    soundCbx.setChecked(pushSetting.isSound());
                    if (soundCbx.isChecked()) {
                        chooseAudioLayout.setVisibility(View.VISIBLE);
                        chooseAudioIv.setVisibility(View.VISIBLE);
                    } else {
                        chooseAudioLayout.setVisibility(View.GONE);
                        chooseAudioIv.setVisibility(View.GONE);
                    }
                    vibrationCbx.setChecked(pushSetting.isShake());
                    if (pushSetting.getStart_time() == 0 && pushSetting.getEnd_time() == 1439) {
                        noticePeriodCbox.setChecked(true);
                        noticeBeginTimeLayout.setVisibility(View.GONE);
                        noticeEndTimeLayout.setVisibility(View.GONE);
                        noticePeriodIv.setVisibility(View.GONE);
                    } else {
                        noticePeriodCbox.setChecked(false);
                        noticeBeginTimeLayout.setVisibility(View.VISIBLE);
                        noticeEndTimeLayout.setVisibility(View.VISIBLE);
                        noticePeriodIv.setVisibility(View.VISIBLE);
                        beginTimeTv.setText(TimeUtil.TimeLong2String(pushSetting.getStart_time()));
                        endTimeTv.setText(TimeUtil.TimeLong2String(pushSetting.getEnd_time()));
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    hideLoading();
                    showToast(e.getErrMessage());
                    finish();
                }
            });
        subscribeRx(d);
    }

    private void setAlarmOption() {
        StringBuilder sb = new StringBuilder();
        if (pushSetting != null && pushSetting.getAlarm_type() != null) {
            for (int i = 0; i < pushSetting.getAlarm_type().size(); i++) {
                if (pushSetting.getAlarm_type().get(i).push) {
                    sb.append(pushSetting.getAlarm_type().get(i).id).append(",");
                }
            }
        }
        showLoading(getString(R.string.please_wait));
        Disposable d = DataEngine.getAllMainApi()
            .setAlarmOption(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount,
                AllOnlineApp.sToken.access_token, "", pushSetting.isPush() ? 1 : 0, pushSetting.getStart_time(),
                pushSetting.getEnd_time(), sb.toString(), pushSetting.isSound() ? 1 : 0, pushSetting.isShake() ? 1 : 0,
                AllOnlineApp.sChannelID)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespBase>() {
                @Override
                public void onNext(RespBase respBase) {
                    hideLoading();
                    finish();
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    hideLoading();
                    showToast(e.getErrMessage());
                    finish();
                }
            });
        subscribeRx(d);
    }

    @Override
    public void onBackPressed() {
        setAlarmTypes();
    }
}
