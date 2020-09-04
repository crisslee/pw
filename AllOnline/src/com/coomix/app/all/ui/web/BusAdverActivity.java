package com.coomix.app.all.ui.web;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebHistoryItem;
import android.webkit.WebView;
import android.widget.Toast;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.model.bean.Adver;
import com.coomix.app.all.share.UmengShareUtils;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.all.util.BitmapUtils;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.NativeH5Interactive;
import com.coomix.app.all.webview.ReWebChomeClient;
import com.coomix.app.all.webview.ReWebViewClient;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.CommonUtil;


/**
 * 公交广告调整进入显示页面
 *
 * @since 2015-02-09 11:50
 * @author goome
 * 
 */
public class BusAdverActivity extends CommunityWebViewActivity {
    private View closeView;
    private String from;
    private Adver adver;
    // 广告实体
    public final static String INTENT_ADVER = "adver";
    // 从哪个页面跳入
    public final static String INTENT_FROM = "from";
    /** 从社区广场进入 */
    public final static String INTENT_FROM_SQUARE = "from_square";
    /** 从启动页进入 */
    public final static String INTENT_FROM_WELCOME = "from_welcome";
    /** 从首页进入 */
    public final static String INTENT_FROM_HOME = "from_home";
    /** 从公告进入 */
    public final static String INTENT_FROM_NOTICE = "from_notice";

    /**
     * 从上班不迟到进入--查看玩法介绍
     */
    public final static String INTENT_FROM_GAME_ON_TIME = "from_notice";
    /**
     * 从群聊进
     */
    public final static String INTENT_FROM_CHAT = "from_chat";
    /**
     * 从红包进
     */
    public final static String INTENT_FROM_REDPACKET = "from_redpacket";
    /**
     * 从我的钱包进
     */
    public final static String INTENT_FROM_MY_WALLET = "from_my_wallet";
    /**
     * 从抽奖详情页
     */
    public final static String INTENT_FROM_LOTTERY_DETAIL = "from_lottery_detail";
    /**
     * 从登录页
     */
    public static final String INTENT_FROM_LOGIN = "from_login";

    /** 当前页面的title */
    private String adverTitle;
    /** 当前页面的url */
    private String adverUrl;
    private NativeH5Interactive nativeH5Interactive = null;
    private MyActionbar actionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_adver);

        Intent intent = getIntent();
        if (intent != null) {
            from = intent.getStringExtra(INTENT_FROM);
            try {
                adver = (Adver) intent.getSerializableExtra(INTENT_ADVER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        actionbar = (MyActionbar) findViewById(R.id.mineActionbar);
        actionbar.initActionbar(true, R.string.bottom_action_item_title_mine, 0, 0);
        actionbar.setCloseImageVisibility(View.GONE);
        actionbar.setCloseImageClickListener(view -> finish());
        actionbar.setLeftImageClickListener(view -> onBackPressed());

        // 分享按钮根据url中的share字段决定
        if (adver != null && !TextUtils.isEmpty(adver.jumpurl) && adver.jumpurl.contains("share=1")) {
            actionbar.setRightImageResource(R.drawable.actionbar_share);
            actionbar.setRightImageClickListener(view -> share());
        }

        mWebView = (WebView) findViewById(R.id.webview);
        initWebView();
        addOnLongClickListener();

        mWebView.setWebViewClient(new ReWebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                if (nativeH5Interactive == null) {
                    nativeH5Interactive = new NativeH5Interactive();
                }
                if (nativeH5Interactive.judgeUrlAndJump(BusAdverActivity.this, url)) {
                    return true;
                }
                url = getUrlOr2Login(url);
                if (url == null) {
                    return true;
                }
                Log.e("ttt", "override url : " + url);
                view.loadUrl(url);
                adverUrl = url;
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        mWebView.setWebChromeClient(new ReWebChomeClient(this, mProgressDialog) {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                adverTitle = title;
                actionbar.setTitle(title);
            }
        });
        if (adver != null && adver.jumpurl != null) {
            String url = getUrlOr2Login(adver.jumpurl);
            if (url == null) {
                return;
            }
            url = ReWebViewClient.modifyUrl(url);
            mWebView.loadUrl(url);
        } else {
            finish();
        }
    }

    private boolean isShare() {
        if (adver == null || adver.jumpurl == null) {
            return false;
        }

        String value = getValueByName(adver.jumpurl, "share");
        if (TextUtils.isEmpty(value)) {
            return false;
        }

        int iShare = Integer.valueOf(value);

        return (iShare == 1);
    }

    /***
     * 获取url 指定name的value;
     */
    private String getValueByName(String url, String name) {
        String result = "";
        int index = url.indexOf("?");
        String temp = url.substring(index + 1);
        String[] keyValue = temp.split("&");
        for (String str : keyValue) {
            if (str.contains(name)) {
                result = str.replace(name + "=", "");
                break;
            }
        }
        return result;
    }

    private String getUrlOr2Login(String url) {
        if (url.contains(PARAMS_NEED_LOGIN)) {
            if (!CommonUtil.isLogin()) {
                Toast.makeText(this, R.string.no_login, Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return url;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            try {
                WebHistoryItem item = mWebView.copyBackForwardList().getCurrentItem();
                adverUrl = item.getUrl();
                adverTitle = item.getTitle();
                actionbar.setTitle(adverTitle);
            } catch (Exception e) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            adverTitle = mWebView.getTitle();
                            actionbar.setTitle(adverTitle);
                            adverUrl = mWebView.getUrl();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }, 500);
                e.printStackTrace();
            }
            return;
        }
        finish();
    }

    @Override
    public void finish() {
        if (INTENT_FROM_WELCOME.equals(from)) {
            // 从欢迎页进入，点击返回键不在返回欢迎页，直接跳入主页
            if (!CommonUtil.isLogin()) {
                //去登陆
                startActivity(new Intent(this, LoginActivity.class));
            }else{
                SettingDataManager.getInstance(this).goToMainByMap(this, null);
            }
        }
        super.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void share(){
        try {
            if (adver != null) {
                String title = mWebView.getTitle();
                String url = mWebView.getUrl();
                if (CommunityUtil.isEmptyTrimStringOrNull(title)) {
                    title = adverTitle;
                }
                if (CommunityUtil.isEmptyTrimStringOrNull(url)) {
                    url = adverUrl;
                }
                String content = "快来看看我给你分享的活动：" + title;
                UmengShareUtils.toShare(BusAdverActivity.this, title, content, title,
                        BitmapUtils.takeScreenShot(BusAdverActivity.this), url, null);
            }
        } catch (Exception e) {
            if (Constant.IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
        }
    }
}
