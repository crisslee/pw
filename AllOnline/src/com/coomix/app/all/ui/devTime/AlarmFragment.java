package com.coomix.app.all.ui.devTime;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DevMode;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.util.ViewUtil;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/12/25.
 */
public class AlarmFragment extends DevTimeFragment {
    private static final int REQUEST_ADD_ALARM = 0;
    @BindView(R.id.topbar)
    MyActionbar topbar;
    @BindView(R.id.rvList)
    SwipeMenuRecyclerView rvList;
    @BindView(R.id.add)
    TextView add;
    private List<AlarmItem> itemList = new ArrayList<>();
    private AlarmAdapter adapter;
    private int minInterval;

    public AlarmFragment() {
    }

    public static AlarmFragment newInstance(DevMode mode) {
        AlarmFragment f = new AlarmFragment();
        f.mode = mode;
        f.currMode = DevMode.MODE_ALARM;
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_alarm;
    }

    @Override
    protected void initView() {
        topbar.setTitle(R.string.title_mode_alarm);
        topbar.setLeftImageResource(R.drawable.btn_back);
        topbar.setLeftText(R.string.senior);
        topbar.setLeftImageClickListener(v -> setMode(R.string.title_mode_alarm));
        topbar.setLeftTextCLickListener(v -> setMode(R.string.title_mode_alarm));
        topbar.setRightText(R.string.edit);
        topbar.setRightTextClickListener(v -> editAlarm());

        add.setOnClickListener(v -> toAddAlarm());
        ViewUtil.setBg(add, ThemeManager.getInstance().getBGColorDrawable(getContext()));
        add.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());

        rvList.setSwipeMenuCreator(menu);
        rvList.setSwipeMenuItemClickListener(l);
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        rvList.setLayoutManager(lm);
        adapter = new AlarmAdapter();
        adapter.registerAdapterDataObserver(observer);
        rvList.setAdapter(adapter);
    }

    @Override
    protected void processContent() {
        itemList.clear();
        if (!isAlarmMode() || TextUtils.isEmpty(mode.content)) {
            minInterval = MIN_INTERVAL;
            observer.onChanged();
            return;
        }
        minInterval = mode.min_interval;
        String[] strs = mode.content.split(",");
        Calendar c = Calendar.getInstance();
        for (String s : strs) {
            int index = s.indexOf(":");
            if (index < 0) {
                continue;
            }
            c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.substring(0, index).trim()));
            c.set(Calendar.MINUTE, Integer.parseInt(s.substring(index + 1).trim()));
            AlarmItem item = new AlarmItem();
            item.ampm = c.get(Calendar.AM_PM);
            item.hour = c.get(Calendar.HOUR);
            item.minute = c.get(Calendar.MINUTE);
            addItem(item);
        }
    }

    private boolean isAlarmMode() {
        return mode != null && mode.mode == DevMode.MODE_ALARM;
    }

    private void addItem(AlarmItem item) {
        itemList.add(item);
        observer.onChanged();
        rvList.getAdapter().notifyDataSetChanged();
    }

    private void removeItem(int position) {
        itemList.remove(position);
        observer.onChanged();
        rvList.getAdapter().notifyDataSetChanged();
    }

    private boolean isEmpty() {
        return itemList == null || itemList.size() == 0;
    }

    private SwipeMenuCreator menu = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int position) {
            int width = getResources().getDimensionPixelSize(R.dimen.space_10x);
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            int type = rvList.getAdapter().getItemViewType(position);
            if (type == AlarmAdapter.ITEM_TIME) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getContext()).setBackground(R.color.red)
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

    private void editAlarm() {
        if (isEmpty()) {
            return;
        }
        topbar.setRightText(R.string.community_done);
        topbar.setRightTextClickListener(v -> editDone());
        for (int i = 0; i < itemList.size(); i++) {
            itemList.get(i).showDelete = true;
            rvList.getAdapter().notifyDataSetChanged();
        }
    }

    private void editDone() {
        if (isEmpty()) {
            return;
        }
        topbar.setRightText(R.string.edit);
        topbar.setRightTextClickListener(v -> editAlarm());
        for (int i = 0; i < itemList.size(); i++) {
            itemList.get(i).showDelete = false;
            rvList.getAdapter().notifyDataSetChanged();
        }
    }

    private void toAddAlarm() {
        Intent t = new Intent(getContext(), ChooseTimeActivity.class);
        t.putExtra(ChooseTimeActivity.SHOW_LOC_MODE, false);
        startActivityForResult(t, REQUEST_ADD_ALARM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (itemList.size() == 4) {
            showToast(getString(R.string.max_alarm_count));
            return;
        }
        if (requestCode == REQUEST_ADD_ALARM && resultCode == Activity.RESULT_OK) {
            int day = data.getIntExtra(ChooseTimeActivity.AMPM, 0);
            int hour = data.getIntExtra(ChooseTimeActivity.HOUR, 0);
            int minute = data.getIntExtra(ChooseTimeActivity.MINUTE, 0);
            AlarmItem item = new AlarmItem();
            item.ampm = day;
            item.hour = hour;
            item.minute = minute;
            if (checkMinInterval(item)) {
                addItem(item);
            } else {
                showToast(getString(R.string.min_interval_hint, minInterval));
            }
        }
    }

    private boolean checkMinInterval(AlarmItem item) {
        int len = itemList.size();
        Calendar c = Calendar.getInstance();
        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.AM_PM, item.ampm);
        c1.set(Calendar.HOUR, item.hour);
        c1.set(Calendar.MINUTE, item.minute);
        for (int i = 0; i < len; i++) {
            AlarmItem it = itemList.get(i);
            c.set(Calendar.AM_PM, it.ampm);
            c.set(Calendar.HOUR, it.hour);
            c.set(Calendar.MINUTE, it.minute);
            if (Math.abs((int) ((c.getTimeInMillis() / 1000 - c1.getTimeInMillis() / 1000) / 60)) < minInterval) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean checkTime() {
        if (isEmpty()) {
            showInvalidDlg();
            return false;
        }
        return true;
    }

    @Override
    protected String makeContent() {
        StringBuilder result = new StringBuilder();
        if (isEmpty()) {
            return "";
        }
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("HH:mm");
        int len = itemList.size();
        for (int i = 0; i < len; i++) {
            AlarmItem item = itemList.get(i);
            c.set(Calendar.AM_PM, item.ampm);
            c.set(Calendar.HOUR, item.hour);
            c.set(Calendar.MINUTE, item.minute);
            result.append(df.format(c.getTimeInMillis()));
            if (i < len - 1) {
                result.append(",");
            }
        }
        return URLEncoder.encode(result.toString());
    }

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

    class AlarmItem {
        boolean showDelete;
        int ampm;
        int hour;
        int minute;
    }

    class AlarmHolder extends RecyclerView.ViewHolder {
        ImageView delete;
        TextView ampm;
        TextView time;

        public AlarmHolder(View itemView) {
            super(itemView);
            delete = (ImageView) itemView.findViewById(R.id.delete);
            ampm = (TextView) itemView.findViewById(R.id.ampm);
            time = (TextView) itemView.findViewById(R.id.time);
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
                AlarmItem item = itemList.get(position);
                if (item.showDelete) {
                    h.delete.setVisibility(View.VISIBLE);
                    h.delete.setOnClickListener(v -> {
                        removeItem(position);
                    });
                } else {
                    h.delete.setVisibility(View.GONE);
                }
                String str = item.ampm == Calendar.AM ? getString(R.string.am) : getString(R.string.pm);
                h.ampm.setText(str);
                Calendar c = Calendar.getInstance();
                c.set(Calendar.AM_PM, item.ampm);
                c.set(Calendar.HOUR, item.hour);
                c.set(Calendar.MINUTE, item.minute);
                DateFormat f = new SimpleDateFormat("hh:mm");
                String time = f.format(c.getTimeInMillis());
                h.time.setText(time);
            } else if (type == ITEM_ADD) {
                AddHolder h = (AddHolder) holder;
                h.add.setOnClickListener(v -> toAddAlarm());
                ViewUtil.setBg(h.add, ThemeManager.getInstance().getBGColorDrawable(getContext()));
                h.add.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isEmpty()) {
                return 0;
            }
            if (position == itemList.size()) {
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
                return itemList.size() + 1;
            }
        }
    }
}
