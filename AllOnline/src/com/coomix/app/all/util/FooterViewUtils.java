package com.coomix.app.all.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.coomix.app.all.R;

/**
 * listview，footerview工具（主要用于社区页面无网络，无粉丝，无关注等提示）
 *
 * @author 刘生健
 * @since 2016-4-8 下午04:46:22
 */
public class FooterViewUtils {
    /** mview的footerView,用户列表为空提示 */
    private View mEmptyFooterLayout;
    /** 用户列表为空提示图片 */
    private ImageView mEmptyFooterIv;
    /** 用户列表为空提示文字 */
    private TextView mEmptyFooterTv;

    private Context ctx;
    private ListView list;

    public FooterViewUtils(Context ctx, ListView list) {
        this.ctx = ctx;
        this.list = list;
        // init();
    }

    private void init() {
        View footerView = LayoutInflater.from(ctx).inflate(R.layout.footer_user_list_empty, null);
        mEmptyFooterLayout = footerView.findViewById(R.id.footer_user_list_empty_layout);
        mEmptyFooterIv = (ImageView) footerView.findViewById(R.id.footer_user_list_empty_iv);
        mEmptyFooterTv = (TextView) footerView.findViewById(R.id.footer_user_list_empty_tv);
        list.addFooterView(footerView, null, false);
        list.setFooterDividersEnabled(false);
    }

    /**
     * {@link #showEmptyView(int, int, OnClickListener, int...)}
     * OnClickListener为null
     */
    public void showEmptyView(int drawableId, int stringId, int... tag) {
        showEmptyView(drawableId, stringId, null, tag);
    }

    /**
     * @param onClickListener 点击事件，不要监听时传null
     * @param tag 显示的类型（如网络错误，没有粉丝等）
     */
    public void showEmptyView(int drawableId, int stringId, OnClickListener onClickListener, int... tag) {
        if (mEmptyFooterLayout == null) {
            init();
        }
        if (tag != null && tag.length > 0) {
            this.tag = tag[0];
        } else {
            this.tag = 0;
        }
        mEmptyFooterLayout.setVisibility(View.VISIBLE);
        // mEmptyFooterLayout.setPadding(0, 0, 0, 0);
        if (drawableId == -1) {
            mEmptyFooterIv.setImageDrawable(null);
            mEmptyFooterIv.setVisibility(View.GONE);
        } else {
            mEmptyFooterIv.setImageResource(drawableId);
            mEmptyFooterIv.setVisibility(View.VISIBLE);
        }
        mEmptyFooterTv.setText(stringId);
        mEmptyFooterLayout.setOnClickListener(onClickListener);
    }

    public boolean isShowing() {
        return mEmptyFooterLayout != null && mEmptyFooterLayout.getVisibility() == View.VISIBLE;
    }

    public void dismiss() {
        if (mEmptyFooterLayout != null) {
            mEmptyFooterLayout.setVisibility(View.GONE);
            // mEmptyFooterLayout.setPadding(0, -mEmptyFooterLayout.getHeight(), 0, 0);
        }
        this.tag = 0;
    }

    /**
     * 显示的类型（如网络错误，没有粉丝等）
     */
    private int tag;

    public int getTag() {
        return tag;
    }

    public static View getNoMoreDataFooterView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.footer_no_more_data, null);
    }
}
