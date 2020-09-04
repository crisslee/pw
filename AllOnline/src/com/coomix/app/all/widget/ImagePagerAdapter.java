package com.coomix.app.all.widget;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import java.util.ArrayList;

public class ImagePagerAdapter extends PagerAdapter {
    private ArrayList<View> layoutViews;

    public ImagePagerAdapter(ArrayList<View> layoutViews) {
        this.layoutViews = layoutViews;
    }

    @Override
    public int getCount() {
        // return layoutViews.length;
        // 设置成较大的数(1000)，使用户看不到边界
        if (layoutViews != null && layoutViews.size() == 1) {
            return 1;
        } else {
            return 1000;
        }
    }

    public int getReallyViewsCount() {
        if (layoutViews != null) {
            return layoutViews.size();
        } else {
            return 0;
        }
    }

    // 默认在中间附近,初始显示第一个
    public int getDefaultItem() {
        if (layoutViews != null && layoutViews.size() == 1) {
            return 1;
        } else {
            int mid = this.getCount() / 2;
            if (layoutViews != null && layoutViews.size() > 0) {
                return mid - mid % layoutViews.size();
            } else {
                return mid;
            }
        }
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object arg2) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // 对ViewPager页号求模取出View列表中要显示的项
        position %= layoutViews.size();
        if (position < 0) {
            position = layoutViews.size() + position;
        }
        View view = layoutViews.get(position);
        // 如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
        ViewParent vp = view.getParent();
        if (vp != null) {
            ViewGroup parent = (ViewGroup) vp;
            parent.removeView(view);
        }
        container.addView(view);
        // add listeners here if necessary
        return view;
    }
}
