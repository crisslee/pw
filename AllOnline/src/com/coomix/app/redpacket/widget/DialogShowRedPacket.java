package com.coomix.app.redpacket.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.location.AMapLocation;
import com.bumptech.GlideApp;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.service.AllOnlineApiClient;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.service.ServiceAdapter.ServiceAdapterCallback;
import com.coomix.app.all.ui.wallet.BindWechatActivity;
import com.coomix.app.all.widget.RoundImageView;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.redpacket.activity.DetailRedPacketActivity;
import com.coomix.app.redpacket.util.RedPacketConstant;
import com.coomix.app.redpacket.util.RedPacketInfo;

/**
 * Created by ssl on 2017/2/13.
 */
public class DialogShowRedPacket extends Dialog implements View.OnClickListener, ServiceAdapterCallback {
    private RoundImageView imageUserIcon;
    private TextView textUserName;
    private TextView textHelloWords;
    private TextView textStatus;
    private ImageView imageToOpen;
    private TextView textLookRpDetail;
    //private ServerRequestController serverRequestController;
    private ServiceAdapter serviceAdapter;
    private int iOpenRedPacket = -1;
    private RedPacketInfo redPacketInfo;
    private AnimationDrawable animationDrawable;
    private boolean isGoToDetail = false;
    private boolean bClickDismiss = true;
    private boolean isOpened = false;
    private long toChatId = 0;

    public DialogShowRedPacket(Context context, RedPacketInfo redPacketInfo) {
        super(context, R.style.add_dialog);

        this.redPacketInfo = redPacketInfo;
        this.isGoToDetail = false;

        initViews();

        setData(redPacketInfo);

        serviceAdapter = ServiceAdapter.getInstance(getContext());
        serviceAdapter.registerServiceCallBack(this);
    }

    private void initViews() {
        View mainView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_show_redpacket, null);
        setContentView(mainView);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        params.windowAnimations = R.style.AnimPopScale;
        getWindow().setAttributes(params);

        textHelloWords = (TextView) mainView.findViewById(R.id.textViewRPTitle);
        textUserName = (TextView) mainView.findViewById(R.id.textViewUserName);
        textStatus = (TextView) mainView.findViewById(R.id.textViewSendARP);
        imageUserIcon = (RoundImageView) mainView.findViewById(R.id.imageViewUserIcon);
        imageToOpen = (ImageView) mainView.findViewById(R.id.imageViewOpen);
        textLookRpDetail = (TextView) mainView.findViewById(R.id.textViewMyRPDetail);

        mainView.findViewById(R.id.imageViewClose).setOnClickListener(this);
        imageToOpen.setOnClickListener(this);
        textLookRpDetail.setOnClickListener(this);
        mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bClickDismiss) {
                    dismiss();
                }
            }
        });
    }

    public void setData(RedPacketInfo redPacketInfo) {
        this.isGoToDetail = false;
        this.redPacketInfo = redPacketInfo;
        if (redPacketInfo != null) {
            textUserName.setText(redPacketInfo.getName());

            try {
                imageUserIcon.setImageResource(R.drawable.login_icon);
                GlideApp.with(getContext().getApplicationContext())
                    .load(redPacketInfo.getImg())
                    .placeholder(R.drawable.login_icon)
                    .error(R.drawable.login_icon)
                    .into(imageUserIcon);
            } catch (Exception e) {

            }

            int iRedPacketType = redPacketInfo.getPacket_type();
            if (iRedPacketType != RedPacketConstant.REDPACKET_TYPE_NORMAL
                && iRedPacketType != RedPacketConstant.REDPACKET_TYPE_RANDOM
                && iRedPacketType != RedPacketConstant.REDPACKET_TYPE_DAILY) {
                //4.1.0版本只支持普通红包和随机红包，4.5.0支持每日红包
                textHelloWords.setSingleLine(false);
                textHelloWords.setText(R.string.redpacket_not_support);
                textStatus.setVisibility(View.GONE);
                textLookRpDetail.setVisibility(View.GONE);
                imageToOpen.setVisibility(View.INVISIBLE);
                return;
            }

            if (redPacketInfo.isCommunityRedpacket()
                && redPacketInfo.getIn_range() == RedPacketInfo.RANGE_OUT_OF_RANGE) {
                // 对于范围社区红包，不在领取范围内，显示错误
                textHelloWords.setSingleLine(false);
                textHelloWords.setText(R.string.redpacket_out_of_range);
                textStatus.setVisibility(View.GONE);
                //不在范围内，自己发的可以看到领取详情
                imageToOpen.setVisibility(View.INVISIBLE);
                if (redPacketInfo.getUid().equals(String.valueOf(AllOnlineApp.sToken.community_id))) {
                    textLookRpDetail.setVisibility(View.VISIBLE);
                    textLookRpDetail.setText(R.string.see_redpacket_detail);
                } else {
                    textLookRpDetail.setVisibility(View.GONE);
                }
                return;
            }

            /*****************红包的状态显示--可以抢、领取完、过期*******************/
            if (redPacketInfo.getStatus() == RedPacketInfo.RP_STATUS_IN_PROGRESS
                || redPacketInfo.getStatus() == RedPacketInfo.RP_STATUS_SENDED) {
                //可以领取
                bClickDismiss = false;
                if (!TextUtils.isEmpty(redPacketInfo.getHello_words())) {
                    textHelloWords.setText(redPacketInfo.getHello_words());
                } else {
                    textHelloWords.setText("");
                }
                if (iRedPacketType == RedPacketConstant.REDPACKET_TYPE_NORMAL) {
                    textStatus.setText(R.string.redpacket_type_normal);
                } else if (iRedPacketType == RedPacketConstant.REDPACKET_TYPE_RANDOM) {
                    textStatus.setText(R.string.redpacket_type_random);
                } else {
                    textStatus.setText("");
                }
            } else if (redPacketInfo.getStatus() == RedPacketInfo.RP_STATUS_LOOT_ALL) {
                //已被领取完
                textHelloWords.setText(R.string.redpacket_clear);
                textStatus.setVisibility(View.INVISIBLE);
                imageToOpen.setVisibility(View.INVISIBLE);
            } else if (redPacketInfo.getStatus() >= RedPacketInfo.RP_STATUS_EXPIRED) {
                //红包过期
                textHelloWords.setText(R.string.redpacket_expired);
                textStatus.setVisibility(View.INVISIBLE);
                imageToOpen.setVisibility(View.INVISIBLE);
            }

            /*****************是否显示查看红包详情*******************/
            if (redPacketInfo.getPacket_type() == RedPacketConstant.REDPACKET_TYPE_DAILY) {
                textLookRpDetail.setVisibility(View.GONE);
            } else {
                if (redPacketInfo.getUid().equals(String.valueOf(AllOnlineApp.sToken.community_id))) {
                    //自己发的红包，无论怎么样都可以查看详情
                    textLookRpDetail.setVisibility(View.VISIBLE);
                    textLookRpDetail.setText(R.string.see_redpacket_detail);
                } else {
                    //别人发的红包只有领取完的红包可以查看详情
                    if (redPacketInfo.getStatus() == RedPacketInfo.RP_STATUS_LOOT_ALL) {
                        //领取的红包，可以查看详情
                        textLookRpDetail.setVisibility(View.VISIBLE);
                        textLookRpDetail.setText(R.string.see_redpacket_detail);
                    } else {
                        textLookRpDetail.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageViewClose:
                dismiss();
                break;

            case R.id.imageViewOpen:
                //拆红包
                if (redPacketInfo != null && iOpenRedPacket <= 0) {
                    if (!CommonUtil.checkAndGoToLogin(getContext(), true)) {
                        //此处判断主要针对每日红包
                        return;
                    }
                    //先绑定微信
                    if (isWechatBinded() == false) {
                        Intent intentBindWechat = new Intent(getContext(), BindWechatActivity.class);
                        intentBindWechat.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        getContext().startActivity(intentBindWechat);
                        break;
                    }
                    //设置、开始动画
                    imageToOpen.setImageResource(R.drawable.redpacket_open_ani);
                    Drawable drawable = imageToOpen.getDrawable();
                    if (drawable != null) {
                        animationDrawable = (AnimationDrawable) drawable;
                        animationDrawable.start();
                    }
                    openRedPacket(redPacketInfo.getRedpacket_id());
                }
                break;

            case R.id.textViewMyRPDetail:
                //查看红包详情
                isGoToDetail = true;
                dismiss();
                break;
            default:
                break;
        }
    }

    private boolean isWechatBinded() {
        //        CommunityUser user = AllOnlineApp.getCommunityUser();
        //        if (!user.isWechatBinded()) {
        //            return false;
        //        } else {
        //            return true;
        //        }
        return true;
    }

    /***拆红包**/
    private void openRedPacket(String redpacketId) {
        AMapLocation currLoc = AllOnlineApp.getCurrentLocation();
        String lat = String.valueOf(GlobalParam.getInstance().get_lat());
        String lng = String.valueOf(GlobalParam.getInstance().get_lng());
        //有转圈动画,请求到数据或状态后停掉动画
        iOpenRedPacket = serviceAdapter.openRedPacket(this.hashCode(), CommonUtil.getTicket(), redpacketId,
            AllOnlineApiClient.MAP_TYPE_GOOGLE, lat, lng);
    }

    public boolean isGoToRedPacketDetail() {
        return isGoToDetail;
    }

    @Override
    public void callback(int messageId, Result result) {
        if (result.statusCode == Result.ERROR_NETWORK) {
            iOpenRedPacket = -1;
            Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
        } else if (iOpenRedPacket == messageId) {
            iOpenRedPacket = -1;
            if (animationDrawable != null && animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
            imageToOpen.setImageResource(R.drawable.open_redpacket);
            if (result != null && result.success) {
                parseOpenRedPacketData(result.mResult);
            } else {
                Toast.makeText(getContext(), R.string.leave_group_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isOpened() {
        return isOpened;
    }

    private void parseOpenRedPacketData(Object object) {
        if (object != null && object instanceof RedPacketInfo) {
            isOpened = true;
            final RedPacketInfo redPacketInfo = (RedPacketInfo) object;
            if (redPacketInfo.getAllocated() == 1) {
                //已经抢到红包，直接去领取详情查看
                dismiss();
                Intent intent = new Intent(getContext(), DetailRedPacketActivity.class);
                intent.putExtra(DetailRedPacketActivity.EXTRA_REDPACKET_INFO, redPacketInfo);
                getContext().startActivity(intent);
                sendOpenRedpacketBroadcast(redPacketInfo);
            } else {
                //没有领取到，则界面显示手慢
                setData(redPacketInfo);
            }

            //            if(!TextUtils.isEmpty(redPacketInfo.getHxuser()) && !TextUtils.isEmpty(redPacketInfo.getHxpwd()))
            //            {
            //                //抢红包加群
            //                EmUser hdUser = new EmUser();
            //                hdUser.setHxuser(redPacketInfo.getHxuser());
            //                hdUser.setHxpwd(redPacketInfo.getHxpwd());
            //                if (MainActivity.mActivity != null)
            //                {
            //                    MainActivity.mActivity.setEmUser(hdUser, 3000);
            //                }
            //                else if (AllOnlineApp.getCommunityUser() != null)
            //                {
            //                    AllOnlineApp.getCommunityUser().setEmUser(hdUser);
            //                    PreferenceUtil.commitString(Constant.PREFERENCE_USER_INFO, new Gson().toJson(BusOnlineApp.getUser(), User.class));
            //                }
            //            }
        }
    }

    private void sendOpenRedpacketBroadcast(RedPacketInfo redPacketInfo) {
        if (redPacketInfo == null) {
            return;
        }

        Intent intent = new Intent(RedPacketConstant.OPEN_REDPACKET_ACTION);
        redPacketInfo.setToChatId(String.valueOf(toChatId));
        intent.putExtra(RedPacketConstant.REDPACKET_DATA, redPacketInfo);
        getContext().sendBroadcast(intent);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        //release();
        if (serviceAdapter != null) {
            serviceAdapter.unregisterServiceCallBack(this);
        }
    }

    //    public void release()
    //    {
    //        if(serverRequestController != null)
    //        {
    //            serverRequestController.unregisterServiceCallBack(this);
    //            serverRequestController = null;
    //        }
    //    }

    public ImageView getOpenImageView() {
        return imageToOpen;
    }

    public long getToChatId() {
        return toChatId;
    }

    public void setToChatId(long toChatId) {
        this.toChatId = toChatId;
    }
}
