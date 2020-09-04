package com.coomix.app.all.ui.devTime;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DevMode;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.TimeUtil;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/12/27.
 */
public class WeekFragment extends DevTimeFragment implements View.OnClickListener {
    private final int REQUEST_CHOOSE_TIME = 0;
    @BindView(R.id.topbar)
    MyActionbar topbar;
    @BindView(R.id.chooseTime)
    RelativeLayout rlTime;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.itemMon)
    TextView itemMon;
    @BindView(R.id.itemTue)
    TextView itemTue;
    @BindView(R.id.itemWed)
    TextView itemWed;
    @BindView(R.id.itemThu)
    TextView itemThu;
    @BindView(R.id.itemFri)
    TextView itemFri;
    @BindView(R.id.itemSat)
    TextView itemSat;
    @BindView(R.id.itemSun)
    TextView itemSun;
    private int[] week = new int[7];
    private TextView[] views;
    private Calendar curCal = Calendar.getInstance();

    public WeekFragment() {
    }

    public static WeekFragment newInstance(DevMode mode) {
        WeekFragment f = new WeekFragment();
        f.mode = mode;
        f.currMode = DevMode.MODE_WEEK;
        return f;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_week;
    }

    @Override
    protected void initView() {
        topbar.setTitle(R.string.title_mode_week);
        topbar.setLeftImageResource(R.drawable.btn_back);
        topbar.setLeftImageClickListener(v -> setMode(R.string.title_mode_week));
        topbar.setLeftText(R.string.senior);
        topbar.setLeftTextCLickListener(v1 -> setMode(R.string.title_mode_week));

        views = new TextView[] { itemMon, itemTue, itemWed, itemThu, itemFri, itemSat, itemSun };
        rlTime.setOnClickListener(this);
        itemMon.setOnClickListener(this);
        itemTue.setOnClickListener(this);
        itemWed.setOnClickListener(this);
        itemThu.setOnClickListener(this);
        itemFri.setOnClickListener(this);
        itemSat.setOnClickListener(this);
        itemSun.setOnClickListener(this);
    }

    @Override
    protected void processContent() {
        if (isWeekMode()) {
            String[] strs = mode.content.split(",");
            if (strs.length > 0) {
                for (int i = 0; i < strs[0].length(); i++) {
                    int k = strs[0].charAt(i) - '0';
                    week[k - 1] = 1;
                    setDrawableRight(views[k - 1], getResources().getDrawable(R.drawable.loc_mode_checked));
                }
            }
            if (strs.length > 1) {
                int index = strs[1].indexOf(":");
                if (index < 0) {
                    return;
                }
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strs[1].substring(0, index).trim()));
                c.set(Calendar.MINUTE, Integer.parseInt(strs[1].substring(index + 1).trim()));
                curCal = c;
                String s = TimeUtil.long212Hour(c.getTimeInMillis());
                s.replace("AM", "上午").replace("PM", "下午");
                time.setText(s);
            }
        } else {
            curCal.set(Calendar.HOUR_OF_DAY, 0);
            curCal.set(Calendar.MINUTE, 0);
            String s = TimeUtil.long212Hour(curCal.getTimeInMillis());
            s.replace("AM", "上午").replace("PM", "下午");
            time.setText(s);
        }
    }

    private boolean isWeekMode() {
        return mode != null && mode.mode == DevMode.MODE_WEEK;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chooseTime:
                toChooseTime();
                break;
            case R.id.itemMon:
                clickText(0, (TextView) v);
                break;
            case R.id.itemTue:
                clickText(1, (TextView) v);
                break;
            case R.id.itemWed:
                clickText(2, (TextView) v);
                break;
            case R.id.itemThu:
                clickText(3, (TextView) v);
                break;
            case R.id.itemFri:
                clickText(4, (TextView) v);
                break;
            case R.id.itemSat:
                clickText(5, (TextView) v);
                break;
            case R.id.itemSun:
                clickText(6, (TextView) v);
                break;
            default:
                break;
        }
    }

    private void toChooseTime() {
        Intent t = new Intent(getContext(), ChooseTimeActivity.class);
        t.putExtra(ChooseTimeActivity.SHOW_LOC_MODE, false);
        startActivityForResult(t, REQUEST_CHOOSE_TIME);
    }

    private void clickText(int index, TextView v) {
        Drawable d = getResources().getDrawable(R.drawable.loc_mode_checked);
        if (isSelected(v)) {
            setDrawableRight(v, null);
            week[index] = 0;
        } else {
            setDrawableRight(v, d);
            week[index] = 1;
        }
    }

    private boolean isSelected(View v) {
        if (v instanceof TextView) {
            Drawable[] d = ((TextView) v).getCompoundDrawables();
            return d[2] != null;
        } else {
            return false;
        }
    }

    private void setDrawableRight(TextView t, Drawable d) {
        if (d != null) {
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        }
        t.setCompoundDrawables(null, null, d, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_TIME && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                int day = data.getIntExtra(ChooseTimeActivity.AMPM, 0);
                int hour = data.getIntExtra(ChooseTimeActivity.HOUR, 0);
                int minute = data.getIntExtra(ChooseTimeActivity.MINUTE, 0);
                Calendar c = Calendar.getInstance();
                c.set(Calendar.AM_PM, day);
                c.set(Calendar.HOUR, hour);
                c.set(Calendar.MINUTE, minute);
                curCal = c;
                String s = TimeUtil.long212Hour(c.getTimeInMillis());
                s.replace("AM", "上午").replace("PM", "下午");
                time.setText(s);
            }
        }
    }

    @Override
    protected boolean checkTime() {
        for (int i = 0; i < 7; i++) {
            if (week[i] == 1) {
                return true;
            }
        }
        showInvalidDlg();
        return false;
    }

    @Override
    protected String makeContent() {
        StringBuilder s = new StringBuilder();
        s.append("1,");
        for (int i = 0; i < 7; i++) {
            if (week[i] == 1) {
                s.append(i + 1);
            }
        }
        if (TextUtils.isEmpty(s.toString())) {
            return "";
        }
        s.append(",");
        DateFormat df = new SimpleDateFormat("HHmm");
        s.append(df.format(curCal.getTimeInMillis()));
        return URLEncoder.encode(s.toString());
    }
}
