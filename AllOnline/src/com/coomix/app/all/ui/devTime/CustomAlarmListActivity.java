package com.coomix.app.all.ui.devTime;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DevMode;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.TimeUtil;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.ViewUtil;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/1/9.
 */
public class CustomAlarmListActivity extends BaseActivity {

    public static final String DATE = "date";
    public static final String ALARM_LIST = "alarm_list";
    public static final String LAST_TIME = "last_time";
    public static final String MAX_INTERVAL = "max_interval";
    public static final String MIN_INTERVAL = "min_interval";
    private static final int REQUEST_ADD_ALARM = 0;

    @BindView(R.id.topbar)
    MyActionbar actionbar;
    @BindView(R.id.rvList)
    SwipeMenuRecyclerView rvList;
    @BindView(R.id.add)
    TextView add;
    private List<AlarmItem> alarmList;
    private AlarmAdapter adapter;
    private String title;
    private long lastTime;
    private int maxInterval, minInterval;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_alarm_list);
        ButterKnife.bind(this);
        initIntentData();
        initView();
    }

    private void initIntentData() {
        Intent t = getIntent();
        if (t == null) {
            return;
        }
        if (t.hasExtra(DATE)) {
            title = t.getStringExtra(DATE);
        }
        if (t.hasExtra(LAST_TIME)) {
            lastTime = t.getLongExtra(LAST_TIME, System.currentTimeMillis() / 1000);
        }
        if (t.hasExtra(MAX_INTERVAL)) {
            maxInterval = t.getIntExtra(MAX_INTERVAL, 7 * 24 * 60);
        }
        if (t.hasExtra(MIN_INTERVAL)) {
            minInterval = t.getIntExtra(MIN_INTERVAL, 5);
        }
        if (t.hasExtra(ALARM_LIST)) {
            alarmList = (List<AlarmItem>) t.getSerializableExtra(ALARM_LIST);
        }
        if (alarmList == null) {
            alarmList = new ArrayList<>();
        }
        observer.onChanged();
    }

    private void initView() {
        actionbar.setTitle(title);
        actionbar.setLeftImageResource(R.drawable.btn_back);
        actionbar.setLeftImageClickListener(v -> goBack());
        actionbar.setLeftText(R.string.alarm_list_left);
        actionbar.setLeftTextCLickListener(v -> goBack());
        actionbar.setRightText(R.string.edit);
        actionbar.setRightTextClickListener(v -> editAlarm());
        add.setOnClickListener(v -> toAddAlarm());
        ViewUtil.setBg(add, ThemeManager.getInstance().getBGColorDrawable(this));
        add.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());

        rvList.setSwipeMenuCreator(menu);
        rvList.setSwipeMenuItemClickListener(l);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rvList.setLayoutManager(lm);
        adapter = new AlarmAdapter();
        adapter.registerAdapterDataObserver(observer);
        rvList.setAdapter(adapter);
    }

    private boolean isEmpty() {
        return alarmList == null || alarmList.size() == 0;
    }

    private void addItem(AlarmItem item) {
        alarmList.add(item);
        observer.onChanged();
        rvList.getAdapter().notifyDataSetChanged();
    }

    private void removeItem(int position) {
        alarmList.remove(position);
        observer.onChanged();
        rvList.getAdapter().notifyDataSetChanged();
    }

    private void editAlarm() {
        if (isEmpty()) {
            return;
        }
        actionbar.setRightText(R.string.community_done);
        actionbar.setRightTextClickListener(v -> editDone());
        for (int i = 0; i < alarmList.size(); i++) {
            alarmList.get(i).showDelete = true;
            rvList.getAdapter().notifyDataSetChanged();
        }
    }

    private void editDone() {
        if (isEmpty()) {
            return;
        }
        actionbar.setRightText(R.string.edit);
        actionbar.setRightTextClickListener(v -> editAlarm());
        for (int i = 0; i < alarmList.size(); i++) {
            alarmList.get(i).showDelete = false;
            rvList.getAdapter().notifyDataSetChanged();
        }
    }

    private void toAddAlarm() {
        Intent t = new Intent(this, ChooseTimeActivity.class);
        t.putExtra(ChooseTimeActivity.SHOW_LOC_MODE, true);
        startActivityForResult(t, REQUEST_ADD_ALARM);
    }

    private void goBack() {
        if (alarmList != null && alarmList.size() > 0) {
            Intent t = new Intent();
            t.putExtra(ALARM_LIST, (Serializable) alarmList);
            setResult(RESULT_OK, t);
        } else {
            Intent t = new Intent();
            t.putExtra(DATE, title);
            setResult(RESULT_CANCELED, t);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        goBack();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_ALARM && resultCode == Activity.RESULT_OK) {
            int day = data.getIntExtra(ChooseTimeActivity.AMPM, 0);
            int hour = data.getIntExtra(ChooseTimeActivity.HOUR, 0);
            int minute = data.getIntExtra(ChooseTimeActivity.MINUTE, 0);
            int locMode = DevMode.LOC_GPS;
            if (data.hasExtra(ChooseTimeActivity.LOC_MODE)) {
                locMode = data.getIntExtra(ChooseTimeActivity.LOC_MODE, DevMode.LOC_GPS);
            }
            DateFormat df = new SimpleDateFormat(TimeUtil.FORMAT_DATE);
            Date d = null;
            try {
                d = df.parse(title);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            c.set(Calendar.AM_PM, day);
            c.set(Calendar.HOUR, hour);
            c.set(Calendar.MINUTE, minute);
            if (lastTime + maxInterval * 60 < c.getTimeInMillis() / 1000) {
                showToast(getString(R.string.over_max_interval_hint, maxInterval / 60 / 24));
                return;
            }
            long newTime = c.getTimeInMillis() / 1000;
            if (lastTime < newTime) {
                lastTime = newTime;
            }
            AlarmItem item = new AlarmItem();
            item.locMode = locMode;
            item.time = c;
            if (checkMinInterval(item)) {
                addItem(item);
            } else {
                showToast(getString(R.string.min_interval_hint, minInterval));
            }
        }
    }

    private boolean checkMinInterval(AlarmItem item) {
        int len = alarmList.size();
        Calendar c = Calendar.getInstance();
        Calendar c1 = item.time;
        for (int i = 0; i < len; i++) {
            AlarmItem it = alarmList.get(i);
            c = it.time;
            if (Math.abs((int) ((c.getTimeInMillis() / 1000 - c1.getTimeInMillis() / 1000) / 60)) < minInterval) {
                return false;
            }
        }
        return true;
    }

    private SwipeMenuCreator menu = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int position) {
            int width = getResources().getDimensionPixelSize(R.dimen.space_10x);
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            int type = rvList.getAdapter().getItemViewType(position);
            if (type == AlarmAdapter.ITEM_TIME) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(CustomAlarmListActivity.this)
                    .setBackground(R.color.red)
                    .setText("删除")
                    .setTextColor(Color.WHITE)
                    .setTextSize(15)
                    .setWidth(width)
                    .setHeight(height);
                rightMenu.addMenuItem(deleteItem);// 添加菜单到右侧。
            }
        }
    };

    private SwipeMenuItemClickListener l = new SwipeMenuItemClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge, int position) {
            menuBridge.closeMenu();

            int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。

            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                removeItem(position);
            }
        }
    };

    private RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (isEmpty()) {
                rvList.setVisibility(View.GONE);
                add.setVisibility(View.VISIBLE);
            } else {
                rvList.setVisibility(View.VISIBLE);
                add.setVisibility(View.GONE);
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            onChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            onChanged();
        }
    };

    class AlarmHolder extends RecyclerView.ViewHolder {
        ImageView delete;
        TextView ampm;
        TextView time;
        TextView locMode;

        public AlarmHolder(View itemView) {
            super(itemView);
            delete = (ImageView) itemView.findViewById(R.id.delete);
            ampm = (TextView) itemView.findViewById(R.id.ampm);
            time = (TextView) itemView.findViewById(R.id.time);
            locMode = (TextView) itemView.findViewById(R.id.locMode);
        }
    }

    class AddHolder extends RecyclerView.ViewHolder {
        TextView add;

        public AddHolder(View itemView) {
            super(itemView);
            add = (TextView) itemView.findViewById(R.id.add);
        }
    }

    class AlarmAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int ITEM_TIME = 1;
        public static final int ITEM_ADD = 2;

        public AlarmAdapter() {
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == ITEM_TIME) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev_mode_alarm, parent,
                    false);
                return new AlarmHolder(v);
            } else if (viewType == ITEM_ADD) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev_mode_alarm_add, parent,
                    false);
                return new AddHolder(v);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int type = getItemViewType(position);
            if (type == ITEM_TIME) {
                AlarmHolder h = (AlarmHolder) holder;
                AlarmItem item = alarmList.get(position);
                if (item.showDelete) {
                    h.delete.setVisibility(View.VISIBLE);
                    h.delete.setOnClickListener(v -> {
                        removeItem(position);
                    });
                } else {
                    h.delete.setVisibility(View.GONE);
                }
                String str = item.time.get(Calendar.AM_PM) == Calendar.AM ? getString(R.string.am)
                    : getString(R.string.pm);
                h.ampm.setText(str);
                DateFormat f = new SimpleDateFormat("hh:mm");
                String time = f.format(item.time.getTimeInMillis());
                h.time.setText(time);
                h.locMode.setVisibility(View.VISIBLE);
                switch (item.locMode) {
                    case DevMode.LOC_WIFI:
                        h.locMode.setText(R.string.location_wifi_hint);
                        break;
                    case DevMode.LOC_GPS:
                        h.locMode.setText(R.string.location_gps_hint);
                        break;
                    case DevMode.LOC_BASE:
                        h.locMode.setText(R.string.location_lbs_hint);
                        break;
                    default:
                        h.locMode.setVisibility(View.GONE);
                        break;
                }
            } else if (type == ITEM_ADD) {
                AddHolder h = (AddHolder) holder;
                h.add.setOnClickListener(v -> toAddAlarm());
                ViewUtil.setBg(h.add, ThemeManager.getInstance().getBGColorDrawable(h.add.getContext()));
                h.add.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isEmpty()) {
                return 0;
            }
            if (position == alarmList.size()) {
                return ITEM_ADD;
            } else {
                return ITEM_TIME;
            }
        }

        @Override
        public int getItemCount() {
            if (isEmpty()) {
                return 0;
            } else {
                return alarmList.size() + 1;
            }
        }
    }
}
