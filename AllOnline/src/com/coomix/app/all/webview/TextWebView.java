package com.coomix.app.all.webview;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.widget.Toast;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.Adver;
import com.coomix.app.all.ui.web.BusAdverActivity;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.NativeH5Interactive;
import com.coomix.app.framework.util.CommonUtil;

/**
 * @author shishenglong
 * @since 2016年8月25日 上午9:48:44
 */
public class TextWebView extends WebView {

    private long lastTime = 0;

    private NativeH5Interactive nativeH5Interactive = null;
    private Context mContext;

    public TextWebView(Context context) {
        super(context);
        mContext = context;
        initWebView();
    }

    private void initWebView() {
        int screenDensity = getContext().getResources().getDisplayMetrics().densityDpi;
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

        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        setScrollContainer(false);

        WebSettings settings = this.getSettings();
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        settings.setDefaultZoom(zoomDensity);
        settings.setRenderPriority(RenderPriority.HIGH);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);// 设置允许访问文件数据
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);

        this.setWebViewClient(new ReWebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                if (url == null) {
                    return true;
                }

                if (nativeH5Interactive == null) {
                    nativeH5Interactive = new NativeH5Interactive();
                }
                if (nativeH5Interactive.judgeUrlAndJump(mContext, url)) {
                    return true;
                }
                url = getUrlOr2Login(url);
                if (!url.contains("?")) {
                    url += "?";
                }
                if (CommonUtil.isLogin()) {
                    url += ("&ticket=" + CommonUtil.getTicket());
                }
                String ver = CommunityUtil.getAppVersionNameNoV(mContext);
                url = url + "&ver=" + ver + "&os=android&apptype=goocar";
                Intent mIntent = new Intent(mContext, BusAdverActivity.class);
                Adver adver = new Adver();
                adver.jumpurl = url;
                mIntent.putExtra(BusAdverActivity.INTENT_ADVER, adver);
                mContext.startActivity(mIntent);

                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        this.setWebChromeClient(new ReWebChomeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });
    }

    private String getUrlOr2Login(String url) {
        if (url.contains("gmlogin=1")) {
            if (!CommonUtil.isLogin()) {
                Toast.makeText(mContext, R.string.no_login, Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return url;
    }

    public void loadData(String value) {
        try {
            StringBuffer buffer = new StringBuffer();
            // 添加viewport width适配不同手机分辨率
            String meta =
                "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no\" />";
            // String meta1 = "<meta name=\"viewport\"
            // content=\"width=device-width\"/>";
            buffer.append(meta).append(value);
            loadDataWithBaseURL(null, buffer.toString(), "text/html", "UTF-8", null);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 禁止掉webview双击自动放大功能
     **/
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                long currentTime = System.currentTimeMillis();
                long intervalTime = currentTime - lastTime;
                if (intervalTime < 300) {
                    lastTime = currentTime;
                    return true;
                } else {
                    lastTime = currentTime;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void destroy() {
        getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        stopLoading();
        removeAllViews();
        super.destroy();
    }
}
