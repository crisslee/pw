package com.coomix.app.all.share;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.common.Keys;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.bean.CommunityShare;
import com.coomix.app.all.model.response.RespShareLocation;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.BitmapUtils;
import com.coomix.app.all.util.CommunityPictureUtil;
import com.coomix.app.all.webview.ReWebViewClient;
import com.coomix.app.framework.util.CommonUtil;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;

public class UmengShareUtils {

    static final int CONTENT = R.string.share_default_content;
    static final int TITLE = R.string.share_default_title;
    static final String TARGETURL = "http://m.gpsoo.net";
    private static BaseActivity baseActivity = null;

    /**
     * 对原生的活动进行分享
     **/
    public static boolean toShare(Activity act, CommunityShare share, Bitmap shareBitmap) {
        boolean result = false;
        try {
            // MobclickAgent.onEvent(context, Constant.UmengEvent.v3_4_0_activity_share_click);
            if (share != null && shareBitmap != null && !shareBitmap.isRecycled()) {
                String title = share.getTitle();
                final String url = share.getUrl();
                String content = share.getDescription();
                if (!CommonUtil.isEmptyTrimStringOrNull(url)) {
                    if (CommonUtil.isEmptyTrimStringOrNull(title)) {
                        title = act.getString(TITLE);
                        share.setTitle(title);
                    }
                    if (CommonUtil.isEmptyTrimStringOrNull(content)) {
                        content = act.getResources().getString(R.string.share_act_content_hint) + title;
                        share.setDescription(content);
                    }

                    toShare(act, title, content, title, shareBitmap, url, null);
                    result = true;
                }
            }
        } catch (Exception e) {
            if (Constant.IS_DEBUG_MODE) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * umeng分享
     *
     * @param title 为null则使用默认{@link #TITLE}
     * @param content 为null则使用默认{@link #CONTENT}
     * @param circleTitle 为null则使用默认{@link #CONTENT}，朋友圈无content，只有title
     * @param bmp 分享显示的图片，为null则显示 R.drawable.ic_share
     * @param url 为null则使用默认{@link #TARGETURL}
     * @param weiboContentFlag 微博标签 #weiboContentFlag#
     */
    public static void toShare(final Activity act, String title, String content,
        String circleTitle, Bitmap bmp, String url, String weiboContentFlag) {
        // 分享的URL移除掉我们添加进去的私有信息
        url = ReWebViewClient.removeSelfInfo(url);
        url = ReWebViewClient.addOsAndVersion(url);
        UMImage image = null;
        if (bmp == null) {
            image = new UMImage(act, R.drawable.ic_share);
        } else {
            image = new UMImage(act, bmp);
        }
        if (TextUtils.isEmpty(title)) {
            title = act.getString(TITLE);
        }
        if (TextUtils.isEmpty(content)) {
            content = act.getString(CONTENT);
        }
        if (TextUtils.isEmpty(circleTitle)) { // 朋友圈设置content无效，默认以CONTENT为TITLE
            circleTitle = act.getString(CONTENT);
        }
        if (TextUtils.isEmpty(url)) {
            url = TARGETURL;
        }
        if (!url.trim().startsWith("http")) {
            url = "http://bussem.gpsoo.net/goocar-post/#/?" + url;
        }
        openShare(act, title, content, circleTitle, image, url, weiboContentFlag);
    }

    /**
     * umeng分享
     *
     * @param title 为null则使用默认{@link #TITLE}
     * @param content 为null则使用默认{@link #CONTENT}
     * @param circleTitle 为null则使用默认{@link #CONTENT}，朋友圈无content，只有title
     * @param bmp 分享显示的图片，为null则显示 R.drawable.ic_share
     * @param weiboContentFlag 微博标签 #weiboContentFlag#
     */
    public static void toShareLocation(final Activity act, String title, StringBuffer content, String circleTitle,
        Bitmap bmp, String weiboContentFlag, String imei) {
        // 分享的URL移除掉我们添加进去的私有信息
        UMImage image = null;
        if (bmp == null) {
            image = new UMImage(act, R.drawable.share_location);
        } else {
            image = new UMImage(act, bmp);
        }
        if (TextUtils.isEmpty(title)) {
            title = act.getString(TITLE);
        }
        if (TextUtils.isEmpty(content)) {
            if (content == null) {
                content = new StringBuffer();
            }
            content.append(act.getString(CONTENT));
        }
        if (TextUtils.isEmpty(circleTitle)) {
            // 朋友圈设置content无效，默认以CONTENT为TITLE
            circleTitle = act.getString(CONTENT);
        }

        openShareForLocation(act, title, content, circleTitle, image, weiboContentFlag, imei);
    }

    private static void openShare(final Activity act, final String title, final String content,
        final String circleTitle, final UMImage image, final String url, final String weiboContentFlag) {
        ArrayList<TextSet> list = new ArrayList<TextSet>();
        TextSet t1 = new TextSet(R.string.share_to_weixin, R.drawable.share_wx_icon, false, new OnClickListener() {
            @Override
            public void onClick(View v) {
                share(act, SHARE_MEDIA.WEIXIN, title, content, image, url);
            }
        });
        list.add(t1);
        TextSet t2 = new TextSet(R.string.share_to_weixin_circle, R.drawable.share_wx_circle_icon, false,
            new OnClickListener() {
                @Override
                public void onClick(View v) {
                    share(act, SHARE_MEDIA.WEIXIN_CIRCLE, circleTitle, content, image, url);
                }
            });
        list.add(t2);
        TextSet t3 = new TextSet(R.string.share_to_qq, R.drawable.share_qq_icon, false, new OnClickListener() {
            @Override
            public void onClick(View v) {
                share(act, SHARE_MEDIA.QQ, title, content, image, url);
            }
        });
        list.add(t3);
        TextSet t4 = new TextSet(R.string.share_to_qzone, R.drawable.share_qzone_icon, false, new OnClickListener() {
            @Override
            public void onClick(View v) {
                share(act, SHARE_MEDIA.QZONE, title, content, image, url);
            }
        });
        list.add(t4);
        TextSet t5 = new TextSet(R.string.share_to_weibo, R.drawable.share_sina_icon, false, new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sinaContent = content;
                if (!TextUtils.isEmpty(weiboContentFlag)) {
                    sinaContent = "#" + weiboContentFlag + "#" + content;
                }
                share(act, SHARE_MEDIA.SINA, title, sinaContent, image, url);
            }
        });
        list.add(t5);
        PopupWindowUtil.showPopWindow(act, act.getWindow().getDecorView(), R.string.share_to, list, true);
    }

    private static void openShareForLocation(final Activity act, final String title, final StringBuffer content,
        final String circleTitle, final UMImage image, final String weiboContentFlag, String imei) {
        StringBuffer timeSb = new StringBuffer();
        StringBuffer duration = new StringBuffer();
        ArrayList<TextSet> list = new ArrayList<TextSet>();
        TextSet t1 = new TextSet(R.string.share_to_weixin, R.drawable.share_wx_icon, false, new OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationUrlAndShare(act, imei, duration.toString(), SHARE_MEDIA.WEIXIN, title, content, timeSb, image);
            }
        });
        list.add(t1);
        TextSet t3 = new TextSet(R.string.share_to_qq, R.drawable.share_qq_icon, false, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(act, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                    getLocationUrlAndShare(act, imei, duration.toString(), SHARE_MEDIA.QQ, title, content, timeSb,
                        image);
                } else {
                    Toast.makeText(act, "分享到QQ需要存储权限", Toast.LENGTH_SHORT).show();
                }
            }
        });
        list.add(t3);
        PopupWindowUtil.showPopWindowForLocation(act, act.getWindow().getDecorView(), R.string.share_to, list, true,
                timeSb,duration);
    }

    private static void getLocationUrlAndShare(Activity act, String imei, String duration, SHARE_MEDIA shareMedia,
        String title, StringBuffer content, StringBuffer timeSb, final UMImage image) {
        if (act instanceof BaseActivity) {
            baseActivity = (BaseActivity) act;
        }
        if (baseActivity != null) {
            baseActivity.showLoading(act.getString(R.string.please_wait));
        }
        Disposable disposable = DataEngine.getAllMainApi().getShareLocationUrl(
            GlobalParam.getInstance().getCommonParas(),
            AllOnlineApp.sAccount,
            GlobalParam.getInstance().getAccessToken(),
            duration, imei)
            .compose(RxUtils.toMain())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespShareLocation>() {
                @Override
                public void onNext(RespShareLocation respShareLocation) {
                    if (baseActivity != null) {
                        baseActivity.hideLoading();
                    }
                    if (respShareLocation != null && !TextUtils.isEmpty(respShareLocation.getData())) {
                        share(act, shareMedia, title, content.append(timeSb).toString(), image,
                            respShareLocation.getData());
                    }
                }

                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    if (baseActivity != null) {
                        baseActivity.hideLoading();
                    }
                    Toast.makeText(act, e.getErrMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        if (baseActivity != null) {
            baseActivity.subscribeRx(disposable);
        }
    }

    private static void share(Activity act, SHARE_MEDIA media, String title, String content, UMImage image,
        String url) {
        StringBuilder sb = new StringBuilder(url);
        if (url.contains("?")) {
            sb.append("&");
        } else {
            sb.append("?");
        }
        sb.append("gffrom=");
        switch (media) {
            case WEIXIN:
                sb.append("wx");
                break;
            case WEIXIN_CIRCLE:
                sb.append("pyq");
                break;
            case QQ:
                sb.append("qq");
                break;
            case QZONE:
                sb.append("qzone");
                break;
            case SINA:
                sb.append("weibo");
                break;
            default:
                break;
        }
        UMWeb web = new UMWeb(sb.toString());
        web.setTitle(title);
        web.setThumb(image);
        web.setDescription(content);
        new ShareAction(act).setPlatform(media).setCallback(new UmengShareListner(act)).withMedia(web).share();
    }

    /***
     * 直接调起微信进行分享--iType=0微信，iType=1朋友圈
     * */
    public static void shareDirectTo(final Activity ctx, String title, String content, String bmpUrl, String url,
        int iType) {
        UMImage umImage;
        if (!TextUtils.isEmpty(bmpUrl)) {
            umImage = new UMImage(ctx, bmpUrl);
        } else {
            Bitmap bitmap = BitmapUtils.takeScreenShot(ctx);
            if (bitmap == null) {
                bitmap = CommunityPictureUtil.drawable2Bitamp(ctx.getResources().getDrawable(R.drawable.ic_share));
            }
            umImage = new UMImage(ctx, bitmap);
        }

        if (TextUtils.isEmpty(title)) {
            title = ctx.getString(TITLE);
        }
        if (TextUtils.isEmpty(content)) {
            content = ctx.getString(CONTENT);
        }
        if (TextUtils.isEmpty(url)) {
            url = TARGETURL;
        }

        PlatformConfig.setWeixin(Keys.WEIXIN_APP_ID, Keys.WEIXIN_APP_SECRET);

        if (iType == 1) {
            share(ctx, SHARE_MEDIA.WEIXIN_CIRCLE, title, content, umImage, url/*, null, shareListener*/);
        } else {
            share(ctx, SHARE_MEDIA.WEIXIN, title, content, umImage, url/*, null, shareListener*/);
        }
    }

    public static class UmengShareListner implements UMShareListener {
        Context ctx;

        public UmengShareListner(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        public void onStart(SHARE_MEDIA share_media) {
        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            Toast.makeText(ctx, ctx.getString(R.string.share_success), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            CrashReport.postCatchedException(t);
            Toast.makeText(ctx, ctx.getString(R.string.share_failed), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            Toast.makeText(ctx, ctx.getString(R.string.share_cancel), Toast.LENGTH_SHORT).show();
        }
    }
}
