package com.coomix.app.all.ui.setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.ActivityStateManager;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.ui.alarm.AlarmSettingActivity;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.boot.BootActivity;
import com.coomix.app.all.ui.update.GoomeUpdateAgent;
import com.coomix.app.all.util.CacheCleaner;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.ExtraConstants;
import com.coomix.app.framework.util.NetworkUtil;
import com.coomix.app.framework.util.OSUtil;
import com.umeng.analytics.MobclickAgent;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SettingActivity extends BaseActivity {
    private TextView textClearCache;
    private TextView textSelectMap;
    private TextView newVersionTv;
    private LinearLayout llDebug;
    private static int chooseditem;
    private int choosedWhich;
    private int choosedMap;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private String[] useMap;
    private volatile boolean mIsCleaning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ev_function", "系统设置");
        MobclickAgent.onEvent(SettingActivity.this, "ev_function", map);

        setContentView(R.layout.activity_setting);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.mineActionbar);
        actionbar.initActionbar(true, R.string.setting, 0, 0);

        //设置围栏半径
        final TextView textFence = (TextView) findViewById(R.id.right_fenceradius);
        int defaultFenceRadius = SettingDataManager.getInstance(this).getFenceRange();
        textFence.setText(String.valueOf(defaultFenceRadius) + getString(R.string.unit_meter));
        findViewById(R.id.rl_setting_fenceradius).setOnClickListener(view -> {
            View layout = getLayoutInflater().inflate(R.layout.dialog_default_fence_radius, null);
            final EditText radiusEt = (EditText) layout.findViewById(R.id.et_fence_radius);
            radiusEt.setText(String.valueOf(defaultFenceRadius));

            final AlertDialog dlgsDate = new AlertDialog.Builder(SettingActivity.this)
                .setInverseBackgroundForced(true).setTitle(R.string.preference_title_efence_radius)
                .setView(layout).setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (TextUtils.isEmpty(radiusEt.getText().toString())) {
                        showToast(getString(R.string.cmd_check_input));
                    } else {
                        try {
                            int fenceRadius = Integer.valueOf(radiusEt.getText().toString());
                            if (fenceRadius >= 200 && fenceRadius <= 5000) {
                                SettingDataManager.getInstance(SettingActivity.this).setFenceRange(fenceRadius);
                                textFence.setText(String.valueOf(fenceRadius) + getString(R.string.unit_meter));
                            } else {
                                showToast(getString(R.string.notice_efence_radius));
                            }
                        } catch (Exception e) {
                            showToast(getString(R.string.notice_efence_radius));
                        }
                    }
                }).setNegativeButton(R.string.cancel, null).create();
            dlgsDate.show();
        });

        //选择地图
        textSelectMap = (TextView) findViewById(R.id.right_usemap);
        useMap = getResources().getStringArray(R.array.entries_list_preference_map_no_google);
        int index = SettingDataManager.getInstance(this).getMapTypeInt();
        if (index >= useMap.length || index < 0) {
            SettingDataManager.getInstance(this).setMapTypeInt(Constant.MAP_TYPE_BAIDU);
            textSelectMap.setText(useMap[0]);
        } else {
            textSelectMap.setText(useMap[index]);
        }
        findViewById(R.id.rl_setting_usemap).setOnClickListener(view -> {
            choosedWhich = SettingDataManager.getInstance(SettingActivity.this).getMapTypeInt();
            chooseditem = R.string.preference_title_map;
            simpleDialog(R.string.preference_title_map, R.array.entries_list_preference_map_no_google, choosedWhich);
        });

        //报警设置
        findViewById(R.id.rl_alarm_setting).setOnClickListener(view -> {
            startActivity(new Intent(SettingActivity.this, AlarmSettingActivity.class));
        });

        //清除缓存
        textClearCache = (TextView) findViewById(R.id.right_clean_cache);
        textClearCache.setText(R.string.setting_clean_cache_calculating);
        Disposable d = Flowable.just(0).subscribeOn(Schedulers.io()).map(integer -> {
            mIsCleaning = true;
            long size = CacheCleaner.with(getApplicationContext()).getFileSize();
            String pattern = "%.2f M";
            String msg = String.format(pattern, size / 1024.0 / 1024);
            mIsCleaning = false;
            return msg;
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(s -> textClearCache.setText(s));
        mCompositeDisposable.add(d);
        textClearCache.setOnClickListener(v -> {
            if (mIsCleaning) {
                showToast(getString(R.string.setting_clean_cache_waiting));
                return;
            }

            Disposable dis = Flowable.just(0).throttleFirst(600, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io()).map(integer -> {
                    CacheCleaner.with(getApplicationContext()).cleanCache();
                    return 0;
                })
                .observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    showToast(getString(R.string.setting_clean_cache_done));
                    textClearCache.setText("0.00 M");
                });
            mCompositeDisposable.add(dis);
        });

        //软件版本
        newVersionTv = (TextView) findViewById(R.id.versionContent);
        if (null != AllOnlineApp.gUpdateInfo && AllOnlineApp.gUpdateInfo.update) {
            newVersionTv.setText(getString(R.string.download_new_version_text) + AllOnlineApp.gUpdateInfo.verName);
            newVersionTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_more_7, 0, 0, 0);
            newVersionTv.setTextColor(getResources().getColor(R.color.text_blue));
            newVersionTv.setOnClickListener(view -> {
                showUpdate();
            });
        } else {
            newVersionTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            newVersionTv.setEnabled(false);
            newVersionTv.setText(getString(R.string.new_version_text, OSUtil.getAppVersionName(this)));
        }

        llDebug = findViewById(R.id.llDebug);
        if (Constant.IS_DEBUG_MODE) {
            findViewById(R.id.volume).setOnClickListener((v) -> {
                showVolume();
            });
            findViewById(R.id.notiChannel).setOnClickListener((v) -> {
                showNotiChannel();
            });
            findViewById(R.id.notiTest).setOnClickListener(v -> {
                showNotiTest();
            });
        } else {
            llDebug.setVisibility(View.GONE);
        }
    }

    private void showNotiTest() {
        Log.i("felix", "tid="+Thread.currentThread().getId()+", tname="+Thread.currentThread().getName());
        Log.i("felix", "pid="+ Process.myPid()+",pname="+AllOnlineApp.getCurProcessName(this));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constant.channelId);
        Notification n = builder.setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setTicker("万物测试通知")
            .setContentTitle("万物测试通知Title")
            .setContentText("万物测试通知Content")
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
            .build();
        NotificationManager mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.notify(1001, n);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3 * 1000);
                    Log.i("felix", "tid="+Thread.currentThread().getId()+", tname="+Thread.currentThread().getName());
                    Log.i("felix", "pid="+ Process.myPid()+",pname="+AllOnlineApp.getCurProcessName(SettingActivity.this));
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(SettingActivity.this, Constant.channelId);
                    Notification n = builder.setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setTicker("万物测试通知")
                        .setContentTitle("万物测试子线程通知Title")
                        .setContentText("万物测试子线程通知Content")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                        .build();
                    NotificationManager mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    mgr.notify(1002, n);
                }catch ( Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showNotiChannel() {
        //test
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = m.getNotificationChannel(Constant.channelId);
        showToast(channel.toString());
    }

    private void showVolume() {
        AudioManager m = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //system
        int sysMax = m.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        int sysCur = m.getStreamVolume(AudioManager.STREAM_SYSTEM);
        //alarm
        int alMax = m.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int alCur = m.getStreamVolume(AudioManager.STREAM_ALARM);
        //notification
        int notiMax = m.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        int notiCur = m.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        showToast("系统：" + sysMax + "," + sysCur + ",Alarm:" + alMax + "," + alCur + ",Noti:" + notiMax + "," + notiCur);
    }

    private void showUpdate() {
        int type = NetworkUtil.checkNetwork(getApplicationContext());
        if (type == NetworkUtil.wifi) {
            if (AllOnlineApp.getAppConfig().isBuglyUpgradeAgent()) {
            } else {
                GoomeUpdateAgent.startDownload(SettingActivity.this, AllOnlineApp.gUpdateInfo, true);
            }
            finish();
        } else if (type == NetworkUtil.mobile) {
            showDialog();
        } else if (type == 0) {
            Toast.makeText(this, "当前网络不可用，请稍候重试", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 移动网络状态下，确认流量更新
     */
    private void showDialog() {
        final Dialog dialog = new AlertDialog.Builder(SettingActivity.this).create();
        dialog.show();
        // dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    finish();
                    return true;
                } else {
                    return false;
                }
            }
        });
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_mobile);
        // 为确认按钮添加事件,执行退出应用操作
        window.findViewById(R.id.dialog_sure).setOnClickListener(
            v -> {
                if (AllOnlineApp.getAppConfig().isBuglyUpgradeAgent()) {
                } else {
                    GoomeUpdateAgent.startDownload(SettingActivity.this, AllOnlineApp.gUpdateInfo, false);
                }
                dialog.dismiss();
                finish();
            });

        // 关闭alert对话框架
        window.findViewById(R.id.dialog_cancel).setOnClickListener(
            v -> {
                dialog.dismiss();
                finish();
            });
    }

    private void simpleDialog(int title, int listArray, int choosedPreference) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setSingleChoiceItems(listArray, choosedPreference, (dialog, which) -> {
            choosedWhich = which;
        });
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            which = choosedWhich;
            if (chooseditem == R.string.preference_title_map) {
                if (which < 0 || which > useMap.length - 1) {
                    return;
                }
                choosedMap = which;
                if (useMap != null && choosedMap >= useMap.length) {
                    return;
                }

                String oldMap = textSelectMap.getText().toString();
                String newMap = useMap[choosedMap];
                if (!StringUtil.isTrimEmpty(oldMap) && newMap.equals(oldMap)) {
                    return;
                }
                chooseMap();
            }
        });
        builder.setNegativeButton(R.string.no, (dialog, which) -> {
        });
        builder.create().show();
    }

    private void chooseMap() {
        final Dialog dialog_app = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title))
            .setMessage(getString(R.string.dialog_msg))
            .setPositiveButton(getString(R.string.dialog_ok), (dialog, which) -> {
                SettingDataManager.getInstance(SettingActivity.this).setMapTypeInt(choosedMap);
                textSelectMap.setText(useMap[choosedMap]);
                Intent intent = new Intent(SettingActivity.this, BootActivity.class);
                intent.putExtra(ExtraConstants.EXTRA_START_PROGRAM, true);
                startActivity(intent);
                dialog.dismiss();
                finish();

                ActivityStateManager.finishAll();
            }).create();
        dialog_app.show();
    }
}
