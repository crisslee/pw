package com.coomix.app.all.ui.devTime;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
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
import com.coomix.app.framework.util.TimeUtil;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/12/27.
 */
public class CustomFragment extends DevTimeFragment {
    private final int WEEK_DAYS = 7;
    private static final int REQUEST_ALARM_LIST = 0;
    @BindView(R.id.topbar)
    MyActionbar topbar;
    @BindView(R.id.list)
    RecyclerView rvList;

    private List<DateMode> dateList;
    private DateAdapter adapter;
    private Map<String, List<AlarmItem>> alarmMap = new HashMap<>();
    private long lastTime;
    private int maxInterval, minInterval;

    public CustomFragment() {
    }

    public static CustomFragment newInstance(DevMode mode) {
        CustomFragment f = new CustomFragment();
        f.mode = mode;
        f.currMode = DevMode.MODE_SCHEDULE;
        return f;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_custom;
    }

    @Override
    protected void initView() {
        topbar.setTitle(R.string.title_mode_custom);
        topbar.setLeftImageResource(R.drawable.btn_back);
        topbar.setLeftImageClickListener(v -> setMode(R.string.title_mode_custom));
        topbar.setLeftText(R.string.senior);
        topbar.setLeftTextCLickListener(v -> setMode(R.string.title_mode_custom));

        GridLayoutManager lm = new GridLayoutManager(getContext(), WEEK_DAYS);
        rvList.setLayoutManager(lm);
        initDateData();
        adapter = new DateAdapter();
        rvList.setAdapter(adapter);
    }

    @Override
    protected void processContent() {
        lastTime = System.currentTimeMillis() / 1000;
        if (!isCustomMode() || TextUtils.isEmpty(mode.content)) {
            maxInterval = 7 * 24 * 60;
            minInterval = MIN_INTERVAL;
            return;
        }
        maxInterval = mode.max_interval;
        minInterval = mode.min_interval;
        String[] strs = mode.content.split("\\|");
        for (int i = 0; i < strs.length; i++) {
            int index = strs[i].indexOf("#");
            if (index < 0) {
                continue;
            }
            int locMode = Integer.parseInt(strs[i].substring(0, index).trim());
            long time = Long.parseLong(strs[i].substring(index + 1).trim());
            if (time == 0) {
                continue;
            }
            if (time > lastTime) {
                lastTime = time;
            }
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(time * 1000L);
            String key = TimeUtil.long2DateString(c);
            AlarmItem item = new AlarmItem();
            item.locMode = locMode;
            item.time = c;
            List<AlarmItem> list = alarmMap.get(key);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(item);
            alarmMap.put(key, list);
        }
    }

    private void initDateData() {
        dateList = new ArrayList<>(1450);
        Calendar c = Calendar.getInstance();
        int MAX_YEAR = 3;
        c.setTimeInMillis(System.currentTimeMillis());
        int currYear = c.get(Calendar.YEAR);
        int currMonth = c.get(Calendar.MONTH);
        int currDay = c.get(Calendar.DAY_OF_MONTH);
        c.set(Calendar.DAY_OF_MONTH, 1);
        int newYear = c.get(Calendar.YEAR);
        int newMonth = c.get(Calendar.MONTH);
        int newDay = c.get(Calendar.DAY_OF_MONTH);
        while (newYear < currYear + MAX_YEAR || newYear == currYear && newMonth < currMonth + 1) {
            if (newDay == 1) {
                DateMode d = new DateMode();
                d.iType = DateMode.TYPE_TITLE;
                d.iYear = newYear;
                d.iMonth = newMonth;
                dateList.add(d);
                int addNull = getDayInWeekIndex(c);
                while (addNull > 0) {
                    DateMode nul = new DateMode();
                    nul.iType = DateMode.TYPE_NULL;
                    dateList.add(nul);
                    addNull--;
                }
            }
            DateMode d = new DateMode();
            d.iType = DateMode.TYPE_DATE;
            d.iYear = newYear;
            d.iMonth = newMonth;
            d.iDay = newDay;
            if (newYear == currYear && newMonth == currMonth && newDay < currDay ||
                newYear == currYear + MAX_YEAR && newMonth == currMonth && newDay > currDay) {
                d.dayStatus = DateMode.UN_CLICK;
            } else if (newYear == currYear && newMonth == currMonth && newDay == currDay) {
                d.dayStatus = DateMode.TODAY;
            }
            dateList.add(d);

            if (newDay == c.getActualMaximum(Calendar.DATE)) {
                //一个月最后一天，也要补齐空格
                int addNull = 6 - getDayInWeekIndex(c);
                while (addNull > 0) {
                    DateMode dateNull = new DateMode();
                    dateNull.iType = DateMode.TYPE_NULL;
                    dateList.add(dateNull);
                    addNull--;
                }
            }

            c.add(Calendar.DAY_OF_MONTH, 1);
            newYear = c.get(Calendar.YEAR);
            newMonth = c.get(Calendar.MONTH);
            newDay = c.get(Calendar.DAY_OF_MONTH);
        }
    }

    private boolean isCustomMode() {
        return mode != null && mode.mode == DevMode.MODE_SCHEDULE && mode.sub_mode == DevMode.SUB_MODE_CUSTOM;
    }

    private int getDayInWeekIndex(Calendar c) {
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return 0;

            case Calendar.MONDAY:
                return 1;

            case Calendar.TUESDAY:
                return 2;

            case Calendar.WEDNESDAY:
                return 3;

            case Calendar.THURSDAY:
                return 4;

            case Calendar.FRIDAY:
                return 5;

            case Calendar.SATURDAY:
                return 6;

            default:
                break;
        }
        return 0;
    }

    private String getModeKey(DateMode mode) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, mode.iYear);
        c.set(Calendar.MONTH, mode.iMonth);
        c.set(Calendar.DAY_OF_MONTH, mode.iDay);
        return TimeUtil.long2DateString(c);
    }

    private boolean dateHasAlarm(DateMode mode) {
        List<AlarmItem> list = alarmMap.get(getModeKey(mode));
        return list != null && list.size() > 0;
    }

    private void toAlarmList(DateMode mode) {
        if (checkOvertime(mode)) {
            showToast(getString(R.string.over_max_interval_hint, maxInterval / 60 / 24));
            return;
        }
        Intent t = new Intent(getContext(), CustomAlarmListActivity.class);
        String key = getModeKey(mode);
        List<AlarmItem> list = alarmMap.get(key);
        if (list != null && list.size() > 0) {
            t.putExtra(CustomAlarmListActivity.ALARM_LIST, (Serializable) list);
        }
        t.putExtra(CustomAlarmListActivity.LAST_TIME, lastTime);
        t.putExtra(CustomAlarmListActivity.MAX_INTERVAL, maxInterval);
        t.putExtra(CustomAlarmListActivity.MIN_INTERVAL, minInterval);
        t.putExtra(CustomAlarmListActivity.DATE, key);
        startActivityForResult(t, REQUEST_ALARM_LIST);
    }

    private boolean checkOvertime(DateMode mode) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, mode.iYear);
        c.set(Calendar.MONTH, mode.iMonth);
        c.set(Calendar.DAY_OF_MONTH, mode.iDay);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        return lastTime + maxInterval * 60 < c.getTimeInMillis() / 1000;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ALARM_LIST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                List<AlarmItem> list = (List<AlarmItem>) data.getSerializableExtra(CustomAlarmListActivity.ALARM_LIST);
                if (list != null && list.size() > 0) {
                    for (AlarmItem item : list) {
                        long newTime = item.time.getTimeInMillis() / 1000;
                        if (lastTime < newTime) {
                            lastTime = newTime;
                        }
                    }
                    AlarmItem item = list.get(0);
                    String key = TimeUtil.long2DateString(item.time);
                    alarmMap.put(key, list);
                    rvList.getAdapter().notifyDataSetChanged();
                }
            }
        } else if (requestCode == REQUEST_ALARM_LIST && resultCode == Activity.RESULT_CANCELED) {
            if (data != null) {
                String key = data.getStringExtra(CustomAlarmListActivity.DATE);
                if (alarmMap.containsKey(key)) {
                    alarmMap.remove(key);
                    rvList.getAdapter().notifyDataSetChanged();
                    updateLastTime();
                }
            }
        }
    }

    private void updateLastTime() {
        lastTime = System.currentTimeMillis() / 1000;
        Set<Map.Entry<String, List<AlarmItem>>> entrys = alarmMap.entrySet();
        if (entrys.size() == 0) {
            return;
        }
        Iterator<Map.Entry<String, List<AlarmItem>>> it = entrys.iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<AlarmItem>> entry = it.next();
            List<AlarmItem> list = entry.getValue();
            if (list == null || list.size() == 0) {
                it.remove();
                continue;
            }
            for (AlarmItem item : list) {
                long newTime = item.time.getTimeInMillis() / 1000;
                if (lastTime < newTime) {
                    lastTime = newTime;
                }
            }
        }
    }

    @Override
    protected boolean checkTime() {
        long preTime = 0, nowTime = 0;
        boolean flagMin = false, flagMax = false;
        Set<Map.Entry<String, List<AlarmItem>>> entrys = alarmMap.entrySet();
        if (entrys.size() == 0) {
            showInvalidDlg();
            return false;
        }
        List<Long> list = new ArrayList<>();
        for (Map.Entry<String, List<AlarmItem>> entry : entrys) {
            List<AlarmItem> items = entry.getValue();
            if (items == null || items.size() == 0) {
                continue;
            }
            for (AlarmItem item : items) {
                list.add(item.time.getTimeInMillis() / 1000);
            }
        }
        Collections.sort(list);
        int len = list.size();
        if (len < 2) {
            return true;
        }
        for (int i = 1; i < list.size(); i++) {
            nowTime = list.get(i);
            preTime = list.get(i - 1);
            if (nowTime - preTime < minInterval * 60) {
                flagMin = true;
                break;
            }
            if (nowTime - preTime > maxInterval * 60) {
                flagMax = true;
                break;
            }
        }
        if (!flagMin && !flagMax) {
            return true;
        }
        String msg = "";
        if (flagMin) {
            msg = getString(R.string.min_interval_hint, minInterval);
        } else if (flagMax) {
            msg = getString(R.string.over_max_interval_hint, maxInterval / 60 / 24);
        }
        msg += TimeUtil.long2MinuteDate(preTime * 1000) + "-" + TimeUtil.long2MinuteDate(nowTime * 1000);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
        return false;
    }

    @Override
    protected String makeContent() {
        StringBuilder s = new StringBuilder();
        Set<Map.Entry<String, List<AlarmItem>>> entrys = alarmMap.entrySet();
        if (entrys.size() == 0) {
            return "";
        }
        for (Map.Entry<String, List<AlarmItem>> entry : entrys) {
            List<AlarmItem> items = entry.getValue();
            if (items == null || items.size() == 0) {
                continue;
            }
            for (AlarmItem item : items) {
                s.append(item.locMode).append("#").append(item.time.getTimeInMillis() / 1000).append("|");
            }
        }
        s.deleteCharAt(s.length() - 1);
        return URLEncoder.encode(s.toString());
    }

    class DateMode {
        /**
         * view的类型
         */
        final static int TYPE_DATE = 0;
        final static int TYPE_TITLE = 1;
        final static int TYPE_NULL = 2;

        /**
         * 日期的类型
         */
        final static int TODAY = 1;
        final static int UN_CLICK = 4;

        private int iType;
        private int dayStatus;

        private int iYear;
        private int iMonth;
        private int iDay;
    }

    class DateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        public DateAdapter() {
            super();
        }

        @Override
        public int getItemViewType(int position) {
            if (position < 0 || position > getItemCount() - 1) {
                return 0;
            }
            return dateList.get(position).iType;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gm = (GridLayoutManager) manager;
                gm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (dateList.get(position).iType == DateMode.TYPE_TITLE) {
                            return WEEK_DAYS;
                        }
                        return 1;
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == DateMode.TYPE_TITLE) {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.date_select_title_item, parent, false);
                return new TitleHolder(v);
            } else if (viewType == DateMode.TYPE_NULL || viewType == DateMode.TYPE_DATE) {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.date_select_item_dev, parent, false);
                return new DateHolder(v);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final DateMode dateMode = dateList.get(position);
            if (dateMode.iType == DateMode.TYPE_TITLE) {
                TitleHolder titleHolder = (TitleHolder) holder;
                titleHolder.textTitle.setText(getString(R.string.year_month, dateMode.iYear, dateMode.iMonth + 1));
            } else if (dateMode.iType == DateMode.TYPE_NULL || dateMode.iType == DateMode.TYPE_DATE) {
                final DateHolder dateHolder = (DateHolder) holder;
                if (dateMode.iType == DateMode.TYPE_NULL) {
                    dateHolder.textDate.setText("");
                } else {
                    //设置显示日期
                    if (dateMode.dayStatus == DateMode.TODAY) {
                        dateHolder.textDate.setText(getString(R.string.today));
                    } else {
                        dateHolder.textDate.setText(String.valueOf(dateMode.iDay));
                    }

                    //设置显示文字的背景和颜色
                    if (dateHasAlarm(dateMode)) {
                        if (dateMode.dayStatus != DateMode.TODAY) {
                            dateMode.dayStatus = 0;
                        }
                        dateHolder.textDate.setTextColor(getResources().getColor(R.color.white));
                        dateHolder.bg.setVisibility(View.VISIBLE);
                    } else {
                        dateHolder.bg.setVisibility(View.GONE);
                        if (dateMode.dayStatus == DateMode.UN_CLICK) {
                            dateHolder.textDate.setTextColor(getResources().getColor(R.color.text_date_future));
                        } else {
                            dateHolder.textDate.setTextColor(getResources().getColor(R.color.color_text_m));
                        }
                    }

                    dateHolder.textDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (dateMode.dayStatus == DateMode.UN_CLICK) {
                                return;
                            }
                            toAlarmList(dateMode);
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return dateList != null ? dateList.size() : 0;
        }
    }

    public static class TitleHolder extends RecyclerView.ViewHolder {
        TextView textTitle;

        public TitleHolder(View itemView) {
            super(itemView);
            textTitle = (TextView) itemView.findViewById(R.id.item_title);
        }
    }

    public static class DateHolder extends RecyclerView.ViewHolder {
        TextView textDate;
        ImageView bg;

        public DateHolder(View itemView) {
            super(itemView);
            textDate = (TextView) itemView.findViewById(R.id.item_date);
            bg = (ImageView) itemView.findViewById(R.id.bg);
        }
    }
}

class AlarmItem implements Serializable {
    private static final long serialVersionUID = 4631987217183584760L;
    boolean showDelete;
    int locMode;
    Calendar time;
}