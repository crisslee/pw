package com.coomix.app.all.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.coomix.app.all.Constant;
import com.coomix.app.all.ui.activity.CommActDetailActivity;
import com.coomix.app.all.ui.activity.CommActListActivity;

/**
 * Created by ShiShengLong on 2016/11/4.
 */

public class NativeH5Interactive {
    private final String FUNC_TITLE = "gmapp:func=";
    private final String ID = "id=";
    private final String CITYCODE = "citycode=";

    /**
     * 跳转到活动列表
     */
    private final String FUNC_ACTICITY_LIST = "gmapp:func=activitylist";
    /**
     * 跳转到活动详情
     */
    private final String FUNC_ACTIVITY_DETAIL = "gmapp:func=activitydetail";
    /**
     * 跳转到登录
     */
    private final String FUNC_GM_LOGIN = "gmapp:func=gmlogin";

    // 跳转到拨打电话
    private final String FUNC_PHONE = "tel:";

    /**
     * 解析携带的数据。格式如：gmapp:func=personhome?id=507232&citycode=860515
     */
    public boolean judgeUrlAndJump(Context context, String url) {
        return judgeUrlAndJump(context, url, false);
    }

    public boolean judgeUrlAndJump(Context context, String url, boolean ifNotMatchToWelcome) {
        if (CommunityUtil.isEmptyTrimStringOrNull(url)) {
            return false;
        }
        String id = "";
        String cityCode = "";

        if (url.contains(ID)) {
            id = url.substring(url.indexOf(ID) + ID.length());
            if (id.contains("&")) {
                //防止有错误的url而出错
                id = id.substring(0, id.indexOf("&"));
            }
        }
        if (url.contains(CITYCODE)) {
            cityCode = url.substring(url.indexOf(CITYCODE) + CITYCODE.length());
            if (cityCode.contains("&")) {
                //防止后面还接的有其他参数
                cityCode = cityCode.substring(0, cityCode.indexOf("&"));
            }
        }

        if (url.contains(FUNC_ACTICITY_LIST)) {
            context.startActivity(new Intent(context, CommActListActivity.class));
        } else if (url.contains(FUNC_ACTIVITY_DETAIL)) {
            Intent intent = new Intent(context, CommActDetailActivity.class);
            intent.putExtra(Constant.NATIVE_ACTIVITY_ID, id);
            context.startActivity(intent);
        } else if (url.contains(FUNC_PHONE)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } else {
            return false;
        }

        return true;
    }
}
