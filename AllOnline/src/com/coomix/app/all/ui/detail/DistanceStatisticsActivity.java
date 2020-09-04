package com.coomix.app.all.ui.detail;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.StatisticsSummary;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RespStatistics;
import com.coomix.app.all.ui.base.BaseActivity;
import io.reactivex.disposables.Disposable;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/11/5.
 */
public class DistanceStatisticsActivity extends BaseActivity {

    @BindView(R.id.actionBar)
    MyActionbar bar;
    @BindView(R.id.rvList)
    RecyclerView rvList;
    @BindView(R.id.tvSummary)
    TextView tvSummary;
    StatisticsAdapter adapter;
    String imei = "";
    String time = "";
    String name = "";
    String token = "";
    long beginTime;
    long endTime;
    int timezone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_statistics);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initView() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(DeviceDetailInfoActivity.DEV_IMEI)) {
            imei = intent.getStringExtra(DeviceDetailInfoActivity.DEV_IMEI);
        }
        if (intent != null && intent.hasExtra("time")) {
            time = intent.getStringExtra("time");
        }
        if (intent != null && intent.hasExtra("device_name")) {
            name = intent.getStringExtra("device_name");
        }

        //actionbar
        bar.setLeftImageResource(R.drawable.btn_back);
        bar.setLeftText(R.string.detail);
        bar.setTitle(name);
        bar.setRightText(time);
        bar.setRightTextClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTime();
            }
        });
        bar.setLeftTextCLickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //list
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
        rvList.setLayoutManager(lm);
        adapter = new StatisticsAdapter();
        rvList.setAdapter(adapter);
    }

    private void initData() {
        token = AllOnlineApp.sToken.access_token;
        setTime();
        timezone = Constant.getTimeZoon() / 3600;
        getTimeRunStatus();
    }

    private void setTime() {
        bar.setRightText(time);
        long current = System.currentTimeMillis();
        if (time.equals("昨天")) {
            long zero = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
            //昨天
            beginTime = (zero - 24 * 3600 * 1000) / 1000L;
            endTime = zero / 1000L;
        } else if (time.equals("本周")) {
            Calendar cal = Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            beginTime = cal.getTimeInMillis() / 1000L;
            endTime = current / 1000L;
        } else if (time.equals("上周")) {
            Calendar cal = Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            beginTime = cal.getTimeInMillis() / 1000L - 7 * 24 * 3600;
            endTime = cal.getTimeInMillis() / 1000L;
        } else if (time.equals("本月")) {
            Calendar cal = Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            beginTime = cal.getTimeInMillis() / 1000L;
            endTime = current / 1000L;
        } else if (time.equals("上月")) {
            Calendar cal = Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            endTime = cal.getTimeInMillis() / 1000L;
            cal.add(Calendar.MONTH, -1);
            beginTime = cal.getTimeInMillis() / 1000L;
        } else {
            beginTime = endTime = current / 1000L;
        }
    }

    private void selectTime() {
        final String items[] = getResources().getStringArray(R.array.statistics_time);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("里程统计");
        builder.setIcon(R.drawable.ic_launcher);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                time = items[which];
                setTime();
                getTimeRunStatus();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void getTimeRunStatus() {
        Disposable d = DataEngine.getAllMainApi()
            .getRunstatus(imei, token, beginTime, endTime, timezone, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespStatistics>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    showErr(e.getErrMessage());
                }

                @Override
                public void onNext(RespStatistics respStatistics) {
                    adapter.setData(respStatistics.getData(), respStatistics.getKeys());
                    setTotal(respStatistics.getSummary());
                }
            });
        subscribeRx(d);
    }

    private void setTotal(StatisticsSummary summary) {
        String str = "";
        if (summary == null) {
            str = getString(R.string.statistic_summary, 0, 0, 0);
        } else {
            str = getString(R.string.statistic_summary, summary.totalmilestat, summary.totaloutspeed,
                summary.totalstop);
        }
        SpannableString ss = new SpannableString(str);
        ss.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_s)), 0, 2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvSummary.setText(ss);
    }
}
