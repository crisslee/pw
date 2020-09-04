package com.coomix.app.all.ui.devTime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DevMode;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.TimeUtil;
import java.net.URLEncoder;
import java.util.Calendar;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/12/27.
 */
public class LoopFragment extends DevTimeFragment {
    public static final String NEW_DATE = "new_date";

    private final int REQUEST_START_DATE = 0;
    private final int REQUEST_END_DATE = 1;
    @BindView(R.id.topbar)
    MyActionbar topbar;
    @BindView(R.id.sDate)
    TextView start;
    @BindView(R.id.eDate)
    TextView end;
    @BindView(R.id.time)
    EditText span;
    @BindView(R.id.maxIntervalHint)
    TextView maxIntervalHint;

    private long sDate, eDate;
    private int locMode, spanTime, maxInterval, minInterval;

    public LoopFragment() {
    }

    public static LoopFragment newInstance(DevMode mode) {
        LoopFragment f = new LoopFragment();
        f.mode = mode;
        f.currMode = DevMode.MODE_SCHEDULE;
        return f;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_loop;
    }

    @Override
    protected void initView() {
        topbar.setTitle(R.string.title_mode_loop);
        topbar.setLeftImageResource(R.drawable.btn_back);
        topbar.setLeftImageClickListener(v -> setMode(R.string.title_mode_loop));
        topbar.setLeftText(R.string.senior);
        topbar.setLeftTextCLickListener(v -> setMode(R.string.title_mode_loop));

        start.setOnClickListener(v -> toChooseDate(REQUEST_START_DATE));
        end.setOnClickListener(v -> toChooseDate(REQUEST_END_DATE));
        span.addTextChangedListener(w);
    }

    @Override
    protected void processContent() {
        if (isLoop()) {
            String[] strs = mode.content.split("\\|");
            if (strs.length >= 4) {
                locMode = Integer.parseInt(strs[0]);
                sDate = Long.parseLong(strs[1]);
                spanTime = Integer.parseInt(strs[2]);
                eDate = Long.parseLong(strs[3]);
                maxInterval = mode.max_interval;
                minInterval = mode.min_interval;
                start.setText(TimeUtil.long2MinuteDate(sDate * 1000L));
                String time = String.valueOf(spanTime);
                span.setText(time);
                span.setSelection(time.length());
                end.setText(TimeUtil.long2MinuteDate(eDate * 1000L));
            }
        } else {
            locMode = DevMode.LOC_GPS;
            sDate = 0;
            spanTime = 100;
            eDate = 0;
            maxInterval = 7 * 24 * 60;
            minInterval = MIN_INTERVAL;
        }
        maxIntervalHint.setText(getString(R.string.max_interval_hint, maxInterval, minInterval));
    }

    private void toChooseDate(int requestCode) {
        Intent t = new Intent(getContext(), ChooseDateActivity.class);
        t.putExtra(ChooseTimeActivity.LOC_MODE, locMode);
        startActivityForResult(t, requestCode);
    }

    private boolean isLoop() {
        return mode != null && mode.mode == DevMode.MODE_SCHEDULE && mode.sub_mode == DevMode.SUB_MODE_LOOP;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_START_DATE:
                if (resultCode == Activity.RESULT_OK) {
                    setNewDate(requestCode, data);
                }
                break;
            case REQUEST_END_DATE:
                if (resultCode == Activity.RESULT_OK) {
                    setNewDate(requestCode, data);
                }
                break;
            default:
                break;
        }
    }

    private void setNewDate(int requestCode, Intent t) {
        if (t != null && t.getExtras() != null) {
            Bundle b = t.getExtras();
            Calendar c = (Calendar) b.getSerializable(NEW_DATE);
            String str = TimeUtil.long2MinuteDate(c.getTimeInMillis());
            if (requestCode == REQUEST_START_DATE) {
                sDate = c.getTimeInMillis() / 1000;
                start.setText(str);
            } else if (requestCode == REQUEST_END_DATE) {
                eDate = c.getTimeInMillis() / 1000;
                end.setText(str);
            }
            if (t.hasExtra(ChooseTimeActivity.LOC_MODE)) {
                locMode = t.getIntExtra(ChooseTimeActivity.LOC_MODE, DevMode.LOC_GPS);
            }
        }
    }

    private TextWatcher w = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String str = span.getText().toString().trim();
            if (str.length() > 1 && str.charAt(0) == '0') {
                span.setText(str.substring(1));
                span.setSelection(str.substring(1).length());
                return;
            }
            int t;
            if (TextUtils.isEmpty(str)) {
                t = 0;
            } else {
                t = Integer.parseInt(str);
            }
            if (t > maxInterval) {
                span.setText(String.valueOf(maxInterval));
                span.setSelection(span.getText().toString().length());
            }
        }
    };

    @Override
    protected boolean checkTime() {
        String str = span.getText().toString().trim();
        if (sDate == 0 || eDate == 0 || str.length() == 0) {
            showInvalidDlg();
            return false;
        }
        if (sDate >= eDate) {
            showToast(getString(R.string.end_more_than_start_error));
            return false;
        }
        int time = Integer.parseInt(str);
        if (time < minInterval) {
            showToast(getString(R.string.min_interval_hint, minInterval));
            return false;
        }
        if ((eDate - sDate) * 60 < time) {
            showToast(getString(R.string.spantime_less_than_alive_time));
            return false;
        }
        return true;
    }

    @Override
    protected String makeContent() {
        StringBuilder s = new StringBuilder();
        s.append(locMode).append("|");
        s.append(sDate).append("|");
        s.append(span.getText().toString().trim()).append("|");
        s.append(eDate);
        return URLEncoder.encode(s.toString());
    }
}
