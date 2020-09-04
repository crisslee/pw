package com.coomix.app.all.ui.devTime;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DevMode;
import com.coomix.app.all.widget.DevViewPager;
import com.coomix.app.all.ui.base.BaseActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/12/25.
 */
public class DevTimeActivity extends BaseActivity {
    public static final String DEV_MODE = "dev_mode";

    public static final int ITEM_ALARM = 0;
    public static final int ITEM_TRACK = 1;
    public static final int ITEM_WEEK = 2;
    public static final int ITEM_LOOP = 3;
    public static final int ITEM_CUSTOM = 4;

    @BindView(R.id.vp)
    ViewPager vp;
    @BindView(R.id.tab)
    TabLayout tab;
    DevMode mode;
    String[] titles = new String[] { "闹钟", "追踪", "星期", "重复", "自定义" };
    private List<Fragment> frgList = new ArrayList<>();
    private int[] selectedImgs = new int[] {
        R.drawable.mode_alarm_selected, R.drawable.mode_track_selected,
        R.drawable.mode_week_selected, R.drawable.mode_loop_selected, R.drawable.mode_custom_selected
    };
    private int[] normalImgs = new int[] {
        R.drawable.mode_alarm_normal, R.drawable.mode_track_normal,
        R.drawable.mode_week_normal, R.drawable.mode_loop_normal, R.drawable.mode_custom_normal
    };
    private int[] frgTitles = new int[] {
        R.string.title_mode_alarm, R.string.title_mode_track, R.string.title_mode_week, R.string.title_mode_loop,
        R.string.title_mode_custom
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_time);
        ButterKnife.bind(this);
        initIntentData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initIntentData() {
        Intent i = getIntent();
        if (i == null) {
            return;
        }
        Bundle b = i.getExtras();
        if (b == null) {
            return;
        }
        mode = (DevMode) b.getSerializable(DEV_MODE);
        //testData();
    }

    private void testData() {
        //闹钟
        mode.mode = DevMode.MODE_ALARM;
        //mode.content = "8:00, 12:00, 13:50";
        mode.content = "";
        //追踪
        //mode.mode = DevMode.MODE_TRACK;
        //mode.content = "999";
        //重复
        //mode.mode = DevMode.MODE_SCHEDULE;
        //mode.sub_mode = DevMode.SUB_MODE_LOOP;
        //mode.content = "1|1546831799|300|1548314945";
        //星期
        //mode.mode = DevMode.MODE_WEEK;
        //mode.content = "15, 12:00";
        //自定义
        //mode.mode = DevMode.MODE_SCHEDULE;
        //mode.sub_mode = DevMode.SUB_MODE_CUSTOM;
        //mode.content = "1#1547005275|2#1547955675";
    }

    private void initView() {
        AlarmFragment af = AlarmFragment.newInstance(mode);
        frgList.add(af);
        TrackFragment tf = TrackFragment.newInstance(mode);
        frgList.add(tf);
        WeekFragment wf = WeekFragment.newInstance(mode);
        frgList.add(wf);
        LoopFragment lf = LoopFragment.newInstance(mode);
        frgList.add(lf);
        CustomFragment cf = CustomFragment.newInstance(mode);
        frgList.add(cf);
        MyFragmentAdapter mf = new MyFragmentAdapter(getSupportFragmentManager());
        vp.setAdapter(mf);
        if (vp instanceof DevViewPager) {
            ((DevViewPager) vp).setNoScroll(true);
        }

        tab.setupWithViewPager(vp);
        tab.addOnTabSelectedListener(l);
        for (int i = 0; i < tab.getTabCount(); i++) {
            TabLayout.Tab t = tab.getTabAt(i);
            if (t != null) {
                t.setCustomView(mf.getCustomView(i));
            }
        }

        if (mode == null || mode.mode == DevMode.MODE_ALARM) {
            vp.setCurrentItem(ITEM_ALARM);
            setTabSelectedImg(ITEM_ALARM);
        } else if (mode.mode == DevMode.MODE_TRACK) {
            vp.setCurrentItem(ITEM_TRACK);
            setTabSelectedImg(ITEM_TRACK);
        } else if (mode.mode == DevMode.MODE_WEEK) {
            vp.setCurrentItem(ITEM_WEEK);
            setTabSelectedImg(ITEM_WEEK);
        } else if (mode.mode == DevMode.MODE_SCHEDULE) {
            if (mode.sub_mode == DevMode.SUB_MODE_LOOP) {
                vp.setCurrentItem(ITEM_LOOP);
                setTabSelectedImg(ITEM_LOOP);
            } else if (mode.sub_mode == DevMode.SUB_MODE_CUSTOM) {
                vp.setCurrentItem(ITEM_CUSTOM);
                setTabSelectedImg(ITEM_CUSTOM);
            }
        } else {
            vp.setCurrentItem(ITEM_ALARM);
            setTabSelectedImg(ITEM_ALARM);
        }
    }

    private void setTabSelectedImg(int position) {
        try {
            TabLayout.Tab t = tab.getTabAt(position);
            View v = t.getCustomView();
            ImageView icon = (ImageView) v.findViewById(R.id.icon);
            icon.setImageResource(selectedImgs[position]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TabLayout.OnTabSelectedListener l = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            vp.setCurrentItem(tab.getPosition());
            View v = tab.getCustomView();
            ImageView iv = (ImageView) v.findViewById(R.id.icon);
            iv.setImageResource(selectedImgs[tab.getPosition()]);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            View v = tab.getCustomView();
            ImageView iv = (ImageView) v.findViewById(R.id.icon);
            iv.setImageResource(normalImgs[tab.getPosition()]);
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    private class MyFragmentAdapter extends FragmentPagerAdapter {
        public MyFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return frgList.get(position);
        }

        @Override
        public int getCount() {
            return frgList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        public View getCustomView(int position) {
            View v = LayoutInflater.from(DevTimeActivity.this).inflate(R.layout.item_dev_time, null);
            ImageView icon = v.findViewById(R.id.icon);
            TextView title = v.findViewById(R.id.title);
            icon.setImageResource(normalImgs[position]);
            title.setText(getPageTitle(position));
            return v;
        }
    }

    @Override
    public void onBackPressed() {
        int position = vp.getCurrentItem();
        Fragment f = frgList.get(position);
        if (f instanceof DevTimeFragment) {
            ((DevTimeFragment) f).setMode(frgTitles[position]);
        }
    }
}