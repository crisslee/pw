package com.coomix.app.all.ui.advance;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.log.GLog;
import com.coomix.app.all.manager.DomainManager;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.widget.MyActionbar;
import com.coomix.app.framework.util.OSUtil;
import com.goome.gpns.utils.FileOperationUtil;

import java.net.URLEncoder;

// 下发指令界面
public class CommandListWebActivity extends BaseActivity {
    private static final String TAG = CommandListWebActivity.class.getSimpleName();
    private WebView mWebView;
    private DeviceInfo device;
    private boolean offline = false;
    private LinearLayout mRootLayout;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_command_list_web);

        // 输入法弹出时候 界面上移
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        device = (DeviceInfo) getIntent().getSerializableExtra(Constant.KEY_DEVICE);
        if (device != null && (device.getState() == DeviceInfo.STATE_RUNNING
            || device.getState() == DeviceInfo.STATE_STOP)) {
            offline = false;
        } else {
            offline = true;
        }

        MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.device_command, 0, 0);

        mWebView = (WebView) findViewById(R.id.webview);

        //getLang();       
        String appVersion = OSUtil.getAppVersionNameExtend(this); 
        String requestUrl = getCommandWebUrl();

        mWebView.setInitialScale(80);

        mWebView.setScrollbarFadingEnabled(true);
        // 自动加载
        mWebView.setWebViewClient(new MyWebViewClient());

        // 设定编码格式
        mWebView.getSettings().setDefaultTextEncodingName("UTF-8");

        WebSettings settings = mWebView.getSettings();
        // settings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        // settings.setDefaultZoom(ZoomDensity.FAR);

        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
        switch (screenDensity) {
        case DisplayMetrics.DENSITY_LOW:
            zoomDensity = WebSettings.ZoomDensity.CLOSE;
            break;
        case DisplayMetrics.DENSITY_MEDIUM:
            zoomDensity = WebSettings.ZoomDensity.MEDIUM;
            break;
        case DisplayMetrics.DENSITY_HIGH:
            zoomDensity = WebSettings.ZoomDensity.FAR;
            break;
        }
        settings.setDefaultZoom(zoomDensity);

        settings.setRenderPriority(RenderPriority.HIGH);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);// 设置允许访问文件数据
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        // 屏蔽js的一些方法
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });

        mRootLayout = (LinearLayout) findViewById(R.id.root_layout);
        mRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mRootLayout.getRootView().getHeight() - mRootLayout.getHeight();
                if (heightDiff > 100) { // 说明键盘是弹出状态
                    // mRootLayout.layout(0, -200, 0, 0);
                } else {  //键盘收起状态
                    // mRootLayout.layout(0, 0, 0, 0);
                }
            }
        });

        GLog.i(TAG, "requestUrl=" + requestUrl);
        mWebView.loadUrl(requestUrl);
    }

    private String getCommandWebUrl(){
        //String requestUrl = AllOnlineApiClient.commandList(device.getImei(),device.getName(),device.dev_type,offline ? "true":"false", AllOnlineApp.sAccount, AllOnlineApp.sToken.access_token,appVersion);
        // "&devtype=" + devtype
        StringBuffer stringBuffer = new StringBuffer(DomainManager.getInstance().getWebHost());
        try {
            stringBuffer.append("wx/command.shtml?");
            stringBuffer.append("account=" + URLEncoder.encode(AllOnlineApp.sAccount, "UTF-8"));
            stringBuffer.append("&time=" +  System.currentTimeMillis() / 1000);
            stringBuffer.append("&access_type=inner");
            stringBuffer.append("&source=" + Constant.NEW_VERSION_TAG);
            stringBuffer.append("&locale=" + SettingDataManager.language);
            stringBuffer.append("&imei=" + device.getImei());
            stringBuffer.append("&devtype=" + URLEncoder.encode(device.getDev_type(), "UTF-8"));
            stringBuffer.append("&name=" + URLEncoder.encode(device.getName(), "UTF-8"));
            stringBuffer.append("&access_token=" +  URLEncoder.encode(AllOnlineApp.sToken.access_token, "UTF-8"));
            stringBuffer.append("&offline=" + offline);
            stringBuffer.append("&appver=" + OSUtil.getAppVersionNameNoV(this));
            stringBuffer.append("&api_domain=" + DomainManager.sRespDomainAdd.domainWeb);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    /**
     * 显示进度条
     * 
     * @author User
     * 
     */
    class MyWebViewClient extends WebViewClient {

        /**
         * 网页结束的时候
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        /**
         * 网页开启的时候
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        /**
         * 网页出错的时候
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        /**
         * 点击打开网页前的准备工作
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        // 重写此方法才能够处理在浏览器中的按键事件。
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            if (mWebView != null) {
                final ViewGroup viewGroup = (ViewGroup) mWebView.getParent();
                if (viewGroup != null) {
                    viewGroup.removeView(mWebView);
                }
                mWebView.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWebView != null) {
            mWebView.destroy();
        }
    }

}
