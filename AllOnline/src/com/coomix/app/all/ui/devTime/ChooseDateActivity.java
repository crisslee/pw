package com.coomix.app.all.ui.devTime;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
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
import com.coomix.app.all.ui.base.BaseActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/1/3.
 */
public class ChooseDateActivity extends BaseActivity {
    private final int WEEK_DAYS = 7;
    private final int REQUEST_CHOOSE_TIME = 0;
    @BindView(R.id.list)
    RecyclerView rvList;
    @BindView(R.id.topbar)
    MyActionbar actionbar;
    List<DateMode> data;
    private int selectDate = -1;
    private DateAdapter adapter;
    private Calendar curCal;
    private int locMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_date);
        ButterKnife.bind(this);
        initIntentData();
        initView();
    }

    private void initIntentData() {
        Intent t = getIntent();
        if (t == null) {
            return;
        }
        if (t.hasExtra(ChooseTimeActivity.LOC_MODE)) {
            locMode = t.getIntExtra(ChooseTimeActivity.LOC_MODE, DevMode.LOC_GPS);
        }
    }

    private void initView() {
        actionbar.setTitle(R.string.back_to_select_date);
        actionbar.setLeftImageResource(R.drawable.btn_back);
        actionbar.setLeftImageClickListener(v -> cancel());
        actionbar.setLeftText(R.string.choose_date_left);
        actionbar.setLeftTextCLickListener(v -> cancel());
        actionbar.setRightText(R.string.bindphone_next);
        actionbar.setRightTextClickListener(v -> toChooseTime());

        GridLayoutManager lm = new GridLayoutManager(this, WEEK_DAYS);
        rvList.setLayoutManager(lm);
        initDateData();
        adapter = new DateAdapter();
        rvList.setAdapter(adapter);
    }

    private void toChooseTime() {
        if (selectDate == -1) {
            showToast(getString(R.string.choose_date_no_select));
            return;
        }
        DateMode d = data.get(selectDate);
        if (curCal == null) {
            curCal = Calendar.getInstance();
        }
        curCal.set(Calendar.YEAR, d.iYear);
        curCal.set(Calendar.MONTH, d.iMonth);
        curCal.set(Calendar.DAY_OF_MONTH, d.iDay);

        Intent t = new Intent(this, ChooseTimeActivity.class);
        t.putExtra(ChooseTimeActivity.SHOW_LOC_MODE, true);
        t.putExtra(ChooseTimeActivity.LOC_MODE, locMode);
        startActivityForResult(t, REQUEST_CHOOSE_TIME);
    }

    private void initDateData() {
        data = new ArrayList<>(1450);
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
                data.add(d);
                int addNull = getDayInWeekIndex(c);
                while (addNull > 0) {
                    DateMode nul = new DateMode();
                    nul.iType = DateMode.TYPE_NULL;
                    data.add(nul);
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
            data.add(d);

            if (newDay == c.getActualMaximum(Calendar.DATE)) {
                //一个月最后一天，也要补齐空格
                int addNull = 6 - getDayInWeekIndex(c);
                while (addNull > 0) {
                    DateMode dateNull = new DateMode();
                    dateNull.iType = DateMode.TYPE_NULL;
                    data.add(dateNull);
                    addNull--;
                }
            }

            c.add(Calendar.DAY_OF_MONTH, 1);
            newYear = c.get(Calendar.YEAR);
            newMonth = c.get(Calendar.MONTH);
            newDay = c.get(Calendar.DAY_OF_MONTH);
        }
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

    private void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_TIME && resultCode == RESULT_OK) {
            if (data != null) {
                int day = data.getIntExtra(ChooseTimeActivity.AMPM, 0);
                int hour = data.getIntExtra(ChooseTimeActivity.HOUR, 0);
                int minute = data.getIntExtra(ChooseTimeActivity.MINUTE, 0);
                if (data.hasExtra(ChooseTimeActivity.LOC_MODE)) {
                    locMode = data.getIntExtra(ChooseTimeActivity.LOC_MODE, DevMode.LOC_GPS);
                }
                if (curCal != null) {
                    curCal.set(Calendar.AM_PM, day);
                    curCal.set(Calendar.HOUR, hour);
                    curCal.set(Calendar.MINUTE, minute);
                    Bundle b = new Bundle();
                    b.putSerializable(LoopFragment.NEW_DATE, curCal);
                    Intent t = new Intent();
                    t.putExtras(b);
                    if (data.hasExtra(ChooseTimeActivity.LOC_MODE)) {
                        t.putExtra(ChooseTimeActivity.LOC_MODE, locMode);
                    }
                    setResult(RESULT_OK, t);
                } else {
                    setResult(RESULT_CANCELED);
                }
            } else {
                setResult(RESULT_CANCELED);
            }
        } else if (requestCode == REQUEST_CHOOSE_TIME && resultCode == RESULT_CANCELED) {
            setResult(RESULT_CANCELED);
        }
        finish();
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
            return data.get(position).iType;
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
                        if (data.get(position).iType == DateMode.TYPE_TITLE) {
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
                View v = LayoutInflater.from(ChooseDateActivity.this).inflate(R.layout.date_select_title_item, parent,
                    false);
                return new TitleHolder(v);
            } else if (viewType == DateMode.TYPE_NULL || viewType == DateMode.TYPE_DATE) {
                View v = LayoutInflater.from(ChooseDateActivity.this).inflate(R.layout.date_select_item_dev, parent,
                    false);
                return new DateHolder(v);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final DateMode dateMode = data.get(position);
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
                    if (position == selectDate) {
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
                            selectDate = position;
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
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
