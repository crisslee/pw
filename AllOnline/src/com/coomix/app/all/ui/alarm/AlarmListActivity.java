package com.coomix.app.all.ui.alarm;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.Alarm;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.NetworkUtil;
import com.coomix.app.framework.util.TimeUtils;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RespAlarmDetailList;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.manager.SettingDataManager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AlarmListActivity extends BaseActivity implements OnClickListener, OnLastItemVisibleListener,
    OnRefreshListener<ListView> {

    private final int PAGE_SIZE = 15;
    private TextView settingTv, setReadTv;
    private ImageView rightBtn;
    private LinearLayout readLayout, settingLayout, setReadLayout;
    private TextView cancleReadTv, ensureReadTv;

    private PullToRefreshListView listView;

    private ArrayList<Alarm> alarms = new ArrayList<Alarm>();
    private ArrayList<String> selectedAlarms = new ArrayList<String>();
    private AlarmAdapter alarmAdapter;
    private boolean isLoading = false;
    private View popView;
    private PopupWindow mPopupWindow;
    private int mPopupWindowX;
    private int mPopupWindowY;
    private boolean isEnableSelect = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ev_function", "报警信息");
        MobclickAgent.onEvent(AlarmListActivity.this, "ev_function", map);

        setContentView(R.layout.activity_alarms_list);

        alarmAdapter = new AlarmAdapter(this);
        alarms = new ArrayList<Alarm>();

        initViews();

        initNetData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alarms != null) {
            CommonUtil.clearCollection(alarms);
        }
    }

    protected void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.alarm, 0, R.drawable.btn_right_menu);
        rightBtn = actionbar.getImageRight();

        readLayout = (LinearLayout) findViewById(R.id.read_layout);
        cancleReadTv = (TextView) findViewById(R.id.cancle_read);
        ensureReadTv = (TextView) findViewById(R.id.ensure_read);
        cancleReadTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readLayout.setVisibility(View.GONE);

                isEnableSelect = !isEnableSelect;
                if (!isEnableSelect) {
                    selectedAlarms.clear();
                }
                alarmAdapter.notifyDataSetChanged();
            }
        });

        ensureReadTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(selectedAlarms != null && selectedAlarms.size() > 0)) {
                    Toast.makeText(AlarmListActivity.this, getString(R.string.ensure_read_num), Toast.LENGTH_SHORT).show();
                    return;
                }
                final Dialog dialog = new Dialog(AlarmListActivity.this, R.style.Dialog);
                View dialogView = LayoutInflater.from(AlarmListActivity.this).inflate(R.layout.dialog_alarm_ensure_read, null);
                //获得dialog的window窗口
                Window window = dialog.getWindow();
                //设置dialog在屏幕底部
                window.setGravity(Gravity.BOTTOM);
                window.getDecorView().setPadding(0, 0, 0, 0);
                //获得window窗口的属性
                android.view.WindowManager.LayoutParams lp = window.getAttributes();
                //设置窗口宽度为充满全屏
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                //设置窗口高度为包裹内容
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                //将设置好的属性set回去
                window.setAttributes(lp);
                //将自定义布局加载到dialog上
                dialog.setContentView(dialogView);
                TextView ensureTitle = (TextView) dialogView.findViewById(R.id.message1);
                ensureTitle.setText(String.format(getResources().getString(R.string.ensure_read_dialog), selectedAlarms.size()));

                TextView ensureBtn = (TextView) dialogView.findViewById(R.id.ensureBtn);
                ensureBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        if (selectedAlarms != null && selectedAlarms.size() > 0) {
                            setAlarmRead(selectedAlarms, 0L, false);
                        }
                    }
                });

                TextView cancleBtn = (TextView) dialogView.findViewById(R.id.cancleBtn);
                cancleBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });

        listView = (PullToRefreshListView) findViewById(R.id.list);
        listView.setOnLastItemVisibleListener(this);
        listView.setEmptyView(findViewById(R.id.empty));
        listView.setAdapter(alarmAdapter);
        listView.setOnRefreshListener(this);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                position = position - 1;

                // if (AllOnlineApp.getMapType().equals(Constant.MAP_GOOGLE) &&
                // !AllOnlineApp.getAMapType().equals(Constant.MAP_AMAP)) {
                int iMapType = SettingDataManager.getInstance(AlarmListActivity.this).getMapTypeInt();
                if (iMapType == Constant.MAP_TYPE_GOOGLE) {
                    // 谷歌
                    intent.setClass(AlarmListActivity.this, GAlarmLocationActivity.class);
                } else if (iMapType == Constant.MAP_TYPE_AMAP) {// 高德地图
                    // 高德地图
                    intent.setClass(AlarmListActivity.this, AMapAlarmLocationActivity.class);
                } else if (iMapType == Constant.MAP_TYPE_TENCENT) {// 腾讯地图
                    // 腾讯地图
                    intent.setClass(AlarmListActivity.this, TAlarmLocationActivity.class);
                } else {
                    // 百度
                    intent.setClass(AlarmListActivity.this, BAlarmLocationActivity.class);
                }

                intent.putExtra("ALARM", alarms.get(position));
                startActivity(intent);
            }
        });

        popView = getLayoutInflater().inflate(R.layout.layout_popup_menu_alarm, null);
        settingLayout = (LinearLayout) popView.findViewById(R.id.alarm_settings_layout);
        setReadLayout = (LinearLayout) popView.findViewById(R.id.alarm_settings_read);
        settingTv = (TextView) popView.findViewById(R.id.settingsTv);
        setReadTv = (TextView) popView.findViewById(R.id.setReadTv);

        settingLayout.setOnClickListener(this);
        setReadLayout.setOnClickListener(this);

        mPopupWindow = new PopupWindow(popView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        mPopupWindowX = getResources().getDimensionPixelSize(R.dimen.alarm_option_popupwindow_x);
        mPopupWindowY = getResources().getDimensionPixelSize(R.dimen.alarm_option_popupwindow_y);

        actionbar.setRightImageClickListener(view -> mPopupWindow.showAsDropDown(rightBtn, -mPopupWindowX, mPopupWindowY));
    }

    private void getAlarmDetailList(String pageDir, long timestamp) {
        showLoading(getString(R.string.please_wait));
        isLoading = true;
        Disposable d = DataEngine.getAllMainApi()
                .getAlarmDetailList(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount,
                        AllOnlineApp.sToken.access_token, timestamp, "", "default", pageDir, PAGE_SIZE)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespAlarmDetailList>() {
                    @Override
                    public void onNext(RespAlarmDetailList respAlarmDetailList) {
                        hideLoading();
                        isLoading = false;
                        listView.onRefreshComplete();
                        if (pageDir.equals(AlarmDetailListActivity.PAGE_DIR_NEXT)) {
                            alarms.addAll(0, (ArrayList<Alarm>) respAlarmDetailList.getData());
                        } else {
                            alarms.addAll((ArrayList<Alarm>) respAlarmDetailList.getData());
                        }
                        alarmAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        hideLoading();
                        isLoading = false;
                        showToast(e.getErrMessage());
                        listView.onRefreshComplete();
                    }
                });
        subscribeRx(d);
    }

    /***删除，相当于标记为已读**/
    private void setAlarmRead(ArrayList<String> listIds, long timestamp, boolean except) {
        if (listIds == null || listIds.size() <= 0) {
            return;
        }
        showLoading(getString(R.string.please_wait));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0, size = listIds.size(); i < size; i++) {
            stringBuilder.append(listIds.get(i));
            if (i != size - 1) {
                stringBuilder.append(",");
            }
        }
        Disposable d = DataEngine.getAllMainApi()
                .setAlarmRead(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount,
                        AllOnlineApp.sToken.access_token, timestamp, "", "default", stringBuilder.toString(), except)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBase>() {
                    @Override
                    public void onNext(RespBase resp) {
                        hideLoading();
                        readLayout.setVisibility(View.GONE);

                        Iterator<String> iterator = selectedAlarms.iterator();
                        while (iterator.hasNext()) {
                            String selectd = iterator.next();
                            for (Alarm alarm : alarms) {
                                if (alarm.getId().equals(selectd)) {
                                    alarms.remove(alarm);
                                    break;
                                }
                            }
                        }
                        selectedAlarms.clear();
                        isEnableSelect = false;
                        alarmAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrMessage());
                    }
                });
        subscribeRx(d);
    }

    @Override
    public void onClick(View v) {
        if (v == settingLayout) {
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
            startActivity(new Intent(this, AlarmSettingActivity.class));
        } else if (v == setReadLayout) {
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
            readLayout.setVisibility(View.VISIBLE);
            isEnableSelect = !isEnableSelect;
            if (!isEnableSelect) {
                selectedAlarms.clear();
            }
            alarmAdapter.notifyDataSetChanged();
        }
    }

    public void initNetData() {
        // 判断网络
        if (!NetworkUtil.isNetworkConnected(this)) {
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            return;
        }

        getAlarmDetailList(AlarmDetailListActivity.PAGE_DIR_NEXT, 0L);
    }

    @Override
    public void onLastItemVisible() {
        if (alarms.size() > 0 && !isLoading) {
            getAlarmDetailList(AlarmDetailListActivity.PAGE_DIR_PRE, alarms.get(alarms.size() - 1).getAlarm_time());
        }
    }

    private class AlarmAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;

        // private String[] directionArray;
        // private String[] gpsStateArray;

        public AlarmAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
            // directionArray =
            // context.getResources().getStringArray(R.array.direction_array);
            // gpsStateArray =
            // context.getResources().getStringArray(R.array.gps_state_array);
        }

        @Override
        public int getCount() {
            return alarms == null ? 0 : alarms.size();
        }

        @Override
        public Object getItem(int position) {
            return alarms.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.alarm_item_new, null, false);
                convertView.setTag(holder);
                holder.alarmDate = (TextView) convertView.findViewById(R.id.alarm_time);
                holder.alarmType = (TextView) convertView.findViewById(R.id.alarm_type);
                holder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
                holder.itemCbx = (CheckBox) convertView.findViewById(R.id.itemCbx);
            }
            holder = (ViewHolder) convertView.getTag();

            final Alarm alarm = alarms.get(position);

            holder.deviceName.setText(alarm.getDev_name());
            holder.alarmDate.setText(TimeUtils.formatMyTime(alarm.getAlarm_time() * 1000, AlarmListActivity.this));

            // converTime
            holder.alarmType.setText(alarm.getAlarm_type());

            holder.itemCbx.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (!selectedAlarms.contains(alarms.get(position).getId())) {
                            selectedAlarms.add(alarms.get(position).getId());
                        }
                    } else {
                        selectedAlarms.remove(alarms.get(position).getId());
                    }
                }
            });

            if (isEnableSelect) {
                holder.itemCbx.setVisibility(View.VISIBLE);
            } else {
                holder.itemCbx.setVisibility(View.GONE);
            }
            if (selectedAlarms.contains(alarm.getId())) {
                holder.itemCbx.setChecked(true);
            } else {
                holder.itemCbx.setChecked(false);
            }

            return convertView;
        }

        class ViewHolder {
            public TextView deviceName, alarmType, alarmDate;
            public CheckBox itemCbx;
        }
    }

    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        if (alarms.size() > 0 && !isLoading) {
            getAlarmDetailList(AlarmDetailListActivity.PAGE_DIR_NEXT, alarms.get(0).getAlarm_time());
        }
    }
}
