package com.coomix.app.all.webview;

import android.net.Uri;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import com.coomix.app.all.dialog.ProgressDialogEx;

public class ReWebChomeClient extends WebChromeClient {

    private OpenFileChooserCallBack mOpenFileChooserCallBack;
    private ProgressDialogEx mProgressDialog;

    public ReWebChomeClient() {
    }

    public ReWebChomeClient(OpenFileChooserCallBack openFileChooserCallBack, ProgressDialogEx progressDialog) {
        mOpenFileChooserCallBack = openFileChooserCallBack;
        mProgressDialog = progressDialog;
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
        super.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        mOpenFileChooserCallBack.openFileChooserCallBack(uploadMsg, acceptType);
    }

    // For Android < 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, "");
    }

    // For Android > 4.1.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg,
        String acceptType, String capture) {
        openFileChooser(uploadMsg, acceptType);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (newProgress >= 100) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
        super.onProgressChanged(view, newProgress);
    }

    // // For Android > 5.0
    // public boolean onShowFileChooser (WebView webView, ValueCallback<Uri[]>
    // uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
    // mOpenFileChooserCallBack.openFileChooserImplForAndroid5(uploadMsg);
    // return true;
    // }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        super.onReceivedTitle(view, title);
    }

    public interface OpenFileChooserCallBack {
        void openFileChooserCallBack(ValueCallback<Uri> uploadMsg,
            String acceptType);
        // void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg);
    }
}
