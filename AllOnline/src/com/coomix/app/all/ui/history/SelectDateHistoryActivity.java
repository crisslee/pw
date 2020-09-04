package com.coomix.app.all.ui.history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.util.ViewUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ssl on 2018/8/2.
 */
public class SelectDateHistoryActivity extends BaseActivity implements View.OnClickListener {

    //最多选择天
    private final int MAX_DAYS = 5;
    private TextView textDataCount;
    private RecyclerView recyclerView;
    private List<DateMode> listDateModes;
    private DateAdapter dateAdapter;
    private int iSelectStart = -1;
    private int iSelectEnd = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_history_calender);

        initViews();
    }

    private void initViews() {
        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.select_history_date, 0, 0);

        textDataCount = (TextView) findViewById(R.id.textDateCount);
        textDataCount.setOnClickListener(this);
        String data = getString(R.string.catch_history, MAX_DAYS);
        SpannableStringBuilder spsbStyl = new SpannableStringBuilder(data);
        spsbStyl.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_s)), 2, data.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textDataCount.setText(spsbStyl);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewDate);

        GridLayoutManager lm = new GridLayoutManager(this, 7);
        recyclerView.setLayoutManager(lm);
        initDateData();
        dateAdapter = new DateAdapter();
        recyclerView.setAdapter(dateAdapter);
        recyclerView.scrollToPosition(listDateModes.size() - 1);

        ViewUtil.setBg(textDataCount, ThemeManager.getInstance().getBGColorDrawable(this));
        textDataCount.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textDateCount:
                long startTime = 0;
                long endTime = 0;
                //开始时间为那天的0时0分0秒；结束时间为那天的23时59分59秒
                if (iSelectStart > 0 && iSelectEnd <= 0) {
                    DateMode dateMode = listDateModes.get(iSelectStart);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, dateMode.iYear);
                    calendar.set(Calendar.MONTH, dateMode.iMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, dateMode.iDay);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    startTime = calendar.getTimeInMillis() / 1000;

                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    endTime = calendar.getTimeInMillis() / 1000;
                    calendar.setTimeInMillis(System.currentTimeMillis());
                } else if (iSelectStart > 0 && iSelectEnd > 0) {
                    DateMode startMode = listDateModes.get(iSelectStart);
                    DateMode endMode = listDateModes.get(iSelectEnd);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, startMode.iYear);
                    calendar.set(Calendar.MONTH, startMode.iMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, startMode.iDay);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    startTime = calendar.getTimeInMillis() / 1000;

                    calendar.set(Calendar.YEAR, endMode.iYear);
                    calendar.set(Calendar.MONTH, endMode.iMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, endMode.iDay);
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    endTime = calendar.getTimeInMillis() / 1000;
                    calendar.setTimeInMillis(System.currentTimeMillis());
                } else {
                    showToast(getString(R.string.pls_select_time));
                    return;
                }
                goToHitory(this, (DeviceInfo) getIntent().getSerializableExtra(Constant.KEY_DEVICE), startTime,
                        endTime);
                finish();
                break;

            default:
                break;
        }
    }

    private void initDateData() {
        listDateModes = new ArrayList<DateMode>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int iCurYear = calendar.get(Calendar.YEAR);
        int iCurMonth = calendar.get(Calendar.MONTH);
        int iCurDay = calendar.get(Calendar.DAY_OF_MONTH);

        //只提供3个月的时间选择
        boolean bBefore3MonthDay = true;
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 3);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        while (calendar.get(Calendar.YEAR) < iCurYear || (calendar.get(Calendar.YEAR) == iCurYear && calendar.get(Calendar.MONTH) <= iCurMonth)) {
            if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                //每个月第一天添加一个title来显示；并且要判断属于星期几，加空格补齐
                DateMode dateTitle = new DateMode();
                dateTitle.iType = DateMode.TYPE_TITLE;
                dateTitle.iYear = calendar.get(Calendar.YEAR);
                dateTitle.iMonth = calendar.get(Calendar.MONTH);
                dateTitle.iDay = calendar.get(Calendar.DAY_OF_MONTH);
                listDateModes.add(dateTitle);

                int iAddNull = getDayInWeekIndex(calendar);
                while (iAddNull > 0) {
                    --iAddNull;
                    DateMode dateNull = new DateMode();
                    dateNull.iType = DateMode.TYPE_NULL;
                    listDateModes.add(dateNull);
                }
            }

            DateMode dateMode = new DateMode();
            dateMode.iType = DateMode.TYPE_DATE;
            dateMode.iYear = calendar.get(Calendar.YEAR);
            dateMode.iMonth = calendar.get(Calendar.MONTH);
            dateMode.iDay = calendar.get(Calendar.DAY_OF_MONTH);
            if (bBefore3MonthDay && dateMode.iDay < iCurDay) {
                //3个月前的今天之前都不可点击选择
                dateMode.dayStatus = DateMode.UN_CLICK;
            } else {
                bBefore3MonthDay = false;
                if (dateMode.iYear == iCurYear && dateMode.iMonth == iCurMonth) {
                    if (dateMode.iDay == iCurDay) {
                        boolean bYesterdayFlag = false;
                        boolean bBeforYesFlag = false;
                        for (int i = listDateModes.size() - 1; i >= 0; i--) {
                            if (listDateModes.get(i).iType == DateMode.TYPE_NULL || listDateModes.get(i).iType == DateMode.TYPE_TITLE) {
                                continue;
                            }
                            if (!bYesterdayFlag) {
                                bYesterdayFlag = true;
                                listDateModes.get(i).dayStatus = DateMode.YESTERDAY;
                            } else if (!bBeforYesFlag) {
                                bBeforYesFlag = true;
                                listDateModes.get(i).dayStatus = DateMode.BEFORE_YESTERDAY;
                            } else {
                                break;
                            }
                        }
                        dateMode.dayStatus = DateMode.TODAY;
                        iSelectStart = listDateModes.size();
                    } else if (dateMode.iDay > iCurDay) {
                        dateMode.dayStatus = DateMode.UN_CLICK;
                    } else {
                        dateMode.dayStatus = 0;
                    }
                } else {
                    dateMode.dayStatus = 0;
                }
            }
            listDateModes.add(dateMode);

            if (calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMaximum(Calendar.DATE)) {
                //一个月最后一天，也要补齐空格
                int iAddNull = 6 - getDayInWeekIndex(calendar);
                while (iAddNull > 0) {
                    --iAddNull;
                    DateMode dateNull = new DateMode();
                    dateNull.iType = DateMode.TYPE_NULL;
                    listDateModes.add(dateNull);
                }
            }

            //自动累加天数到当前月的最大天数，才结束循环
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        }

        //复原日历
        calendar.setTimeInMillis(System.currentTimeMillis());
    }

    private void goToHitory(Context context, DeviceInfo device, long start, long end) {
        Intent intent = SettingDataManager.getInstance(this).getHistoryActIntent(this);
        intent.putExtra(HistoryParentActivity.START_TIME, start);
        intent.putExtra(HistoryParentActivity.END_TIME, end);
        intent.putExtra(Constant.KEY_DEVICE, device);
        context.startActivity(intent);
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
        final static int YESTERDAY = 2;
        final static int BEFORE_YESTERDAY = 3;
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
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
        }

        @Override
        public int getItemViewType(int position) {
            if (position < 0 || position > getItemCount() - 1) {
                return 0;
            }
            return listDateModes.get(position).iType;
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
                        if (listDateModes.get(position).iType == DateMode.TYPE_TITLE) {
                            return 7;
                        }
                        return 1;
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == DateMode.TYPE_TITLE) {
                View v = LayoutInflater.from(SelectDateHistoryActivity.this).inflate(R.layout.date_select_title_item, parent, false);
                return new TitleHolder(v);
            } else if (viewType == DateMode.TYPE_NULL || viewType == DateMode.TYPE_DATE) {
                View v = LayoutInflater.from(SelectDateHistoryActivity.this).inflate(R.layout.date_select_item, parent, false);
                return new DateHolder(v);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final DateMode dateMode = listDateModes.get(position);
            if (dateMode.iType == DateMode.TYPE_TITLE) {
                TitleHolder titleHolder = (TitleHolder) holder;
                titleHolder.textTitle.setText(getString(R.string.year_month, dateMode.iYear, dateMode.iMonth + 1));
            } else if (dateMode.iType == DateMode.TYPE_NULL || dateMode.iType == DateMode.TYPE_DATE) {
                final DateHolder dateHolder = (DateHolder) holder;
                if (dateMode.iType == DateMode.TYPE_NULL) {
                    dateHolder.textDate.setText("");
                } else {
                    //设置显示日期
                    if (dateMode.dayStatus == DateMode.BEFORE_YESTERDAY) {
                        dateHolder.textDate.setText(R.string.before_yesterday);
                    } else if (dateMode.dayStatus == DateMode.YESTERDAY) {
                        dateHolder.textDate.setText(R.string.yesterday);
                    } else if (dateMode.dayStatus == DateMode.TODAY) {
                        dateHolder.textDate.setText(R.string.today);
                    } else if (dateMode.dayStatus == DateMode.UN_CLICK) {
                        dateHolder.textDate.setText(String.valueOf(dateMode.iDay));
                    } else {
                        dateHolder.textDate.setText(String.valueOf(dateMode.iDay));
                    }

                    //设置显示文字的背景和颜色
                    if (position == iSelectStart) {
                        //起始
                        dateHolder.textDate.setBackgroundResource(R.drawable.rounded_half_corners_left_black);
                        dateHolder.textDate.setTextColor(getResources().getColor(R.color.white));
                    } else if (position == iSelectEnd) {
                        //结束
                        dateHolder.textDate.setBackgroundResource(R.drawable.rounded_half_corners_right_black);
                        dateHolder.textDate.setTextColor(getResources().getColor(R.color.white));
                    } else if (position > iSelectStart && position < iSelectEnd) {
                        //位于选择的中间
                        dateHolder.textDate.setBackgroundResource(R.color.register_hint_text);
                        dateHolder.textDate.setTextColor(getResources().getColor(R.color.color_text_m));
                    } else {
                        dateHolder.textDate.setBackgroundResource(R.color.white);
                        if (dateMode.dayStatus == DateMode.TODAY) {
                            dateHolder.textDate.setTextColor(getResources().getColor(R.color.text_date_now));
                        } else if (dateMode.dayStatus == DateMode.UN_CLICK) {
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

                            if (iSelectStart <= 0) {
                                iSelectStart = position;
                                iSelectEnd = -1;
                            } else if (iSelectStart > 0 && iSelectEnd <= 0) {
                                if (position > iSelectStart) {
                                    iSelectEnd = position;
                                    int iSelectedCounts = 0;
                                    for (int i = iSelectStart; i <= iSelectEnd; i++) {
                                        if (listDateModes.get(i).iType == DateMode.TYPE_DATE) {
                                            iSelectedCounts++;
                                        }
                                    }
                                    /**********************最多选择MAX_DAYS天***********************/
                                    if (iSelectedCounts > MAX_DAYS) {
                                        iSelectStart = position;
                                        iSelectEnd = -1;
                                    }
                                } else {
                                    iSelectStart = position;
                                    iSelectEnd = -1;
                                }
                            } else if (iSelectStart > 0 && iSelectEnd > 0) {
                                iSelectStart = position;
                                iSelectEnd = -1;
                            }
                            dateAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return listDateModes != null ? listDateModes.size() : 0;
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

        public DateHolder(View itemView) {
            super(itemView);
            textDate = (TextView) itemView.findViewById(R.id.item_date);
        }
    }
}
