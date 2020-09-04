package com.coomix.app.all.ui.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.GlideApp;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.CommunityAct;
import com.coomix.app.all.util.CommunityDateUtil;

import java.util.ArrayList;

/**
 * Created by ly on 2017/8/25.
 */

public class CommActListAdapter extends BaseAdapter {
    private ArrayList<CommunityAct> listActs = new ArrayList<CommunityAct>();
    private Context mContext;
    private final int[] color = {R.color.act_register_flag_color1, R.color.act_register_flag_color2,
            R.color.act_register_flag_color3, R.color.act_register_flag_color4, R.color.act_register_flag_color5,
            R.color.act_register_flag_color6, R.color.act_register_flag_color7};

    public CommActListAdapter(Context context, ArrayList<CommunityAct> listActs) {
        this.listActs = listActs;
        mContext = context;
    }

    @Override
    public int getCount() {
        return listActs.size();
    }

    @Override
    public Object getItem(int position) {
        return listActs.get(position);
        //return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.native_activity_main_list_layout, null);
            viewHolder.imageMain = (ImageView) convertView.findViewById(R.id.imageViewMain);
            viewHolder.imageStatus = (ImageView) convertView.findViewById(R.id.imageViewStatus);
            viewHolder.imageColor = (ImageView) convertView.findViewById(R.id.imageViewColor);
            viewHolder.imageFull = (ImageView) convertView.findViewById(R.id.imageViewFull);
            viewHolder.textTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
            viewHolder.textInfo = (TextView) convertView.findViewById(R.id.textViewInfo);
            convertView.setTag(R.layout.native_activity_main_list_layout, viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag(R.layout.native_activity_main_list_layout);
        }

        setAdapterData(viewHolder, position);

        return convertView;
    }

    private class ViewHolder {
        ImageView imageMain;
        ImageView imageStatus;
        ImageView imageColor;
        ImageView imageFull;
        TextView textTitle;
        TextView textInfo;
    }

    private void setAdapterData(final ViewHolder viewHolder, int position) {
        CommunityAct commAct = null;
        if (position < listActs.size()) {
            commAct = listActs.get(position);
        }
        if (viewHolder == null || commAct == null) {
            return;
        }

        int iImageStatusResId = R.color.transparent;
        if (TextUtils.isEmpty(commAct.getCost()) || commAct.getCost().equals("0")) {
            iImageStatusResId = R.drawable.cost_free;
        }
        switch (commAct.getStatus()) {
            case CommunityAct.ACT_FINISHED:// 已结束
                iImageStatusResId = R.drawable.register_finish;
                break;

            case CommunityAct.ACT_STOPPED:// 已经截止
                // iImageStatusResId = R.drawable.register_stop;
                // iImageStatusResId = R.color.transparent;
                break;

            case CommunityAct.ACT_FULL:// 已经满员
                // iImageStatusResId = R.color.transparent;
                break;

            case CommunityAct.ACT_SIGNING:// 报名中
            default:
                // iImageStatusResId = R.drawable.registering;
                // iImageStatusResId = R.color.transparent;
                break;
        }

        if (commAct.getStatus() == CommunityAct.ACT_FULL) {
            viewHolder.imageFull.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imageFull.setVisibility(View.GONE);
        }

        viewHolder.imageStatus.setImageResource(iImageStatusResId);
        if (commAct.getStatus() == CommunityAct.ACT_FINISHED) {
            viewHolder.imageColor.setImageResource(R.color.act_register_flag_color_gray);
        } else {
            viewHolder.imageColor.setImageResource(color[position % color.length]);
        }

        GlideApp.with(mContext)
                .load(commAct.getPic())
                .thumbnail(0.1f)
                .error(R.drawable.image_default_error)
                .placeholder(R.drawable.image_default)
                .centerCrop()
                .override(AllOnlineApp.screenWidth, mContext.getResources().getDimensionPixelSize(R.dimen.dimen_200dp))
                .into(viewHolder.imageMain);

//
        viewHolder.textTitle.setText(commAct.getTitle());
        if (CommunityDateUtil.isSameDay(commAct.getBegtime() * 1000, commAct.getEndtime() * 1000)) {
            // 一天的活动，时间只显示一个当天日期
            viewHolder.textInfo.setText(
                    CommunityDateUtil.formatCommunityActDateTime(mContext, commAct.getBegtime(), false)
                            + "\n" + commAct.getLocation());
        } else {
            viewHolder.textInfo.setText(
                    CommunityDateUtil.formatCommunityActDateTime(mContext, commAct.getBegtime(), false)
                            + " - " + CommunityDateUtil.formatCommunityActDateTime(mContext,
                            commAct.getEndtime(), false)
                            + "\n" + commAct.getLocation());
        }
    }

}
