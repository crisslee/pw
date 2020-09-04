package com.coomix.app.all.ui.web;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.Toast;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.share.PopupWindowUtil;
import com.coomix.app.all.share.TextSet;
import com.coomix.app.all.webview.ImageUtil;
import com.coomix.app.all.webview.JSInterface;
import com.coomix.app.all.webview.ReWebChomeClient;
import com.coomix.app.all.webview.ReWebViewClient;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.util.CommunityPictureUtil;
import com.coomix.app.all.util.MulThreadDownload;
import com.coomix.app.all.util.MulThreadDownload.DownloadCallBack;
import java.io.File;
import java.lang.reflect.Method;

/**
 * 带webview的activity<br/>
 * webview可访问相机及相册
 *
 * @author 刘生健
 * @since 2016-4-10 下午03:53:54
 */
public class CommunityWebViewActivity extends BaseActivity implements ReWebChomeClient.OpenFileChooserCallBack {
    private static final String TAG = CommunityWebViewActivity.class.getSimpleName();
    public static final String PARAMS_NEED_LOGIN = "gmlogin=1";
    protected static final int REQUEST_CODE_PICK_IMAGE = 0;
    protected WebView mWebView;
    protected Intent mSourceIntent;
    protected ValueCallback<Uri> mUploadMsg;
    protected ProgressDialogEx mProgressDialog;
    protected JSInterface jsInterface;

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            if (mWebView == null) {
                return;
            }
            mWebView.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 延迟三秒，保证zoomButton消失
                    // 解决Receiver not registered:
                    // android.widget.ZoomButtonsController$1@354d07b7
                    if (mWebView != null) {
                        try {
                            mWebView.destroy();
                            mWebView = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initWebView() {
        mProgressDialog = ProgressDialogEx.show(CommunityWebViewActivity.this, "",
            getString(R.string.community_loading), true, Constant.DIALOG_AUTO_DISMISS,
            new ProgressDialogEx.OnCancelListener2() {
                @Override
                public void onCancel(DialogInterface dialog) {
                }

                @Override
                public void onAutoCancel(DialogInterface dialog) {
                }
            });
        mWebView.setInitialScale(80);

        mWebView.setScrollbarFadingEnabled(true);

        mWebView.setWebViewClient(new ReWebViewClient());
        mWebView.setWebChromeClient(new ReWebChomeClient(this, mProgressDialog));

        // 设定编码格式
        mWebView.getSettings().setDefaultTextEncodingName("UTF-8");

        WebSettings settings = mWebView.getSettings();
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
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
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowContentAccess(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            setMixedContentMode(settings);
            CookieManager cm = CookieManager.getInstance();
            setAcceptThirdPartyCookies(cm);
        }

        jsInterface = new JSInterface(this, mWebView);
        mWebView.addJavascriptInterface(jsInterface, "native");

        fixDirPath();
    }

    private void setMixedContentMode(WebSettings settings) {
        try {
            Class cls = settings.getClass();
            Method method = cls.getDeclaredMethod("setMixedContentMode", int.class);
            method.setAccessible(true);
            method.invoke(settings, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAcceptThirdPartyCookies(CookieManager cm) {
        try {
            Class cls = cm.getClass();
            Method method = cls.getDeclaredMethod("setAcceptThirdPartyCookies", mWebView.getClass(), boolean.class);
            method.setAccessible(true);
            method.invoke(cm, mWebView, true);
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE) {
                Uri result = data == null ? null : data.getData();
                if (mUploadMsg != null) {
                    mUploadMsg.onReceiveValue(result);
                    mUploadMsg = null;
                }
            }
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_PICK_IMAGE: {
                try {
                    if (mUploadMsg == null) {
                        return;
                    }
                    String sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);
                    if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {
                        System.out.println("您没有选择图片或图片不存在");
                        break;
                    }
                    Uri uri = Uri.fromFile(new File(sourcePath));
                    mUploadMsg.onReceiveValue(uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType) {
        mUploadMsg = uploadMsg;
        mSourceIntent = ImageUtil.choosePicture();
        startActivityForResult(mSourceIntent, REQUEST_CODE_PICK_IMAGE);
    }

    private void fixDirPath() {
        String path = ImageUtil.getDirPath();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    protected void addOnLongClickListener() {
        mWebView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 长按事件监听（注意：需要实现LongClickCallBack接口并传入对象）
                try {
                    HitTestResult htr = mWebView.getHitTestResult();// 获取所点击的内容
                    if (htr.getType() == WebView.HitTestResult.IMAGE_TYPE) {// 判断被点击的类型为图片
                        final String urlStr = htr.getExtra();
                        String name = urlStr.substring(urlStr.lastIndexOf(File.separator) + 1);
                        if (name.startsWith("image")) {
                            // 与web端约定以img开头的图片才能进行保存，其它图片不能保存
                            TextSet setItem1 = new TextSet(R.string.save_pictrue, false, new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 先读取webview缓存文件
                                    boolean isSaved = false;
                                    if (!isSaved) { // 复制webview缓存图片失败，则从网络下载
                                        downloadImg(urlStr);
                                    }
                                }
                            });
                            PopupWindowUtil.showPopWindow(CommunityWebViewActivity.this, mWebView,
                                R.string.save_picture_hint, setItem1, null, true);
                            return false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    private void downloadImg(final String urlStr) {
        new MulThreadDownload().download(urlStr, CommunityPictureUtil.getSavedDir(),
            "Coomix_" + System.currentTimeMillis(), 1, new DownloadCallBack() {
                @Override
                public void totalSize(long total) {
                }

                @Override
                public void progress(long total, long size) {
                }

                @Override
                public void error() {
                }

                @Override
                public void contentLengthError() {
                }

                @Override
                public void complete(String path) {
                    Uri uri = Uri.fromFile(new File(path));
                    if (path != null) {
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(uri);
                        sendBroadcast(intent);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CommunityWebViewActivity.this, R.string.save_picture_success,
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
    }
}
