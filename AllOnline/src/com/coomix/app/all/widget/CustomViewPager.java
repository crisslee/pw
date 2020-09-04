package com.coomix.app.all.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // @Override
    // public void setCurrentItem(int item, boolean smoothScroll) {
    // LogUtil.e("item="+item);
    // long start = System.currentTimeMillis();
    // PagerAdapter adapter = getAdapter();
    // if(adapter != null && adapter instanceof ImagePagerAdapter){
    // ImagePagerAdapter imagerAdapter = (ImagePagerAdapter) adapter;
    // int totalCount = adapter.getCount();
    // if(item >= totalCount){//到最大值
    // item = item % imagerAdapter.getReallyViewsCount() +
    // imagerAdapter.getDefaultItem();
    // }
    // }
    // LogUtil.e("consumeTime="+(System.currentTimeMillis() - start));
    // super.setCurrentItem(item, smoothScroll);
    // }

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        switch (evt.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // // 记录按下时候的坐标
                // downPoint.x = evt.getX();
                // downPoint.y = evt.getY();
                if (this.getChildCount() > 1) { // 有内容，多于1个时
                    // 通知其父控件，现在进行的是本控件的操作，不允许拦截
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (this.getChildCount() > 1) { // 有内容，多于1个时
                    // 通知其父控件，现在进行的是本控件的操作，不允许拦截
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                /*
                 * // 在up时判断是否按下和松手的坐标为一个点 if (PointF.length(evt.getX() -
                 * downPoint.x, evt.getY() - downPoint.y) < (float) 5.0) {
                 * onSingleTouch(this); return true; }
                 */
                break;
        }
        return super.onTouchEvent(evt);
    }

    /*
     * PointF downPoint = new PointF(); OnSingleTouchListener
     * onSingleTouchListener;
     *
     * public void onSingleTouch(View v) { if (onSingleTouchListener != null) {
     * onSingleTouchListener.onSingleTouch(v); } }
     *
     * public interface OnSingleTouchListener { public void onSingleTouch(View
     * v); }
     *
     * public void setOnSingleTouchListener( OnSingleTouchListener
     * onSingleTouchListener) { this.onSingleTouchListener =
     * onSingleTouchListener; }
     */
}