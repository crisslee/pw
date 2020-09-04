package com.coomix.app.all.ui.web;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.ui.boot.BootActivity;
import com.coomix.app.all.ui.main.MainActivityParent;
import com.coomix.app.all.manager.SettingDataManager;

/**
 * 一般用于分享
 * 从Web界面点击按钮后携带数据，类似：goocarapp://?gmapp:func=activitylist?id=0&citycode=860515
 * gmapp:func=activitylist?id=0&citycode=860515和WebView中跳转原生一样
 * 3.6.0 版本只启动App，不跳往指定界面
 *
 * Created by shishenglong on 2016/11/21.
 */
public class WebAgentActivity extends BaseActivity {
    //Web跳转时候携带的需要跳转的界面信息已经用到的数据信息
    public static String funcNameAndData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            funcNameAndData = getIntent().getDataString();
        }

        if (MainActivityParent.isAlive && !TextUtils.isEmpty(funcNameAndData)) {
            // //App已经启动直接跳转
            // NativeH5Interactive nativeH5Interactive = new NativeH5Interactive();
            // nativeH5Interactive.judgeUrlAndJump(WebAgentActivity.this, funcNameAndData, true);
            // funcNameAndData = null;

            //mainactivity是singletask模式，如果已经启动则不会产生新的实例
            //startActivity(new Intent(this, TabActionActivity.class));
            SettingDataManager.getInstance(this).goToMainByMap(this, null);
            finish();
            return;
        }

        //App没有启动，则重置状态。走到MainActivity中再跳转处理funcNameAndData
        Intent intent = new Intent(this, BootActivity.class);
        //intent.putExtra(BootActivity.SHOW_AD, false);
        startActivity(intent);
        finish();
    }
}
