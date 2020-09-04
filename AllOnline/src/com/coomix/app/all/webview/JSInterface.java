package com.coomix.app.all.webview;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.dialog.ProgressDialogEx;
import com.coomix.app.all.ui.activity.CommActDetailActivity;
import com.coomix.app.all.ui.activity.CommActListActivity;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.service.Error;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.share.UmengShareUtils;
import com.coomix.app.all.dialog.AskDialog;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.all.ui.login.LoginActivity;
import com.coomix.app.pay.CoomixPayManager;
import com.coomix.app.pay.CoomixPayRsp;
import com.coomix.app.pay.GoomePayRsp;
import com.coomix.app.pay.ICoomixPay;
import com.coomix.app.pay.PayResultManager;
import com.coomix.app.pay.WechatPayManager;
import com.coomix.app.all.util.CommunityUtil;

import org.json.JSONObject;

/**
 * Created by ssl on 2017/7/7.
 */

public class JSInterface implements ServiceAdapter.ServiceAdapterCallback
{
    //CMD就是对应下面的FUNC_XXX
    private final String KEY_FUNC = "cmd";
    //对应func调用后，回调给js的接口名
    private final String KEY_CALLBACK = "callback";
    //H5请求时候携带的参数
    private final String KEY_PARAM = "param";
    //GPS关闭时候的提示语
    private final String KEY_PARAM_GPS_TIP = "turn_on_gps_tip";
    //获取当前位置信息
    private final String FUNC_GET_LOCATION = "getlocation";
    //调起充值
    private final String FUNC_RECHARGE = "recharge";
    //登录页面
    private final String FUNC_GMLOGIN = "gmlogin";
    //web页面
    private final String FUNC_GMHTTP = "gmhttp";
    //活动列表
    private final String FUNC_ACTIVITYLIST = "activitylist";
    //活动详情
    private final String FUNC_ACTIVITYDETAIL = "activitydetail";
    //获取当前用户的信息（uid，name，avator， ticket， vtype，label）
    private final String FUNC_GET_CUR_USER_INFO = "getcuruserinfo";
    //到私聊
    private final String FUNC_PRIVATE_CHAT = "messagedetail";
    //退出
    private final String FUNC_GO_OUT = "goout";
    //直接分享，例如到微信，朋友圈
    private final String FUNC_SHARE_DIRECT = "shareDirect";


    private final int ERR_CODE_NOT_LOGIN = 9000;

    private WebView webView;
    private Activity curActivity;
    private ServiceAdapter serverController;
    private int iRechageForH5 = -1;
    private ProgressDialogEx progressDialogEx;
    private int iPayPlatform = -1;
    private String rechargeCallbackName;

    public JSInterface(Activity activity, WebView webView)
    {
        this.webView = webView;
        this.curActivity = activity;
    }

    private void showWaitInfoDialog()
    {
        progressDialogEx = new ProgressDialogEx(curActivity);
        progressDialogEx.setAutoDismiss(false);
        progressDialogEx.setCancelOnTouchOutside(false);

        try
        {
            progressDialogEx.setMessage(curActivity.getString(R.string.load_waiting));
            progressDialogEx.show();
        }
        catch (Exception e)
        {

        }
    }

    private void dismissWaitDialog()
    {
        if (progressDialogEx != null && progressDialogEx.isShowing())
        {
            progressDialogEx.dismiss();
        }
    }

    /**
     * jsonStr是H5中传输过来的.
     * 格式：
     * cmd:"getLocation", //原生的方法名
     * callback:"getLocationCallback" //H5中的方法名
     * */
    @JavascriptInterface
    public void call(final String jsonStr)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(jsonStr);
            String funcName = jsonObject.optString(KEY_FUNC, "");
            String callbackName = jsonObject.optString(KEY_CALLBACK, "");
            JSONObject param = null;
            if(jsonObject.has(KEY_PARAM))
            {
                param = jsonObject.optJSONObject(KEY_PARAM);
                if(param != null){
                    String fileMethodLine = "";
                    fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
                    fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
                    fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
                    GoomeLog.getInstance().logE(fileMethodLine," param: " + param.toString(), 0);
                }
            }
            if(!TextUtils.isEmpty(funcName))
            {
                methodByFuncName(funcName.toLowerCase(), callbackName, param);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 判断H5调用的接口，然后返回数据给H5相应的接口
    * */
    private void methodByFuncName(String funcName, String callbackName, JSONObject param)
    {
        if(funcName.equals(FUNC_GET_LOCATION))
        {
            //获取当前的位置信息
            getLocation(callbackName, param);
        }
        else if(funcName.equals(FUNC_GO_OUT))
        {
            ((FragmentActivity) curActivity).finish();
        }
        else if(funcName.equals(FUNC_RECHARGE))
        {
            //H5充值
            recharge(callbackName, param);
        }
        else if(funcName.equals(FUNC_GMLOGIN))
        {
            //登录页面
            goToLogin(callbackName, param);
        }
        else if(funcName.equals(FUNC_GMHTTP))
        {
            //web页面
            goToWebView(callbackName, param);
        }
        else if(funcName.equals(FUNC_ACTIVITYLIST))
        {
            //活动列表
            goToActivityList(callbackName, param);
        }
        else if(funcName.equals(FUNC_ACTIVITYDETAIL))
        {
            //活动详情
            goToActivityDetail(callbackName, param);
        }
        else if(funcName.equals(FUNC_GET_CUR_USER_INFO))
        {
            //获取当前用户的信息
            getCurUserInfo(callbackName, param);
        }
        else if(funcName.equalsIgnoreCase(FUNC_SHARE_DIRECT))
        {
            //分享到微信朋友圈
            shareDirectTo(callbackName, param);
        }
    }

    /**
     * 回调H5中的方法
     *
     *{
     data: {
     json
     },
     errcode: 0, // 0 成功，-1 错误失败
     msg: "OK", // 错误信息
     success: "true"
     }
     * */
    private void callbackH5(final String callbackName, JSONObject jsonData, int errcode, String msg, boolean success)
    {
        final JSONObject allJson = new JSONObject();
        try
        {
            allJson.put("errcode", errcode);
            allJson.put("msg", msg);
            allJson.put("success", success);
            allJson.put("data", jsonData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if(!TextUtils.isEmpty(callbackName))
        {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    String url = "javascript:" + callbackName + "('" + allJson.toString() + "')";
                    Log.i("Test", "---url: " + url);
                    webView.loadUrl(url);
                }
            });

        }
    }

    //获取当前位置并返回给H5
    private void getLocation(final String callbackName, JSONObject param)
    {
        final JSONObject jsonParams = new JSONObject();
        try
        {
            jsonParams.put("lng", AllOnlineApp.getCurrentLocation().getLongitude());
            jsonParams.put("lat", AllOnlineApp.getCurrentLocation().getLatitude());
            jsonParams.put("maptype", "GOOGLE");

            if(!CommunityUtil.isGPSOpened(curActivity))
            {
                //没有开启定位功能，提示定位，返回-1
                callbackH5(callbackName, jsonParams, -1, "", false);
                String msg = null;
                if(param != null && param.has(KEY_PARAM_GPS_TIP))
                {
                    msg = param.optString(KEY_PARAM_GPS_TIP, "");
                }
                if(TextUtils.isEmpty(msg))
                {
                    msg = curActivity.getString(R.string.h5_get_location_gps_off);
                }
                showAskGpsDialog(msg);
                return;
            }

            LatLng latLng = new LatLng(AllOnlineApp.getCurrentLocation().getLatitude(),
                    AllOnlineApp.getCurrentLocation().getLongitude());
            CommunityUtil.getAddressByLatLng(curActivity, latLng, new CommunityUtil.OnAddressCallBack()
            {
                @Override
                public void onAddressBack(PoiItem poiItem)
                {
                    try
                    {
                        jsonParams.put("name", poiItem.getTitle());
                        callbackH5(callbackName, jsonParams, 0, "OK", true);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        callbackH5(callbackName, jsonParams, -2, e.getMessage(), false);
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void showAskGpsDialog(String msg)
    {
        final AskDialog askGps = new AskDialog(curActivity);
        askGps.setShowMsg(msg);
        askGps.setNoText(R.string.redpacket_range_gps_no);
        askGps.setYesText(R.string.redpacket_range_gps_yes);
        askGps.setOnYesClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                askGps.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                curActivity.startActivity(intent);
            }
        });
        askGps.show();
    }

    //H5调起充值(一般是钻石)，包括相关的参数
    private void recharge(final String callbackName, final JSONObject param)
    {
        if(!CommonUtil.checkAndGoToLogin(curActivity))
        {
            //没登录去登录
            return;
        }

        if(param != null)
        {
            this.rechargeCallbackName = callbackName;
            int iAmount = param.optInt("amount", 0);
            iPayPlatform = param.optInt("platform", 0);// 1:微信，2：支付宝，3：零钱
            //int iType = param.optInt("type", 0); // 0:零钱充值 ， 1:酷币充值(android), 3：钻石
            serverController = ServiceAdapter.getInstance(curActivity);
            serverController.registerServiceCallBack(this);

            showWaitInfoDialog();
            int pay_manner = 0;
            if(iPayPlatform == CoomixPayManager.PAY_WECHAT)
            {
                pay_manner = WechatPayManager.WECHAT_PAY_APP;
            }
//            else if(iPayPlatform == CoomixPayManager.PAY_ALI)
//            {
//                pay_manner = AliPayManager.ALI_PAY_APP;
//            }
            else if(iPayPlatform == CoomixPayManager.PAY_GOOME)
            {
                pay_manner = GoomePayRsp.POCKET_PAY_APP;
            }
            else
            {
                //无法识别的支付渠道，不处理
                return;
            }
            iRechageForH5 = serverController.rechargeBalance(this.hashCode(), CommonUtil.getTicket(), iAmount, iPayPlatform,
                    pay_manner);
        }
    }

    /**
     * 充值后的回调
     * */
    private void callbackRechage(int errcode, String msg, boolean status, JSONObject jsonObject)
    {
        callbackH5(rechargeCallbackName, jsonObject, errcode, msg, status);
    }

    @Override
    public void callback(int messageid, Result result)
    {
        if (result.statusCode == Result.ERROR_NETWORK)
        {
            dismissWaitDialog();
            Toast.makeText(curActivity, R.string.network_error, Toast.LENGTH_SHORT).show();
        }
        else if(iRechageForH5 == messageid)
        {
            dismissWaitDialog();
            if (result.success)
            {
                if (result.mResult != null && result.mResult instanceof CoomixPayRsp)
                {
                    CoomixPayRsp payRsp = (CoomixPayRsp) result.mResult;
                    final JSONObject jsonObject = new JSONObject();
                    try
                    {
                        jsonObject.put("order_id", payRsp.getOrder_id());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if(payRsp.getError() != null &&  payRsp.getError().getCode() == Error.ERRCODE_BALANCE_INSUFFICIENT)
                    {
                         //余额不足
                        callbackRechage(-2, payRsp.getError().getMsg(), false, null);
                    }
                    else
                    {
                        if (iPayPlatform == CoomixPayManager.PAY_GOOME)
                        {
                            Toast.makeText(curActivity, curActivity.getString(R.string.recharge_success), Toast.LENGTH_SHORT).show();
                            callbackRechage(0, result.msg, true, jsonObject);
                        }
                        else
                        {
                            //第三方支付，发起支付
                            CoomixPayManager coomixPayManager = new CoomixPayManager(curActivity, iPayPlatform);
                            PayResultManager.getInstance().setPayOrderType(ICoomixPay.ORDER_FROM.FROM_GAME_H5);
                            PayResultManager.getInstance().setOnPayResultCallback(new PayResultManager.OnPayResultCallback()
                            {
                                @Override
                                public void onPayResult(boolean bOk, String msg, int code)
                                {
                                    PayResultManager.getInstance().setOnPayResultCallback(null);
                                    callbackRechage(code, msg, bOk, jsonObject);
                                }
                            });

                            coomixPayManager.pay(payRsp);
                        }
                    }
                }
                else
                {
                    callbackRechage(-1, "Error:预支付信息返回为空", false, null);
                }
            }
            else
            {
                callbackRechage(-1, result.msg, false, null);
                Toast.makeText(curActivity, curActivity.getString(R.string.withdraw_network_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //登录页面
    private void goToLogin(final String callbackName, final JSONObject param) {
        if (!CommonUtil.isLogin()) {
            curActivity.startActivity(new Intent(curActivity, LoginActivity.class));
            callbackH5(callbackName, null, 0, "OK", true);
        } else {
            Toast.makeText(curActivity, R.string.has_login, Toast.LENGTH_SHORT).show();
            callbackH5(callbackName, null, ERR_CODE_NOT_LOGIN, "ERROR: has not login", false);
        }
    }

    //web页面
    private void goToWebView(final String callbackName, final JSONObject param)
    {
        int bShare = 0;
        String url = null;
        if(param != null)
        {
            //是否可分享
            bShare = param.optInt("share", 0);
            url = param.optString("url", "");
        }
        if(!TextUtils.isEmpty(url))
        {
            CommunityUtil.switch2WebViewActivity(curActivity, url, bShare==1, "");
        }
        callbackH5(callbackName, null, 0, "OK", true);
    }

    //活动列表
    private void goToActivityList(final String callbackName, final JSONObject param)
    {
        curActivity.startActivity(new Intent(curActivity, CommActListActivity.class));
        callbackH5(callbackName, null, 0, "OK", true);
    }

    //活动详情
    private void goToActivityDetail(final String callbackName, final JSONObject param)
    {
        int id = 0;
        if(param != null)
        {
            id = param.optInt("id", 0);
        }
        if(id > 0)
        {
            Intent intent = new Intent(curActivity, CommActDetailActivity.class);
            intent.putExtra(Constant.NATIVE_ACTIVITY_ID, id);
            curActivity.startActivity(intent);
            callbackH5(callbackName, null, 0, "OK", true);
        }
        else
        {
            callbackH5(callbackName, null, -1, "ERROR: id error", false);
        }
    }

    //获取当前用户的信息uid，name，avator， ticket， vtype，label
    private void getCurUserInfo(final String callbackName, final JSONObject param)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("uid", AllOnlineApp.sToken.community_id);
            jsonObject.put("name", AllOnlineApp.sToken.name);
            jsonObject.put("avator", "");
            jsonObject.put("ticket", CommonUtil.getTicket());
            //jsonObject.put("vtype", user.getVtype());
            jsonObject.put("label", "");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            callbackH5(callbackName, jsonObject, -1, e.getMessage(), false);
            return;
        }
        callbackH5(callbackName, jsonObject, 0, "OK", true);
    }

    //分享到微信朋友圈
    private void shareDirectTo(final String callbackName, final JSONObject param)
    {
        String title = "";
        String content = "";
        String imageUrl = "";
        String shareUrl = null;
        int iType = 0;
        if(param != null)
        {
            title = param.optString("title", ""); //标题
            content = param.optString("content", "");//描述
            imageUrl = param.optString("image_url", "");//图片url
            shareUrl = param.optString("share_url", "");//分享url
            iType = param.optInt("type", 0);//类型。0微信，1朋友圈
        }

        UmengShareUtils.shareDirectTo(curActivity, title, content, imageUrl, shareUrl, iType);

        if(!TextUtils.isEmpty(shareUrl))
        {
            callbackH5(callbackName, null, 0, "OK", true);
        }
        else
        {
            callbackH5(callbackName, null, -1, "ERROR: shareUrl" + shareUrl, false);
        }
    }

    public void release()
    {
        if(serverController != null)
        {
            serverController.unregisterServiceCallBack(this);
            serverController = null;
        }
    }
}
