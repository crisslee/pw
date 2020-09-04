package com.coomix.app.all.ui.devTime;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DevMode;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.all.ui.base.BaseActivity;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/1/5.
 */
public class ChooseTimeActivity extends BaseActivity {
    public static final String SHOW_LOC_MODE = "show_loc_mode";
    public static final String LOC_MODE = "loc_mode";
    public static final String AMPM = "am_pm";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";

    @BindView(R.id.topbar)
    MyActionbar actionbar;
    @BindView(R.id.day)
    NumberPicker day;
    @BindView(R.id.hour)
    NumberPicker hour;
    @BindView(R.id.minute)
    NumberPicker minute;
    @BindView(R.id.locMode)
    ConstraintLayout locMode;
    @BindView(R.id.modeGps)
    TextView modeGps;
    @BindView(R.id.modeBase)
    TextView modeBase;
    @BindView(R.id.modeWifi)
    TextView modeWifi;
    private TextView currSelect;
    private boolean showLocMode;
    private int curLocMode = DevMode.LOC_GPS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_time);
        ButterKnife.bind(this);
        initIntentData();
        initView();
    }

    private void initIntentData() {
        Intent t = getIntent();
        if (t == null) {
            finish();
            return;
        }
        if (t.hasExtra(SHOW_LOC_MODE)) {
            showLocMode = t.getBooleanExtra(SHOW_LOC_MODE, false);
        }
        if (t.hasExtra(LOC_MODE)) {
            curLocMode = t.getIntExtra(LOC_MODE, DevMode.LOC_GPS);
        }
    }

    private void initView() {
        actionbar.setTitle(R.string.history_choose_time);
        actionbar.setLeftImageVisibility(View.GONE);
        actionbar.setLeftText(R.string.cancel);
        actionbar.setLeftTextCLickListener(v -> cancel());
        actionbar.setRightText(R.string.community_done);
        actionbar.setRightTextClickListener(v -> goBack());

        modeGps.setOnClickListener(v -> processModeChange(v));
        modeWifi.setOnClickListener(v -> processModeChange(v));
        modeBase.setOnClickListener(v -> processModeChange(v));
        if (showLocMode) {
            locMode.setVisibility(View.VISIBLE);
            Drawable d = getResources().getDrawable(R.drawable.loc_mode_checked);
            switch (curLocMode) {
                case DevMode.LOC_WIFI:
                    currSelect = modeWifi;
                    setDrawableRight(modeWifi, d);
                    setDrawableRight(modeGps, null);
                    setDrawableRight(modeBase, null);
                    break;
                case DevMode.LOC_GPS:
                    currSelect = modeGps;
                    setDrawableRight(modeWifi, null);
                    setDrawableRight(modeGps, d);
                    setDrawableRight(modeBase, null);
                    break;
                case DevMode.LOC_BASE:
                    currSelect = modeBase;
                    setDrawableRight(modeWifi, null);
                    setDrawableRight(modeGps, null);
                    setDrawableRight(modeBase, d);
                    break;
                default:
                    break;
            }
        } else {
            locMode.setVisibility(View.GONE);
        }

        String[] days = new String[] { "上午", "下午" };
        initNumberPicker(day, 0, 0, days.length - 1, "");
        day.setDisplayedValues(days);
        initNumberPicker(hour, 1, 1, 12, "");
        initNumberPicker(minute, 0, 0, 59, "");
    }

    private void initNumberPicker(NumberPicker picker, int value, int min, int max, String label) {
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setMaxValue(max);
        picker.setMinValue(min);
        picker.setValue(value);
        picker.setFocusable(true);
        picker.setFocusableInTouchMode(true);
        picker.setFormatter(i -> {
            return String.format("%02d%s", i, label);
        });
    }

    private void processModeChange(View v) {
        if (v != currSelect) {
            setDrawableRight(currSelect, null);
            setDrawableRight((TextView) v, getResources().getDrawable(R.drawable.loc_mode_checked));
            currSelect = (TextView) v;
        }
        switch (v.getId()) {
            case R.id.modeWifi:
                curLocMode = DevMode.LOC_WIFI;
                break;
            case R.id.modeGps:
                curLocMode = DevMode.LOC_GPS;
                break;
            case R.id.modeBase:
                curLocMode = DevMode.LOC_BASE;
                break;
            default:
                break;
        }
    }

    private void setDrawableRight(TextView t, Drawable d) {
        if (d != null) {
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        }
        t.setCompoundDrawables(null, null, d, null);
    }

    private void goBack() {
        int d = day.getValue();
        int h = hour.getValue();
        if (h == 12) {
            h = 0;
        }
        int m = minute.getValue();
        Intent t = new Intent();
        t.putExtra(AMPM, d);
        t.putExtra(HOUR, h);
        t.putExtra(MINUTE, m);
        if (showLocMode) {
            t.putExtra(LOC_MODE, curLocMode);
        }
        setResult(RESULT_OK, t);
        finish();
    }

    private void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
