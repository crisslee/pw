package com.coomix.app.redpacket.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.dialog.AskDialog;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.service.AllOnlineApiClient;
import com.coomix.app.all.ui.wallet.BindWechatActivity;
import com.coomix.app.all.util.AppUtil;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.redpacket.activity.DetailRedPacketActivity;
import com.coomix.app.redpacket.util.RedPacketConstant;
import com.coomix.app.redpacket.util.RedPacketInfo;

public abstract class RedPacketBaseView extends LinearLayout {
    protected View mainView;
    protected LayoutInflater mInflater;
    protected Context mContext;
    protected TextView textTitle;
    protected TextView textHint;
    protected TextView textExplain;
    protected ImageView imageFlag;
    private String redPacketId;
    private ProgressDialogEx progressDialogEx;
    private boolean bRedPacketSended = false;
    private boolean isGoToDetail = false;
    protected RedPacketInfo redPacketInfo;
    private long toChatId = 0;

    private final int WEIXIN_UNBIND_MSG = 10000;
    private final int COMMUNITY_RESTART_MSG = 10001;

    public RedPacketBaseView(Context context, String redPacketId) {
        this(context, null, 0, redPacketId);
    }

    public RedPacketBaseView(Context context, AttributeSet attrs, String redPacketId) {
        this(context, attrs, 0, redPacketId);
    }

    public RedPacketBaseView(Context context, AttributeSet attrs, int defStyle, String redPacketId) {
        super(context, attrs, defStyle);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.redPacketId = redPacketId;
        initViews();
        setOnClickListener(onClickListener);
    }

    public void setRedPacketId(String id) {
        this.redPacketId = id;
    }

    public void setRedPacketSended(boolean bRedPacketSended) {
        this.bRedPacketSended = bRedPacketSended;
    }

    public void setRedPacketTitle(CharSequence text, TextView.BufferType type) {
        if (textTitle != null) {
            textTitle.setText(text, type);
        }
    }

    public void showWaitInfoDialog(Context context, String msg) {
        if (progressDialogEx != null) {
            if (!progressDialogEx.isShowing()) {
                progressDialogEx.show();
            }
            return;
        }
        progressDialogEx = new ProgressDialogEx(context);
        progressDialogEx.setAutoDismiss(false);
        progressDialogEx.setCancelOnTouchOutside(false);

        try {
            progressDialogEx.show(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissWaitDialog() {
        if (progressDialogEx != null && progressDialogEx.isShowing()) {
            progressDialogEx.dismiss();
        }
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //先绑定微信
            if (isWechatBinded() == false) {
                Intent intentBindWechat = new Intent(getContext(), BindWechatActivity.class);
                intentBindWechat.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                getContext().startActivity(intentBindWechat);
                return;
            }
            if (!bRedPacketSended) {
                Toast.makeText(mContext, "not sended", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!CommonUtil.checkAndGoToLogin(getContext())) {
                //去登陆了
                return;
            }
            if (needOpenGps()) {
                showAskGpsDialog();
            } else {
                requestRedPacketInfo(false);
            }
        }
    };

    private boolean needOpenGps() {
        return needLocation() && !CommunityUtil.isGPSOpened(mContext);
    }

    private boolean needLocation() {
        return redPacketInfo != null && redPacketInfo.isCommunityRedpacket() && redPacketInfo.getAlloc_range() > 0;
    }

    private void showAskGpsDialog() {
        final AskDialog askGps = new AskDialog(mContext);
        askGps.setShowMsg(R.string.redpacket_range_gps_off);
        askGps.setNoText(R.string.redpacket_range_gps_no);
        askGps.setYesText(R.string.redpacket_range_gps_yes);
        askGps.setOnYesClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                askGps.dismiss();
            }
        });
        askGps.show();
    }

    private class CallBackRunnale implements Runnable {
        private Result result = null;

        CallBackRunnale(Result response) {
            this.result = response;
        }

        @Override
        public void run() {
            if (result == null) {
                dismissWaitDialog();
                return;
            }

            if (result.statusCode == Result.ERROR_NETWORK) {
                dismissWaitDialog();
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            } else {
                dismissWaitDialog();
                if (result.success && result.mResult != null && result.mResult instanceof RedPacketInfo) {
                    RedPacketInfo info = (RedPacketInfo) result.mResult;
                    if (isGoToDetail) {
                        isGoToDetail = false;
                        goToDetailActivity(info);
                    } else {
                        parseRedPacketStatusData(info);
                    }
                } else {
                    Toast.makeText(getContext(), R.string.leave_group_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void requestRedPacketInfo(boolean bGoToDetail) {
        //        AMapLocation currLocation = AllOnlineApp.getCurrentLocation();
        //        final String lat = String.valueOf(currLocation.getLatitude());
        //        final String lng = String.valueOf(currLocation.getLongitude());
        //        if (needLocation() && !RpLocationUtil.isCurrLocationValid())
        //        {
        //            Toast.makeText(mContext, R.string.redpacket_location_fail, Toast.LENGTH_SHORT).show();
        //        }
        //        else
        {
            //先网络请求红包状态，然后根据状态跳转
            this.isGoToDetail = bGoToDetail;
            showWaitInfoDialog(getContext(), getContext().getString(R.string.query_ing));

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Result result = AllOnlineApp.mApiClient.getRedPacketInfoById(-1, CommonUtil.getTicket(),
                        redPacketId, 0, RedPacketConstant.REDPACKET_USER_PER_PAGE,
                        AllOnlineApiClient.MAP_TYPE_GOOGLE,
                        String.valueOf(GlobalParam.getInstance().get_lat()),
                        String.valueOf(GlobalParam.getInstance().get_lng()), 0);
                    if (result.statusCode == Result.ERRCODE_LONGIN_TWO_DEVICES) {
                        if (handler != null) {
                            handler.sendEmptyMessage(COMMUNITY_RESTART_MSG);
                        }
                        return;
                    } else if (result.statusCode == Result.ERR_INVALID_SESSION) {
                        if (handler != null) {
                            handler.sendEmptyMessage(WEIXIN_UNBIND_MSG);
                        }
                        return;
                    }
                    if (handler == null) {
                        handler = new Handler(Looper.getMainLooper());
                    }
                    handler.post(new CallBackRunnale(result));
                }
            };
            new Thread(myRunnable).start();
        }
    }

    private void restartCommunityOnly(final Context context) {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Result resultLogin = AllOnlineApp.mApiClient.login(AllOnlineApp.sToken.access_token,
                        AllOnlineApp.channelId(context), AllOnlineApp.sAccount);
                    if (resultLogin != null && resultLogin.success) {
                        if (isWechatBinded()) {
                            requestRedPacketInfo(false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0);
    }

    @SuppressWarnings("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case COMMUNITY_RESTART_MSG: {
                    dismissWaitDialog();
                    restartCommunityOnly(getContext());
                    break;
                }
                case WEIXIN_UNBIND_MSG: {
                    dismissWaitDialog();
                    AppUtil.showWeinxinUndindDialog(AllOnlineApp.getInstantce().mApp, 1);
                    break;
                }
                default:
                    break;
            }
        }
    };

    private void parseRedPacketStatusData(RedPacketInfo redPacketInfo) {
        if (redPacketInfo != null) {
            if (redPacketInfo.getAllocated() == 1) {
                //已经抢到红包，直接去领取详情查看
                goToDetailActivity(redPacketInfo);
            } else {
                //没有领取，则去领取界面
                final DialogShowRedPacket showRedPacket = new DialogShowRedPacket(getContext(), redPacketInfo);
                showRedPacket.setToChatId(toChatId);
                showRedPacket.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (showRedPacket.isGoToRedPacketDetail()) {
                            requestRedPacketInfo(true);
                        }
                    }
                });
                try {
                    showRedPacket.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void goToDetailActivity(RedPacketInfo redPacketInfo) {
        Intent intent = new Intent(getContext(), DetailRedPacketActivity.class);
        intent.putExtra(DetailRedPacketActivity.EXTRA_REDPACKET_INFO, redPacketInfo);
        getContext().startActivity(intent);
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

    public long getToChatId() {
        return toChatId;
    }

    public void setToChatId(long toChatId) {
        this.toChatId = toChatId;
    }

    protected abstract void initViews();
}
