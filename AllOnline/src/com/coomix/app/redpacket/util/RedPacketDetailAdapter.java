package com.coomix.app.redpacket.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.GlideApp;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.widget.ImagePagerAdapter;
import com.coomix.app.all.widget.VRoundImageView;
import com.coomix.app.redpacket.activity.RedPacketMapActivity;
import com.viewpagerindicator.LoopCirclePageIndicator;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ssl on 2017/4/7.
 */
public class RedPacketDetailAdapter extends BaseAdapter {
    private RedPacketInfo redPacketInfo;
    private Context mContext;
    private Timer timer;
    private int iImageViewSize = 0;
    private ViewPager viewPager = null;

    private static final float AD_HEIGHT_RATIO = 3f / 10f;
    private static final int AD_AUTO_PLAY_PERIO = 5000;

    public RedPacketDetailAdapter(Context context, RedPacketInfo redPacketInfo) {
        this.redPacketInfo = redPacketInfo;
        this.mContext = context;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (redPacketInfo != null) {
            return redPacketInfo.getAlloc_infos().size() + 1;
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        ArrayList<AllocInfo> listAllocInfos = redPacketInfo.getAlloc_infos();
        if (listAllocInfos != null && position > 0 && position < listAllocInfos.size() + 1) {
            position -= 1;
            return listAllocInfos.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            convertView = createTopView(position, convertView, parent);
        } else {
            convertView = createItemView(position, convertView, parent);
        }
        return convertView;
    }

    public View createTopView(int position, View convertView, ViewGroup parent) {
        ViewTop viewTop = null;
        if (convertView == null || convertView.getTag(R.layout.redpacket_detail_top) == null) {
            viewTop = new ViewTop();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.redpacket_detail_top, null);
            viewTop.imageUserIcon = (VRoundImageView) convertView.findViewById(R.id.imageViewUserIcon);
            viewTop.textUserName = (TextView) convertView.findViewById(R.id.textViewUserName);
            viewTop.textRPTitle = (TextView) convertView.findViewById(R.id.textViewRPTitle);
            viewTop.textRpLocation = (TextView) convertView.findViewById(R.id.txt_rp_location);
            viewTop.textRPType = (TextView) convertView.findViewById(R.id.textViewRPType);
            viewTop.textRPAmount = (TextView) convertView.findViewById(R.id.textViewRPAmount);
            viewTop.textRPToast = (TextView) convertView.findViewById(R.id.textViewRPToast);
            viewTop.textRPCountStatus = (TextView) convertView.findViewById(R.id.textViewRPCount);
            viewTop.textYuan = (TextView) convertView.findViewById(R.id.textViewYuan);
            viewTop.viewPager = (ViewPager) convertView.findViewById(R.id.viewPagerRedPacket);
            viewTop.pageIndicator = (LoopCirclePageIndicator) convertView.findViewById(R.id.pager_indicator);
            viewTop.slideLayout = (RelativeLayout) convertView.findViewById(R.id.imageLayout);
            viewTop.slideLayout.setLayoutParams(new LinearLayout.LayoutParams(AllOnlineApp.screenWidth,
                (int) (AD_HEIGHT_RATIO * AllOnlineApp.screenWidth)));
            convertView.setTag(R.layout.redpacket_detail_top, viewTop);
        } else {
            viewTop = (ViewTop) convertView.getTag(R.layout.redpacket_detail_top);
        }

        if (redPacketInfo == null) {
            return convertView;
        }

        //红包抽奖
        setViewPagerData(viewTop.slideLayout, viewTop.viewPager, viewTop.pageIndicator);

        //set data
        viewTop.imageUserIcon.setUserData(redPacketInfo.getImg(),
            mContext.getResources().getDimensionPixelSize(R.dimen.space_14x),
            redPacketInfo.getVtype(), mContext.getResources().getDimensionPixelSize(R.dimen.v_big_size));

        if (redPacketInfo.getPacket_type() == RedPacketConstant.REDPACKET_TYPE_DAILY) {
            viewTop.textUserName.setText(redPacketInfo.getName());
        } else {
            viewTop.textUserName.setText(mContext.getString(R.string.whose_redpacket, redPacketInfo.getName()));
        }

        if (!TextUtils.isEmpty(redPacketInfo.getHello_words())) {
            viewTop.textRPTitle.setText(redPacketInfo.getHello_words());
        } else {
            viewTop.textRPTitle.setText("");
        }
        if (redPacketInfo.isCommunityRedpacket() && redPacketInfo.getAlloc_range() > 0) {
            viewTop.textRpLocation.setVisibility(View.VISIBLE);
            RedPacketExtendInfo extendInfo = redPacketInfo.getExtend_item();
            if (extendInfo != null) {
                String locName = extendInfo.getLoc_name();
                if (TextUtils.isEmpty(locName)) {
                    locName = mContext.getString(R.string.redpacket_def_loc_name);
                }
                String range = RpLocationUtil.getRangeString(mContext, redPacketInfo.getAlloc_range());
                viewTop.textRpLocation.setText(mContext.getString(R.string.redpacket_detail_location, locName, range));
            }
            viewTop.textRpLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, RedPacketMapActivity.class);
                    intent.putExtra(RedPacketConstant.REDPACKET_DATA, redPacketInfo);
                    mContext.startActivity(intent);
                }
            });
        } else {
            viewTop.textRpLocation.setVisibility(View.GONE);
        }
        if (redPacketInfo.getPacket_type() == RedPacketConstant.REDPACKET_TYPE_RANDOM) {
            viewTop.textRPType.setVisibility(View.VISIBLE);
            viewTop.textRPType.setText(R.string.grab);
        } else {
            viewTop.textRPType.setVisibility(View.GONE);
        }

        boolean bMyAllcoed = false;
        if (redPacketInfo.getAllocated() == 1) {
            AllocInfo myAllocInfo = redPacketInfo.getMy_alloc_info();
            if (myAllocInfo != null) {
                viewTop.textRPAmount.setTypeface((Typeface.createFromAsset(mContext.getAssets(), "goome.ttf")));
                viewTop.textRPAmount.setText(CommunityUtil.getDecimalStrByInt(myAllocInfo.getShares(), 2));
                viewTop.textYuan.setVisibility(View.VISIBLE);
                viewTop.textRPToast.setText(mContext.getString(R.string.redpacket_to_pocket));
                bMyAllcoed = true;
            }
        }
        if (!bMyAllcoed) {
            viewTop.textYuan.setVisibility(View.GONE);
            viewTop.textRPAmount.setVisibility(View.GONE);
            viewTop.textRPToast.setVisibility(View.INVISIBLE);
        }

        if (redPacketInfo.getStatus() == RedPacketInfo.RP_STATUS_LOOT_ALL) {
            if (redPacketInfo.getUid().equals(String.valueOf(AllOnlineApp.sToken.community_id))) {
                //红包已抢完 --  x个红包共x元，x秒被抢光
                viewTop.textRPCountStatus.setText(
                    mContext.getString(R.string.redpacket_detail_expired_my, redPacketInfo.getPacket_num(),
                        CommunityUtil.getDecimalStrByInt(redPacketInfo.getAmount(), 2), CommunityUtil
                            .getTimeHSM(mContext, redPacketInfo.getAlive_times())));
            } else {
                //红包已抢完 --  x个红包，x秒被抢光
                viewTop.textRPCountStatus.setText(
                    mContext.getString(R.string.redpacket_detail_expired_other, redPacketInfo.getPacket_num(),
                        CommunityUtil
                            .getTimeHSM(mContext, redPacketInfo.getAlive_times())));
            }
        } else {
            //红包没抢完
            if (redPacketInfo.getPacket_type() == RedPacketConstant.REDPACKET_TYPE_DAILY) {
                //每日红包，显示领取个数
                viewTop.textRPCountStatus.setText(
                    mContext.getString(R.string.redpacket_detail_deaily, redPacketInfo.getAlloc_num()));
            } else {
                //其他红包
                if (redPacketInfo.getUid().equals(String.valueOf(AllOnlineApp.sToken.community_id))) {
                    //已领取x/x个，共x/x元
                    viewTop.textRPCountStatus.setText(
                        mContext.getString(R.string.redpacket_detail_progree_my, redPacketInfo.getAlloc_num(),
                            redPacketInfo.getPacket_num(),
                            CommunityUtil.getDecimalStrByInt(redPacketInfo.getAlloc_amount(), 2),
                            CommunityUtil.getDecimalStrByInt(redPacketInfo.getAmount(), 2)));
                } else {
                    //已领取x/x个
                    viewTop.textRPCountStatus.setText(
                        mContext.getString(R.string.redpacket_detail_progree_other, redPacketInfo.getAlloc_num(),
                            redPacketInfo.getPacket_num()));
                }
            }
        }

        viewTop.imageUserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        viewTop.textRPToast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击可跳转到零钱
                if (redPacketInfo.getAllocated() == 1) {
                    //领取了红包可以跳转到我的钱包
                }
            }
        });
        return convertView;
    }

    public View createItemView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null || convertView.getTag(R.layout.redpacket_detail_item) == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.redpacket_detail_item, null);
            viewHolder.imageIcon = (VRoundImageView) convertView.findViewById(R.id.imageViewIconRp);
            viewHolder.imageRank = (ImageView) convertView.findViewById(R.id.imageViewRank);
            viewHolder.textName = (TextView) convertView.findViewById(R.id.textViewName);
            viewHolder.textTime = (TextView) convertView.findViewById(R.id.textViewTime);
            viewHolder.textAmount = (TextView) convertView.findViewById(R.id.textViewAmount);
            viewHolder.textRank = (TextView) convertView.findViewById(R.id.textViewRank);
            viewHolder.textIndex = (TextView) convertView.findViewById(R.id.textViewIndex);
            convertView.setTag(R.layout.redpacket_detail_item, viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag(R.layout.redpacket_detail_item);
        }

        final AllocInfo allocInfo = (AllocInfo) getItem(position);
        if (allocInfo == null) {
            return convertView;
        }

        //set data
        viewHolder.imageIcon.setUserData(allocInfo.getImg(),
            mContext.getResources().getDimensionPixelSize(R.dimen.setting_item_height),
            allocInfo.getVtype(), mContext.getResources().getDimensionPixelSize(R.dimen.v_small_size));

        viewHolder.textName.setText(allocInfo.getName());

        if (allocInfo.getFlag() == AllocInfo.MOST_LUCKY) {
            viewHolder.imageRank.setVisibility(View.VISIBLE);
            viewHolder.textRank.setVisibility(View.VISIBLE);
            viewHolder.textRank.setTextColor(mContext.getResources().getColor(R.color.redpacket_red));
            String imgUrl = AllOnlineApp.getAppConfig().getRedpacket_best_imgurl();
            String text = AllOnlineApp.getAppConfig().getRedpacket_best_tip();
            if (TextUtils.isEmpty(text)) {
                text = mContext.getString(R.string.most_lucky);
            }
            viewHolder.textRank.setText(text);
            viewHolder.textRank.setTextColor(mContext.getResources().getColor(R.color.redpacket_best));
            GlideApp.with(mContext).load(imgUrl).placeholder(R.drawable.redpacket_best).error(
                R.drawable.redpacket_best).override(
                mContext.getResources().getDimensionPixelOffset(R.dimen.redpacket_flag_width),
                mContext.getResources().getDimensionPixelOffset(R.dimen.redpacket_flag_height)).into(
                viewHolder.imageRank);
        } else if (allocInfo.getFlag() == AllocInfo.MOST_NOT_LUCKY) {
            viewHolder.imageRank.setVisibility(View.VISIBLE);
            viewHolder.textRank.setVisibility(View.VISIBLE);
            viewHolder.textRank.setTextColor(mContext.getResources().getColor(R.color.redpacket_green));
            String imgUrl = AllOnlineApp.getAppConfig().getRedpacket_worst_imgurl();
            String text = AllOnlineApp.getAppConfig().getRedpacket_worst_tip();
            if (TextUtils.isEmpty(text)) {
                text = mContext.getString(R.string.most_not_lucky);
            }
            viewHolder.textRank.setText(text);
            viewHolder.textRank.setTextColor(mContext.getResources().getColor(R.color.redpacket_worst));
            GlideApp.with(mContext).load(imgUrl).placeholder(R.drawable.redpacket_worst).error(
                R.drawable.redpacket_worst).override(
                mContext.getResources().getDimensionPixelOffset(R.dimen.redpacket_flag_width),
                mContext.getResources().getDimensionPixelOffset(R.dimen.redpacket_flag_height)).into(
                viewHolder.imageRank);
        } else {
            viewHolder.imageRank.setVisibility(View.GONE);
            viewHolder.textRank.setVisibility(View.GONE);
        }

        viewHolder.textAmount.setText(
            mContext.getString(R.string.amount_yuan, CommunityUtil.getDecimalStrByInt(allocInfo.getShares(), 2)));

        viewHolder.textTime.setText(CommunityUtil.getTimeHM(allocInfo.getRecv_time() * 1000));

        viewHolder.imageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        return convertView;
    }

    private void setViewPagerData(View slideLayout, final ViewPager viewPager, LoopCirclePageIndicator pageIndicator) {
        if (viewPager == null || pageIndicator == null) {
            slideLayout.setVisibility(View.GONE);
            return;
        }

        if (redPacketInfo.getSlideshows() == null || redPacketInfo.getSlideshows().size() <= 0) {
            slideLayout.setVisibility(View.GONE);
            return;
        }

        this.viewPager = viewPager;
        slideLayout.setVisibility(View.VISIBLE);
        int viewPagerHeight = (int) (AD_HEIGHT_RATIO * AllOnlineApp.screenWidth);
        final ArrayList<View> views = new ArrayList<View>();
        int size = redPacketInfo.getSlideshows().size();
        iImageViewSize = size;
        for (int i = 0; i < size; i++) {
            redPacketInfo.getSlideshows().get(i);
            addImageView(views, redPacketInfo.getSlideshows().get(i), AllOnlineApp.screenWidth, viewPagerHeight, i);
        }
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(views);
        viewPager.setAdapter(pagerAdapter);
        if (size > 1) {
            //大于等于两张，显示圆点
            pageIndicator.setVisibility(View.VISIBLE);
            pageIndicator.setViewsCount(size);
            pageIndicator.setViewPager(viewPager);
        } else {
            pageIndicator.setVisibility(View.GONE);
        }
        viewPager.setCurrentItem(pagerAdapter.getDefaultItem());
        startAutoPlay();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                startAutoPlay();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    public void startAutoPlay() {
        if (iImageViewSize <= 1) {
            return;
        }
        stopAutoPlay();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (viewPager != null && viewPager.getChildCount() > 0) {
                    viewPager.post(new Runnable() {
                        @Override
                        public void run() {
                            int curItem = viewPager.getCurrentItem();
                            if (curItem + 1 > viewPager.getAdapter().getCount() - 1) {
                                viewPager.setCurrentItem(((ImagePagerAdapter) viewPager.getAdapter()).getDefaultItem());
                            } else {
                                viewPager.setCurrentItem(curItem + 1, true);
                            }
                        }
                    });
                }
            }
        }, AD_AUTO_PLAY_PERIO, AD_AUTO_PLAY_PERIO);
    }

    public void stopAutoPlay() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private ArrayList<View> addImageView(ArrayList<View> views, final RedPacketSlideShow slideShow, int width,
        int height, final int index) {
        final ImageView imageView = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.viewpager_item, null);

        GlideApp.with(mContext).load(slideShow.getPicurl()).override(width, height).placeholder(
            R.drawable.image_default).error(R.drawable.image_default_error).centerCrop().into(imageView);
        imageView.setTag(slideShow);
        views.add(imageView);
        return views;
    }

    class ViewTop {
        public VRoundImageView imageUserIcon;
        public TextView textUserName;
        public TextView textRPTitle;
        public TextView textRpLocation;
        public TextView textRPType;
        public TextView textRPAmount;
        public TextView textRPToast;
        public TextView textRPCountStatus;
        public TextView textYuan;
        public ViewPager viewPager;
        public LoopCirclePageIndicator pageIndicator;
        public RelativeLayout slideLayout;
    }

    class ViewHolder {
        public VRoundImageView imageIcon;
        public ImageView imageRank;
        public TextView textName;
        public TextView textTime;
        public TextView textAmount;
        public TextView textRank;
        public TextView textIndex;
    }
}
