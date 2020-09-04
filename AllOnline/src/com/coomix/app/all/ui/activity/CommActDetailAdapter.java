package com.coomix.app.all.ui.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.bumptech.GlideApp;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.model.bean.CommunityActContent;
import com.coomix.app.all.model.bean.CommunityActDetail;
import com.coomix.app.all.webview.TextWebView;
import com.coomix.app.all.util.CommunityDateUtil;
import com.coomix.app.all.util.CommunityPictureUtil;
import java.util.ArrayList;

/**
 * @author shishenglong
 * @since 2016年8月18日 上午10:04:17
 */
public class CommActDetailAdapter extends BaseAdapter {
    private final String TAG = "CommActDetailAdapter";
    private Context mContext = null;
    private CommunityActDetail actDetail = null;
    private final int TEXT = 1;
    private final int IMAGE = 2;
    private ArrayList<TextWebView> listWebView;
    private ArrayList<LinearLayout> listParent;
    private View viewParent;

    public CommActDetailAdapter(Context context, CommunityActDetail actDetail, View viewParent) {
        this.mContext = context;
        this.actDetail = actDetail;
        this.viewParent = viewParent;
    }

    public void setData(CommunityActDetail actDetail) {
        this.actDetail = actDetail;
    }

    @Override
    public int getCount() {
        if (actDetail == null) {
            return 0;
        }
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return actDetail;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            convertView = mergeActDetailView(convertView);
        }

        return convertView;
    }

    private View mergeActDetailView(View convertView) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.comm_act_detail_layout, null);
            viewHolder.imageTitle = (ImageView) convertView.findViewById(R.id.imageViewTitle);
            viewHolder.textTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
            viewHolder.textTime = (TextView) convertView.findViewById(R.id.textViewTime);
            viewHolder.textLocation = (TextView) convertView.findViewById(R.id.textViewLocation);
            viewHolder.textLimit = (TextView) convertView.findViewById(R.id.textViewLimit);
            viewHolder.textCost = (TextView) convertView.findViewById(R.id.textViewCost);
            viewHolder.imageTitle.setOnLongClickListener(imageLongClickListener);

            // 活动介绍中的图片，以及文字展示UI
            LinearLayout layoutContent = (LinearLayout) convertView.findViewById(R.id.layoutContent);
            if (actDetail.getActDetailContent() == null) {
                return convertView;
            }
            ArrayList<CommunityActContent> listContent = actDetail.getActDetailContent().getList();
            viewHolder.contentImageViewList = new ArrayList<ImageView>();
            viewHolder.contentWebViewList = new ArrayList<TextWebView>();
            viewHolder.contentParentList = new ArrayList<LinearLayout>();
            boolean bTextShow = false;
            for (int i = 0; i < listContent.size(); i++) {
                CommunityActContent actContent = listContent.get(i);
                if (actContent != null) {
                    int type = actContent.getType();
                    if (type == TEXT) {
                        if (bTextShow) {
                            layoutContent.addView(getGrayLineView());
                        }
                        TextWebView webView = new TextWebView(mContext);
                        LayoutParams params =
                            new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                        params.gravity = Gravity.CENTER_HORIZONTAL;
                        webView.setLayoutParams(params);
                        //						 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        //							 webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        //						 }
                        layoutContent.addView(webView);
                        viewHolder.contentWebViewList.add(webView);
                        viewHolder.contentParentList.add(layoutContent);
                        bTextShow = true;
                    } else if (type == IMAGE) {
                        if (bTextShow) {
                            bTextShow = false;
                        }
                        ImageView imageView = new ImageView(mContext);
                        imageView.setScaleType(ScaleType.CENTER_CROP);
                        imageView.setOnLongClickListener(imageLongClickListener);
                        LayoutParams params =
                            new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                        params.topMargin = AllOnlineApp.getDensity() * 15 / 480;
                        params.bottomMargin = AllOnlineApp.getDensity() * 15 / 480;
                        params.gravity = Gravity.CENTER_HORIZONTAL;
                        imageView.setLayoutParams(params);
                        imageView.setMinimumWidth(AllOnlineApp.screenWidth - 2 * mContext.getResources()
                            .getDimensionPixelSize(R.dimen.space_2x));
                        imageView.setMinimumHeight(
                            mContext.getResources().getDimensionPixelSize(R.dimen.comm_act_detail_image_height));
                        layoutContent.addView(imageView);
                        viewHolder.contentImageViewList.add(imageView);
                    }
                }
            }

            listWebView = viewHolder.contentWebViewList;
            listParent = viewHolder.contentParentList;
            convertView.setTag(R.layout.comm_act_detail_layout, viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag(R.layout.comm_act_detail_layout);
        }

        refreshData(actDetail, viewHolder);

        return convertView;
    }

    private View getGrayLineView() {
        View grayLine = new View(mContext);
        grayLine.setBackgroundColor(mContext.getResources().getColor(R.color.register_grid_color));
        LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
            mContext.getResources().getDimensionPixelSize(R.dimen.comm_act_line_height));
        params.topMargin = AllOnlineApp.getDensity() * 10 / 480;
        params.bottomMargin = AllOnlineApp.getDensity() * 10 / 480;
        grayLine.setLayoutParams(params);
        return grayLine;
    }

    private void refreshData(CommunityActDetail actDetail, final ViewHolder viewHolder) {
        if (actDetail == null) {
            return;
        }
        viewHolder.imageTitle.setTag(R.id.imageViewTitle, actDetail.getPic());
        //		GlideApp.with(mContext).load(actDetail.getPic()).placeholder(R.drawable.image_default)
        //				.error(R.drawable.image_default_error).centerCrop().into(viewHolder.imageTitle);
        GlideApp.with(mContext).load(actDetail.getPic()).placeholder(R.drawable.image_default)
            .error(R.drawable.image_default_error).centerCrop()
            .override(AllOnlineApp.screenWidth,
                mContext.getResources().getDimensionPixelSize(R.dimen.comm_act_detail_title_height))
            .into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (resource != null) {
                        viewHolder.imageTitle.setImageDrawable(resource);
                    }
                }
            });
        viewHolder.textTitle.setText(actDetail.getTitle());
        if (CommunityDateUtil.isSameDay(actDetail.getBegtime() * 1000, actDetail.getEndtime() * 1000)) {
            // 当天结束的活动，只显示开始时间
            viewHolder.textTime.setText(
                CommunityDateUtil.formatCommunityActDateTime(mContext, actDetail.getBegtime(), true));
        } else {
            viewHolder.textTime.setText(
                CommunityDateUtil.formatCommunityActDateTime(mContext, actDetail.getBegtime(), true) + " - "
                    + CommunityDateUtil.formatCommunityActDateTime(mContext, actDetail.getEndtime(), true));
        }

        viewHolder.textLocation.setText(actDetail.getLocation());

        if (actDetail.getMaxnum() <= 0) {
            if (actDetail.getApplynum() > 0) {
                String apply =
                    String.format(mContext.getString(R.string.activity_register_apply), actDetail.getApplynum());
                viewHolder.textLimit.setText(Html.fromHtml(apply + "/" + "<font color=\"#999999\">"
                    + mContext.getString(R.string.activity_no_limit) + "</font>"));
            } else {
                viewHolder.textLimit.setText(mContext.getString(R.string.activity_no_limit));
            }
        } else if (actDetail.getApplynum() > 0 && actDetail.getApplynum() <= actDetail.getMaxnum()) {
            String apply = String.format(mContext.getString(R.string.activity_register_apply), actDetail.getApplynum());
            String max = String.format(mContext.getString(R.string.activity_register_max), actDetail.getMaxnum());
            viewHolder.textLimit.setText(Html.fromHtml(apply + "/" + "<font color=\"#999999\">" + max + "</font>"));
        } else if (actDetail.getApplynum() == 0) {
            String mString = String.format(mContext.getString(R.string.activity_register_max), actDetail.getMaxnum());
            viewHolder.textLimit.setText(Html.fromHtml("<font color=\"#999999\">" + mString + "</font>"));
        } else {
            String fileMethodLine = "";
            fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
            fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
            fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
            GoomeLog.getInstance()
                .logE(fileMethodLine,
                    "registered num:" + actDetail.getApplynum() + " . max limit num:" + actDetail.getMaxnum(), -1);
            if (actDetail.getApplynum() < 0) {
                actDetail.setApplynum(0);
                viewHolder.textLimit.setText(mContext.getString(R.string.activity_no_limit));
            } else if (actDetail.getApplynum() > actDetail.getMaxnum()) {
                actDetail.setApplynum(actDetail.getMaxnum());
                String apply =
                    String.format(mContext.getString(R.string.activity_register_apply), actDetail.getApplynum());
                viewHolder.textLimit.setText(apply);
            }
        }

        // 费用显示
        String cost = actDetail.getCost();
        if (TextUtils.isEmpty(cost) || cost.equals("0")) {
            viewHolder.textCost.setText(R.string.free);
        } else {
            viewHolder.textCost.setText(cost);
        }

        if (actDetail.getActDetailContent() == null) {
            return;
        }
        ArrayList<CommunityActContent> listContent = actDetail.getActDetailContent().getList();
        int iImageNum = 0;
        int iTextNum = 0;
        for (int i = 0; i < listContent.size(); i++) {
            CommunityActContent actContent = listContent.get(i);
            if (actContent != null) {
                int type = actContent.getType();
                if (type == TEXT) {
                    if (iTextNum < viewHolder.contentWebViewList.size()) {
                        String value = actContent.getValue();
                        if (!TextUtils.isEmpty(value)) {
                            viewHolder.contentWebViewList.get(iTextNum).loadData(value);
                        }
                        iTextNum++;
                    }
                } else if (type == IMAGE) {
                    if (iImageNum < viewHolder.contentImageViewList.size()) {
                        final ImageView imageView = viewHolder.contentImageViewList.get(iImageNum);
                        /**
                         * 因为Glide内部有用到setTag，避免报错冲突，需加id以避免 The key must be an
                         * application-specific resource id.
                         */
                        imageView.setTag(R.id.imageViewTitle, actContent.getValue());
                        GlideApp.with(mContext).load(actContent.getValue()).placeholder(R.drawable.image_default)
                            .error(R.drawable.image_default_error).centerCrop()//.into(imageView);
                            //							 	.override(AllOnlineApp.screenWidth - 2 * mContext.getResources().getDimensionPixelSize(R.dimen.space_2x),
                            //										mContext.getResources().getDimensionPixelSize(R.dimen.comm_act_detail_image_height))
                            .into(new SimpleTarget<Drawable>() {
                                @Override
                                public void onResourceReady(Drawable resource,
                                    Transition<? super Drawable> transition) {
                                    if (resource != null) {
                                        imageView.setImageDrawable(resource);
                                    }
                                }
                            });
                        iImageNum++;
                    }
                }
            }
        }
    }

    private class ViewHolder {
        ImageView imageTitle;
        TextView textTitle;
        TextView textTime;
        TextView textLocation;
        TextView textLimit;
        TextView textCost;
        ArrayList<ImageView> contentImageViewList;
        ArrayList<TextWebView> contentWebViewList;
        ArrayList<LinearLayout> contentParentList;
    }

    private OnLongClickListener imageLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            // TODO Auto-generated method stub
            if (v != null && viewParent != null) {
                String picUrl = (String) v.getTag(R.id.imageViewTitle);
                if (!TextUtils.isEmpty(picUrl)) {
                    CommunityPictureUtil.showMorePopWindow(mContext, viewParent, picUrl);
                    return true;
                }
            }
            return false;
        }
    };

    public void destyoy() {
        //		if(listParent != null)
        //		{
        //			for(int i = 0; i < listParent.size();i++)
        //			{
        //				LinearLayout parent = listParent.get(i);
        //				if(parent != null)
        //				{
        //					parent.removeAllViews();
        //				}
        //			}
        //		}
        if (listWebView != null) {
            for (int i = 0; i < listWebView.size(); i++) {
                TextWebView webView = listWebView.get(i);
                if (webView != null) {
                    webView.destroy();
                }
            }
        }
    }
}
