package com.coomix.app.all.ui.web;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;

import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.ui.web.CommunityWebViewActivity;
import com.coomix.app.all.webview.ReWebChomeClient;
import com.coomix.app.all.webview.ReWebViewClient;
import com.coomix.app.all.widget.MyActionbar;

/**
 * @author shishenglong
 * @since 2016年8月23日 上午9:23:59
 */
public class DisclaimerActivity extends CommunityWebViewActivity {
    public static final String URL = "URL";
    public static final String TITLE = "TITLE";

    private String mUrl = Constant.DISCLAIMER_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_disclaimer);

        initViews();

        setData();
    }

    private void initViews() {
        final MyActionbar actionbar = (MyActionbar) findViewById(R.id.myActionbar);
        actionbar.initActionbar(true, R.string.disclaimer_of_liability, 0, 0);

        mWebView = (WebView) findViewById(R.id.webViewDisclaimer);
        initWebView();
        mWebView.setWebChromeClient(new ReWebChomeClient(this, mProgressDialog) {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                actionbar.setTitle(title);
                super.onReceivedTitle(view, title);
            }
        });

        actionbar.setLeftTextCLickListener(view -> {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
        });
    }

    private void setData() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                String url = intent.getStringExtra(URL);
                mUrl = (url == null ? mUrl : url);
                mUrl = ReWebViewClient.modifyUrl(mUrl);
            }
            mWebView.loadUrl(mUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
