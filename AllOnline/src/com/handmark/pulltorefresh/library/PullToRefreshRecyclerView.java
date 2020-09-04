package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.coomix.app.all.R;

public class PullToRefreshRecyclerView extends PullToRefreshBase<RecyclerView> {

    public PullToRefreshRecyclerView(Context context) {
        super(context);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected RecyclerView createRefreshableView(Context context, AttributeSet attrs) {
        RecyclerView recyclerView;
        recyclerView = new RecyclerView(context, attrs);
        recyclerView.setId(R.id.recyclerview);
        return recyclerView;
    }

    @Override
    protected boolean isReadyForPullStart() {
        if (mRefreshableView.getChildCount() <= 0) {
            return true;
        }
        int firstVisiblePosition = mRefreshableView.getChildAdapterPosition(mRefreshableView.getChildAt(0));
        int top = mRefreshableView.getChildAt(0).getTop();
        if (firstVisiblePosition == 0 && top >= 0) {
            // int top = mRefreshableView.getChildAt(0).getTop();
            // return top == 27;
            return true;
        } else {
            return false;
        }

    }

    @Override
    protected boolean isReadyForPullEnd() {
        if (mRefreshableView == null) {
            return false;
        }
        Adapter<?> adapter = mRefreshableView.getAdapter();
        if (adapter == null) {
            return false;
        }
        if (adapter.getItemCount() <= 0) {
            return false;
        }
        int lastVisiblePosition = mRefreshableView
                .getChildPosition(mRefreshableView.getChildAt(mRefreshableView.getChildCount() - 1));
        if (lastVisiblePosition >= mRefreshableView.getAdapter().getItemCount() - 1) {
            return mRefreshableView.getChildAt(mRefreshableView.getChildCount() - 1).getBottom() <= mRefreshableView
                    .getBottom();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        RecyclerView.Adapter<?> adapter = getRefreshableView().getAdapter();
        if (adapter == null || adapter.getItemCount() <= 0) {
            return false;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        RecyclerView.Adapter<?> adapter = getRefreshableView().getAdapter();
        if (adapter == null || adapter.getItemCount() <= 0) {
            return false;
        }
        return super.onTouchEvent(event);
    }

}
