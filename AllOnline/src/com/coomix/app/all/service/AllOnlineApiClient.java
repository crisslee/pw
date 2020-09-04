package com.coomix.app.all.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;
import com.baidu.location.BDLocation;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.log.AppConfigs;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.log.GoomeLogUtil;
import com.coomix.app.all.log.LogUploadInfo;
import com.coomix.app.all.manager.DomainManager;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.model.bean.ActCommitItem;
import com.coomix.app.all.model.bean.ActOrderInfo;
import com.coomix.app.all.model.bean.Adver;
import com.coomix.app.all.model.bean.Alarm;
import com.coomix.app.all.model.bean.AreaFence;
import com.coomix.app.all.model.bean.AuthPages;
import com.coomix.app.all.model.bean.BalanceInfo;
import com.coomix.app.all.model.bean.Blacklist;
import com.coomix.app.all.model.bean.City;
import com.coomix.app.all.model.bean.Command;
import com.coomix.app.all.model.bean.CommitExtendItem;
import com.coomix.app.all.model.bean.CommunityActDetail;
import com.coomix.app.all.model.bean.CommunityActs;
import com.coomix.app.all.model.bean.CommunitySignedInfo;
import com.coomix.app.all.model.bean.DeviceSetting;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.all.model.bean.MyActivities;
import com.coomix.app.all.model.bean.Overspeed;
import com.coomix.app.all.model.bean.Paint;
import com.coomix.app.all.model.bean.PeopleForActRect;
import com.coomix.app.all.model.bean.Province;
import com.coomix.app.all.model.bean.PushAdv;
import com.coomix.app.all.model.bean.RespDomainAdd;
import com.coomix.app.all.model.bean.Token;
import com.coomix.app.all.model.bean.TrackPoint;
import com.coomix.app.all.model.bean.TrackPoints;
import com.coomix.app.all.model.response.CommunityImageInfo;
import com.coomix.app.all.model.response.CommunityUser;
import com.coomix.app.all.ui.update.GoomeUpdateInfo;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.CoordinateUtil;
import com.coomix.app.all.util.ExperienceUserUtil;
import com.coomix.app.all.util.Md5Util;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.framework.app.BaseApiClient;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.NetworkUtil;
import com.coomix.app.framework.util.OSUtil;
import com.coomix.app.framework.util.PhoneInfo;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.pay.CoomixPayRsp;
import com.coomix.app.pay.OrderStatusRsp;
import com.coomix.app.redpacket.util.CreateRedPacketInfo;
import com.coomix.app.redpacket.util.RedPacketConfig;
import com.coomix.app.redpacket.util.RedPacketInfo;
import com.coomix.app.redpacket.util.RpLocationUtil;
import com.coomix.app.redpacket.util.UnsendRedPackets;
import com.coomix.security.Security;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tencent.tinker.lib.tinker.Tinker;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

public class AllOnlineApiClient extends BaseApiClient {
    protected Context mContext;
    private Security security;

    public AllOnlineApiClient(Context context) {
        super(context);
        mContext = context;
        security = new Security();
        PreferenceUtil.init(mContext);
    }

    // qq登录接口：
    public Result qQHttpLogin(String nickname, String openid) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        String server = DomainManager.sRespDomainAdd.domainMain;
        try {
            requestUrl = "/1/auth/access_token?method=qq_connect" + "&nickname=" + Uri.encode(nickname, "UTF-8") + "&openid=" + openid;
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("qQHttpLogin------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;

                    Token token = ExperienceUserUtil.userJsonToToken(json);
                    result.mResult = token;
                    ExperienceUserUtil.saveUserJsonToSp(mContext, content);
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                    String fileMethodLine = "";
                    fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
                    fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
                    fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
                    GoomeLog.getInstance().logE(fileMethodLine, " SERVER: " + server + "URL:  " + result.debugUrl + " statusCode: " + result.statusCode + "errorMessage: " + result.errorMessage + "content: " + content, 0);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 密码加密
     *
     * @param sSrc
     * @return
     */
    public static String encrypt(String sSrc) {
        StringBuilder sb = new StringBuilder();
        int length = sSrc.length();
        for (int i = 0; i < length; i++) {
            byte c = (byte) (sSrc.charAt(i) ^ 20000);
            sb.append((char) c);
        }
        return sb.toString();
    }

    /**
     * 用户体验
     *
     * @return
     */
    public Result experienceQQUser(String account) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/auth/access_token?account=" + Uri.encode(account, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("experienceQQUser------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    Token token = ExperienceUserUtil.userJsonToToken(json);
                    result.mResult = token;
                    ExperienceUserUtil.saveUserJsonToSp(mContext, content);
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                    String fileMethodLine = "";
                    fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
                    fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
                    fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
                    GoomeLog.getInstance().logE(fileMethodLine, " SERVER: " + server + "URL:  " + result.debugUrl + " statusCode: " + result.statusCode + "errorMessage: " + result.errorMessage + "content: " + content, 0);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    // 解除qq绑定登录
    public Result unBindQQCount(String openid, String account, String token) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/auth/access_token?method=qq_unbind&openid=" + openid + "&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("unBindQQCount------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                    String fileMethodLine = "";
                    fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
                    fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
                    fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
                    GoomeLog.getInstance().logE(fileMethodLine, " SERVER: " + server + "URL:  " + result.debugUrl + " statusCode: " + result.statusCode + "errorMessage: " + result.errorMessage + "content: " + content, 0);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    // qq绑定登录
    public Result bindQQCount(String account, String password, String nickname, String openid) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String signaturePwd = encrypt(password);
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/auth/access_token?method=bind_account&pwd=" + Uri.encode(signaturePwd, "UTF-8") + "&nickname=" + Uri.encode(nickname, "UTF-8") + "&openid=" + openid + "&account=" + Uri.encode(account, "UTF-8");

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("bindQQCount------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    result.statusCode = Result.OK;
                    Token token = ExperienceUserUtil.userJsonToToken(json);
                    result.mResult = token;
                    ExperienceUserUtil.saveUserJsonToSp(mContext, content);
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                    String fileMethodLine = "";
                    fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
                    fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
                    fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
                    GoomeLog.getInstance().logE(fileMethodLine, " SERVER: " + server + "URL:  " + result.debugUrl + " statusCode: " + result.statusCode + "errorMessage: " + result.errorMessage + "content: " + content, 0);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;

    }

    // FORGET PASSWORD INTERFACE
    public Result forgetBindPhoneSms(String token, String phoneNum, String account) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/modify_pwd?method=get_phone_captcha&phone_num=" + phoneNum + "&access_token=" + Uri.encode(token, "UTF-8") + "&account=" + Uri.encode(account, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("forgetBindPhoneSms------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result forgetPwBind(String token, String phoneNum, String smsCode, String account) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/modify_pwd?method=bind_account&phone_num=" + phoneNum + "&captcha=" + Uri.encode(smsCode, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&account=" + Uri.encode(account, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("forgetPwBind------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result forgetPwValidate(String account) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/modify_pwd?method=get_bind_captcha&account=" + Uri.encode(account, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("forgetPwValidate------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result forgetPwReset(String validateNum, String newPw, String account) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String signatureNewPw = encrypt(newPw);
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/modify_pwd?method=set_pwd&captcha=" + validateNum + "&pwd=" + Uri.encode(signatureNewPw, "UTF-8") + "&account=" + Uri.encode(account, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("forgetPwReset------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    Token token = new Token();
                    JSONObject data = json.optJSONObject("data");
                    token.access_token = data.optString("access_token");
                    if (!TextUtils.isEmpty(token.access_token)) {
                        result.mResult = token;
                    }
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result readFirstHost(String domain, boolean... isIp)
    {
        Result result = new Result();
        result.statusCode = Result.FAIL;
        String requestUrl = null;
        String content = null;
        try
        {
            //http://www.gpsoo.net/appserverlist
            boolean userIP = false;
            if (isIp != null && isIp.length > 0 && isIp[0])
            {
                userIP = true;
                String defaultIp = getDefaultIp(domain);
                if (defaultIp != null)
                {
                    requestUrl = getDN(false, defaultIp) + "/appserverlist";
                }
                else
                {
                    requestUrl = getDN(false, domain) + "/appserverlist";
                }
            }
            else
            {
                requestUrl = getDN(false, domain) + "/appserverlist";
            }
            if (Constant.IS_DEBUG_MODE)
            {
                System.out.println("readFirstHost------" + requestUrl);
            }
            boolean bWap = false;
            if (NetworkUtil.isWap(mContext) > 0)
            {
                bWap = true;
            }
            else
            {
                bWap = false;
            }
            //最多尝试三次请求
            HttpResponse resp = null;
            for(int i = 0; i < 3; i++)
            {
                resp = excute(mContext, requestUrl, domain, false, getHttpConnectionTimeout(i, bWap), getHttpSOTimeout(i, 0, bWap), null);
                if (resp != null && resp.getStatusLine() != null && resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                {
                    break;
                }
            }
            if (resp != null && resp.getStatusLine() != null)
            {
                result.statusCode = resp.getStatusLine().getStatusCode();
                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                {
                    InputStream is = getUngzippedContent(resp.getEntity());
                    content = convertStreamToString(is);
                    JSONObject json = new JSONObject(content);
                    if (json != null)
                    {
                        result.statusCode = Result.OK;
                        RespDomainAdd mRespDomainAdd = ExperienceUserUtil.jsonToDomainAdd(json);
                        if (mRespDomainAdd != null)
                        {
                            PreferenceUtil.commitString(Constant.PREFERENCE_DOMAINS, content);
                            if (!StringUtil.isTrimEmpty(mRespDomainAdd.backupIp))
                            {
                                PreferenceUtil.commitString(Constant.PREFERENCE_DOMAIN_IP, mRespDomainAdd.backupIp);
                            }
                        }
                        result.mResult = mRespDomainAdd;
                    }
                }
                else
                {
                    String bip = getFirstIp();
                    if (StringUtil.isTrimEmpty(bip))
                    {
                        return readFirstHost(bip, !userIP);
                    }
                }
            }
        }
        catch (Exception e)
        {
            processException(result, e);
            e.printStackTrace();
        }
        return result;
    }

    // 登录接口
    public Result accessToken(String account, String password, String login_type, String alias, String push_type, long time) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        long current = time;
        String signature = OSUtil.toMD5(OSUtil.toMD5(password) + current);
        String channelId = AllOnlineApp.sChannelID;
        int timeZone = Constant.getTimeZoon();

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        // source=app1 //新版本app使用 告诉新版本的app 消息推送使用的是GPNS
        try {
            /*
             * 同时使用jpush和gpns双通道推送
             */
            if (StringUtil.isTrimEmpty(channelId)) {
                requestUrl = "/1/auth/access_token?access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&signature=" + signature + "&platform=android" + "&alias=" + alias + "&push=" + push_type + "&timezone=" + timeZone + "&time=" + current;
            } else {
                requestUrl = "/1/auth/access_token?access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&signature=" + signature + "&platform=android" + "&alias=" + alias + "&push=" + push_type + "&timezone=" + timeZone + "&channelid=" + channelId + "&time=" + current;
            }

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("accessToken------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, false, current, false, null, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;

                    Token token = ExperienceUserUtil.userJsonToToken(json);
                    result.mResult = token;
                    ExperienceUserUtil.saveUserJsonToSp(mContext, content);
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");

                    if (TextUtils.isEmpty(result.errorMessage)) {
                        result.errorMessage = result.statusCode + "";
                    }
                    if (TextUtils.isEmpty(result.statusCode + "") && TextUtils.isEmpty(result.errorMessage)) {
                        result.errorMessage = "Unknown error";
                    }
                    String fileMethodLine = "";
                    fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
                    fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
                    fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
                    GoomeLog.getInstance().logE(fileMethodLine, " SERVER: " + server + "URL:  " + result.debugUrl + " statusCode: " + result.statusCode + "errorMessage: " + result.errorMessage + "content: " + content, 0);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result monitor(String target, String account, String token, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/account/monitor?access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&map_type=" + mapType + "&target=" + Uri.encode(target) + "&access_token=" + Uri.encode(token, "UTF-8");

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("monitor------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
//                    ArrayList<DeviceState> devices = new ArrayList<DeviceState>();
//                    JSONArray data = json.optJSONArray("data");
//                    if (data != null) {
//                        int length = data.length();
//                        for (int i = 0; i < length; i++) {
//                            JSONObject tmp = data.optJSONObject(i);
//                            DeviceState device = new DeviceState();
//                            device.course = tmp.optInt("course");
//                            device.device_info = tmp.optInt("device_info");
//                            device.gps_time = tmp.optLong("gps_time");
//                            device.heart_time = tmp.optLong("heart_time");
//                            device.imei = tmp.optString("imei");
//                            device.lat = tmp.optDouble("lat", 0.0);
//                            device.lng = tmp.optDouble("lng", 0.0);
//                            device.acc = tmp.optInt("acc");
//                            device.server_time = tmp.optLong("server_time");
//                            device.speed = tmp.optInt("speed");
//                            device.sys_time = tmp.optLong("sys_time");
//                            device.device_info_new = tmp.optInt("device_info_new"); // 获取设置的状态
//                            device.seconds = tmp.optLong("seconds");// 离线或者是静止时长
//                            device.acc_seconds = tmp.optLong("acc_seconds"); // ACC开启/熄火时长
//                            device.power = tmp.optString("power");
//                            device.locationType = tmp.optString("location");
//                            device.voice_status = tmp.optInt("voice_status");
//                            device.voice_gid = tmp.optLong("voice_gid");
//
//                            devices.add(device);
//                        }
//                    }
//                    result.mResult = devices;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result tracking(String[] imeis, String account, String token, String mapType) {
        Result result = new Result();
        StringBuilder requestUrl = new StringBuilder("/1/devices/tracking?");
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;

        try {
            StringBuilder sb = new StringBuilder();
            if (imeis.length > 1) {
                for (int i = 0; i < imeis.length - 1; i++) {
                    sb.append(imeis[i]).append(",");
                }
                sb.append(imeis[imeis.length - 1]);
            } else {
                sb.append(imeis[0]);
            }
            requestUrl.append("imeis=").append(sb.toString());
            requestUrl.append("&map_type=").append(mapType);
            requestUrl.append("&access_token=").append(Uri.encode(token, "UTF-8"));
            requestUrl.append("&account=").append(Uri.encode(account, "UTF-8"));
            requestUrl.append("&ver=").append(BaseApiClient.COMMUNITY_COOMIX_VERSION);

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, ">>> tracking >>>");
            }
            content = httpToServerRequestOrUpData(requestUrl.toString(), server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    //ArrayList<DeviceState> devices = new ArrayList<DeviceState>();
                    //JSONArray data = json.optJSONArray("data");
                    //if (data != null) {
                    //    int length = data.length();
                    //    for (int i = 0; i < length; i++) {
                    //        JSONObject tmp = data.optJSONObject(i);
                    //        DeviceState device = new DeviceState();
                    //        device.course = tmp.optInt("course");
                    //        device.device_info = tmp.optInt("device_info");
                    //        device.device_info_new = tmp.optInt("device_info_new");
                    //        device.gps_time = tmp.optLong("gps_time");
                    //        device.heart_time = tmp.optLong("heart_time");
                    //        device.imei = tmp.optString("imei");
                    //        device.lat = tmp.optDouble("lat", 0.0);
                    //        device.lng = tmp.optDouble("lng", 0.0);
                    //        device.server_time = tmp.optLong("server_time");
                    //        device.speed = tmp.optInt("speed");
                    //        device.acc = tmp.optInt("acc");
                    //        device.sys_time = tmp.optLong("sys_time");
                    //        device.seconds = tmp.optLong("seconds");
                    //        device.acc_seconds = tmp.optLong("acc_seconds"); // ACC开启/熄火时长
                    //        device.power = tmp.optString("power");
                    //        device.locationType = tmp.optString("location");
                    //
                    //        devices.add(device);
                    //    }
                    //}
                    //result.mResult = devices;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result history(String token, String imei, String account, long begin_time, long end_time, long cur_time, String mapType, int limit) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;

        try {
            requestUrl = "/1/devices/history?access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&begin_time=" + begin_time + "&end_time=" + end_time + "&access_token=" + Uri.encode(token, "UTF-8") + "&limit=" + limit + "&imei=" + imei + "&time=" + cur_time + "&map_type=" + mapType;
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("history------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    // 1.9.5版本修改格式， 之前data中存在的是array，1.9.5开始存放object, 原array数据存入pos中
                    TrackPoints trackPoints = new TrackPoints();
                    JSONObject data = json.optJSONObject("data");
                    if (data != null) {
                        JSONArray array = data.optJSONArray("pos");
                        if (array != null) {
                            ArrayList<TrackPoint> points = new ArrayList<TrackPoint>();
                            int length = array.length();
                            for (int i = 0; i < length; i++) {
                                JSONObject tmp = array.optJSONObject(i);
                                TrackPoint point = new TrackPoint();
                                point.course = tmp.optInt("course");
                                point.gps_time = tmp.optLong("gps_time");
                                point.lat = tmp.optDouble("lat", 0.0);
                                point.lng = tmp.optDouble("lng", 0.0);
                                point.speed = tmp.optInt("speed");
                                points.add(point);
                            }
                            trackPoints.setTrackPoints(points);
                        }
                        trackPoints.setResEndTime(data.optLong("resEndTime", 0l));
                    }
                    result.mResult = trackPoints;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    // 谷歌你地址解析
    public Result reverseGeo(String token, double lng, double lat, String account, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        boolean isChinaFlag = CommonUtil.isChina();

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            if (!isChinaFlag) {
                //return reverseGeoGoogle(lng, lat);
            }
            requestUrl = "/1/tool/textAddress?access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&map_type=" + mapType + "&access_token=" + Uri.encode(token, "UTF-8") + "&lng=" + lng + "&lat=" + lat;

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("setAddressText------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    JSONObject data = json.optJSONObject("data");
                    String address = data.optString("textAddress");
                    if (!StringUtil.isTrimEmpty(address)) {
                        result.mResult = address;
                    } else {
                        if (mapType.equals(Constant.MAP_BAIDU)) {
                            BDLocation bd = new BDLocation();
                            bd.setLatitude(lat);
                            bd.setLongitude(lng);
                            BDLocation target = CoordinateUtil.BAIDU_to_WGS84(bd);
                            return reverseGeoGoogle(target.getLongitude(), target.getLatitude());
                        } else {
                            return reverseGeoGoogle(lng, lat);
                        }
                    }
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                    if (mapType.equals(Constant.MAP_BAIDU)) {
                        BDLocation bd = new BDLocation();
                        bd.setLatitude(lat);
                        bd.setLongitude(lng);
                        BDLocation target = CoordinateUtil.BAIDU_to_WGS84(bd);
                        return reverseGeoGoogle(target.getLongitude(), target.getLatitude());
                    } else {
                        return reverseGeoGoogle(lng, lat);
                    }
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    private Result reverseGeoGoogle(double lng, double lat) {
        Result result = new Result();
        String lang = SettingDataManager.language;
        String requestUrl = null;
        String content = null;
        try {

            requestUrl = "http://maps.google.com/maps/api/geocode/json?language=" + lang + "&sensor=true&latlng=" + lat + "," + lng;
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("reverseGeoGoogle------");
            }
            HttpGet request = new HttpGet(requestUrl);
            HttpResponse resp = getShortHttpClient(mContext).execute(request);
            if (resp.getStatusLine() != null) {
                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    InputStream is = getUngzippedContent(resp.getEntity());
                    content = convertStreamToString(is);

                    JSONObject json = new JSONObject(content);
                    JSONArray jarray = json.optJSONArray("results");
                    if (jarray != null && jarray.length() > 0) {
                        result.statusCode = Result.OK;
                        JSONObject jobj = jarray.optJSONObject(0);
                        if (jobj != null) {
                            result.mResult = jobj.optString("formatted_address");
                        }
                    }
                } else {
                    result.statusCode = resp.getStatusLine().getStatusCode();
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 修改设备信息
     *
     * @return
     */
    public Result modifyUser(String token, String user_name, String phone, String sex, String owner, String tel, String user_id, String account, String remark, String deviceremark, int isdealer) {
//        可修改设备属性如下：
//        user_name 设备名称
//        phone 设备号码
//        tel 联系人电话
//        sex 车牌号
//        sudu 超速阈值
//        use_time 销售时间
//        remark 经销商备注信息
//        alarm 告警参数
//        alias 设备别名
//        owner 所属人
//        icontype 设备图标
//        out_time 平台到期时间
//        deviceremark 设备备注信息
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/account/devinfo?method=modifyUser&user_name=" + URLEncoder.encode(user_name, "UTF-8")
                    + "&phone=" + phone
                    + "&sex=" + URLEncoder.encode(sex, "UTF-8")
                    + "&imei=" + user_id
                    + "&access_token=" + Uri.encode(token, "UTF-8")
                    + "&account=" + Uri.encode(account, "UTF-8")
                    + "&owner=" + URLEncoder.encode(owner, "UTF-8")
                    + "&tel=" + tel;
            if(isdealer == 1)
            {
                //经销商备注
                requestUrl += ("&remark=" + Uri.encode(remark, "UTF-8"));
                requestUrl += ("&isdealer=" + isdealer);
            }
            else
            {
                requestUrl += ("&deviceremark=" + Uri.encode(deviceremark, "UTF-8"));
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();

        }
        return result;
    }

    public Result devinfo(String token, String target, String account) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/account/devinfo?access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&target=" + Uri.encode(target, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8");

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);

            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    //AllOnlineApp.isBeyondLimit = json.optBoolean("isBeyondLimit");
                    AllOnlineApp.customerId = json.optInt("customer_id");

//                    HashMap<String, ArrayList> devicesHashMap = new HashMap<String, ArrayList>();
//                    ArrayList<Device> devices = new ArrayList<Device>();
//                    ArrayList<SubAccount> subAccounts = new ArrayList<SubAccount>();
//
//                    JSONArray data = json.optJSONArray("data");
//                    if (data != null) {
//                        int length = data.length();
//                        for (int i = 0; i < length; i++) {
//                            JSONObject tmp = data.optJSONObject(i);
//                            Device device = new Device();
//                            device.imei = tmp.optString("imei");
//                            device.phone = tmp.optString("phone");
//
//                            if (account.equals(Constant.expName)) {
//                                device.name = "*****";
//                                device.number = "*****";
//                            } else {
//                                device.name = tmp.optString("name");
//                                device.number = tmp.optString("number");
//                            }
//
//                            device.group_id = tmp.optInt("group_id");
//                            device.is_enable = tmp.optInt("is_enable");
//                            device.group_name = tmp.optString("group_name");
//                            if(tmp.has("enable_time"))
//                            {
//                                device.enable_time = tmp.optString("enable_time");
//                            }
//                            else
//                            {
//                                device.enable_time = null;
//                            }
//                            device.in_time = tmp.optLong("in_time");
//                            device.out_time = tmp.optLong("out_time");
//                            device.dev_type = tmp.optString("dev_type");
//                            device.efence_support = tmp.optBoolean("efence_support");
//                            // device.efence_support = true;
//                            device.owner = tmp.optString("owner", "");
//                            device.tel = tmp.optString("tel", "");
//                            device.goome_card = tmp.optInt("goome_card", 0);
//                            device.is_iot_card = tmp.optInt("is_iot_card", 0);
//                            device.remark = tmp.optString("remark", "");
//                            device.deviceremark = tmp.optString("deviceremark", "");
//                            device.auto_id_phone = tmp.optString("auto_id_phone", "");
//                            device.auto_id_type = tmp.optInt("auto_id_type", 0);
//                            devices.add(device);
//                        }
//                    }
//
//                    JSONArray children = json.optJSONArray("children");
//                    if (children != null) {
//                        int length = children.length();
//                        for (int i = 0; i < length; i++) {
//                            JSONObject tmp = children.optJSONObject(i);
//                            SubAccount subAccount = new SubAccount();
//                            subAccount.id = tmp.optString("id");
//                            subAccount.name = tmp.optString("name");
//                            subAccount.showname = tmp.optString("showname");
//                            subAccount.haschild = tmp.optBoolean("haschild");
//                            subAccounts.add(subAccount);
//                        }
//                    }
//                    devicesHashMap.put("devices", devices);
//                    devicesHashMap.put("subAccounts", subAccounts);
//                    result.mResult = devicesHashMap;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result adjust(String token, String account, String mapType, final Location location) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/lnglat?method=adj&access_type=inner&lng=" + location.getLongitude() + "&lat=" + location.getLatitude() + "&map_type=" + mapType + "&access_token=" + Uri.encode(token, "UTF-8") + "&account=" + Uri.encode(account, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("adjust------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    JSONObject jobj = json.optJSONObject("data");
                    location.setLatitude(jobj.optDouble("lat", 0.0));
                    location.setLongitude(jobj.optDouble("lng", 0.0));
                    result.mResult = location;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result queryFence(String token, String imei, String account, String mapType, String ... alarmId) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/efence?method=get&imei=" + imei + "&map_type=" + mapType + "&access_token=" + Uri.encode(token, "UTF-8") + "&account=" + Uri.encode(account, "UTF-8");
            if(alarmId != null && alarmId.length > 0 && !TextUtils.isEmpty(alarmId[0]))
            {
                requestUrl = requestUrl + "&alarm_id=" + alarmId[0];
            }

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("queryfence------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
//                if (result.success) {
//                    result.statusCode = Result.OK;
//                    Fence fence = null;
//
//                    JSONObject jobj = json.optJSONObject("data");
//                    if (jobj != null) {
//                        String id = jobj.optString("id");
//                        if (!TextUtils.isEmpty(id)) {
//                            fence = new Fence(jobj);
//                        }
//                    }
//                    result.mResult = fence;
//                } else {
//                    result.statusCode = json.optInt("errcode");
//                    result.errorMessage = json.optString("msg");
//                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result AddFence(String token, String imei, int shapeType, String shapeParam, int validateFlag, String account, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/efence?method=set&imei=" + imei + "&shape_type=" + shapeType + "&shape_param=" + shapeParam + "&validate_flag=" + validateFlag + "&map_type=" + mapType + "&access_token=" + Uri.encode(token, "UTF-8") + "&account=" + Uri.encode(account, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("addfence------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    result.mResult = null;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result switchFence(String token, String imei, String id, int validateFlag, String account, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/efence?method=switch&imei=" + imei + "&validate_flag=" + validateFlag + "&id=" + id + "&access_token=" + Uri.encode(token, "UTF-8") + "&account=" + Uri.encode(account, "UTF-8");

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("switchfence------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result modifyPwd(String tokenold, String account, String oldpwd, String newpwd) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        long current = System.currentTimeMillis() / 1000;
        String signature = OSUtil.toMD5(OSUtil.toMD5(oldpwd) + current);

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/modify_pwd?access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(tokenold, "UTF-8") + "&signature=" + signature + "&new_pwd=" + Uri.encode(newpwd, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("modifypwd------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    Token token = new Token();
                    JSONObject data = json.optJSONObject("data");
                    token.access_token = data.optString("access_token");
                    if (!TextUtils.isEmpty(token.access_token)) {
                        result.mResult = token;
                    }
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result getAlarmInfo(String token, String target, String account, int pagesize, String type, String filter, long timestamp, String imei, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/get_alarminfo?method=" + filter + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&page_dir=" + type + "&timestamp=" + timestamp + "&imei=" + imei + "&pagesize=" + pagesize + "&map_type=" + mapType + "&target=" + target;
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("getAlarmInfo------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;

                    ArrayList<Alarm> list = new ArrayList<Alarm>();
                    result.mResult = list;
                    JSONArray jarray = json.optJSONArray("data");
                    if (jarray != null) {
                        int size = jarray.length();
                        for (int i = 0; i < size; i++) {
                            JSONObject jobj = jarray.getJSONObject(i);
                            Alarm alarm = new Gson().fromJson(jobj.toString(), Alarm.class);
//                            Alarm alarm = new Alarm();
//                            alarm.setId(jobj.optLong("id"));
//                            alarm.alarm_time = jobj.optLong("alarm_time");
//                            alarm.alarm_type = jobj.optString("alarm_type");
//                            alarm.gps_time = jobj.optLong("gps_time");
//                            alarm.dev_name = jobj.optString("dev_name");
//                            alarm.dev_type = jobj.optString("dev_type");
//                            alarm.dir = jobj.optString("dir");
//                            alarm.gps_status = jobj.optInt("gps_status");
//                            alarm.speed = jobj.optInt("speed");
//                            alarm.lat = jobj.optDouble("lat", 0.0);
//                            alarm.lng = jobj.optDouble("lng", 0.0);
                            list.add(alarm);
                        }
                    }
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result getAlarmCategoryList(String token, String account, final long timestamp, String imei, final String logintype, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/get_alarminfo?method=getAlarmOverview" +
                    "&access_type=inner&account=" + Uri.encode(account, "UTF-8") +
                    "&access_token=" + Uri.encode(token, "UTF-8") +
                    "&timestamp=" + timestamp +
                    "&imei=" + imei +
                    "&login_type=" + logintype +
                    "&map_type=" + mapType;// +
            //"alarm_type=1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42";
            if (Constant.IS_DEBUG_MODE) {
                Log.e("ttt", "getAlarmCategoryList------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
//                AlarmCategoryListBean data = new Gson().fromJson(content, AlarmCategoryListBean.class);
//                if (result.success) {
//                    result.statusCode = Result.OK;
//                    result.mResult = data;
//                } else {
//                    result.statusCode = data.getErrorcode();
//                    result.errorMessage = data.getMsg();
//                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            //            Log.e("ttt","Exception",e);
        }
        return result;
    }

    public Result getAllAlarmCount(String token, String account, final long timestamp, String imei, final String logintype, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/get_alarminfo?method=getAlarmOverviewCount" + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&timestamp=" + timestamp + "&imei=" + imei + "&login_type=" + logintype + "&map_type=" + mapType;
            if (Constant.IS_DEBUG_MODE) {
                Log.e("ttt", "getAlarmCategoryList------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    JSONObject data = json.getJSONObject("data");
                    result.mResult = data.optInt("count");
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            //            Log.e("ttt","Exception",e);
        }
        return result;
    }

    public Result getAlarmCategoryItemList(String token, String account, long timestamp, String pageDir, int pageSize, int alarmType, String imei, String logintype, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/get_alarminfo?method=getAlarmDetail" + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&timestamp=" + timestamp + "&imei=" + imei + "&login_type=" + logintype + "&map_type=" + mapType + "&page_dir=" + pageDir + "&pagesize=" + pageSize + "&alarm_type=" + (alarmType == -1 ? "" : alarmType);
            if (Constant.IS_DEBUG_MODE) {
                Log.e("ttt", "getAlarmCategoryItemList------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
//                AlarmCategoryItemListBean data = new Gson().fromJson(content, AlarmCategoryItemListBean.class);
//                if (result.success) {
//                    result.statusCode = Result.OK;
//                    result.mResult = data;
//                } else {
//                    result.statusCode = data.getErrorcode();
//                    result.errorMessage = data.getMsg();
//                }
            }
        } catch (Exception e) {
            Log.e("ttt", "Exception", e);
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result getDeviceCommand(String token, String account, String dev_type, String imei) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/order?method=get&devtype=" + dev_type + "&imei=" + imei + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&menu_version=" + "1.0";

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("getDeviceCommand------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;

                    ArrayList<Command> list = new ArrayList<Command>();
                    result.mResult = list;
                    JSONArray jarray = json.optJSONArray("data");
                    if (jarray != null) {
                        int size = jarray.length();
                        for (int i = 0; i < size; i++) {
                            JSONObject jobj = jarray.getJSONObject(i);
                            Command command = new Command();
                            command.check = jobj.optBoolean("check");
                            command.id = jobj.optInt("id");
                            command.name = jobj.optString("name");
                            command.type = jobj.optInt("type");
                            list.add(command);
                        }
                    }
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result sendCommand(String token, String account, String imei, String cmdID, String cmdParams) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/order?method=set&imei=" + imei + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&order_id=" + cmdID + "&param=" + cmdParams;
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("sendCommand------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    JSONObject jobj = json.optJSONObject("data");
                    result.mResult = jobj.optString("id");
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result getCommandResponse(String token, String account, String respId, String imei) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/order?method=get_response&id=" + respId + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&imei=" + imei;
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("getCommandResponse------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    result.mResult = json.optString("data");
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result setAlarmPhoneNum(String token, String account, String phoneNumber, String imei, String fenceId) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/efence?method=saveAlarmPhoneNum&id=" + fenceId + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&imei=" + imei + "&phone_num=" + phoneNumber;

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result signout(String token, String account, String alias) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String channelId = AllOnlineApp.sChannelID;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/account/unregister?alias=" + alias + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&channelid=" + channelId;
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("signout------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result setRead(String token, String login_type, String account, String imei, ArrayList<String> ids, Long timeStamp, String except, String alarm_type) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0, size = ids.size(); i < size; i++) {
            sb.append(ids.get(i));
            if (i != size - 1) {
                sb.append(",");
            }
        }

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/get_alarminfo?method=setread&ids=" + sb.toString()
                    + "&timestamp=" + timeStamp
                    + "&except=" + except
                    + "&access_type=inner&account=" + Uri.encode(account, "UTF-8")
                    + "&imei=" + imei
                    + "&login_type=" + login_type
                    + "&access_token=" + Uri.encode(token, "UTF-8")
                    + "&alarm_type=" + alarm_type;

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("setRead------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result setAlarmTypes(String token, String account, String alias, boolean push, int startTime, int endTime, String alarmTypeIds, boolean shake, boolean sound) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String channelId = AllOnlineApp.sChannelID;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/get_alarminfo?method=set_option&alias=" + alias + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&push=" + (push ? 1 : 0) + "&start_time=" + startTime + "&end_time=" + endTime + "&alarm_type=" + alarmTypeIds + "&sound=" + (sound ? 1 : 0) + "&shake=" + (shake ? 1 : 0) + "&channelid=" + channelId;

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("setAlarmTypes------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result getAlarmTypes(String token, String account, String alias) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String channelId = AllOnlineApp.sChannelID;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/get_alarminfo?method=get_option&alias=" + alias + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&channelid=" + channelId;
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("getAlarmTypes------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    JSONObject datajson = json.optJSONObject("data");
                    if (datajson != null) {
//                        result.statusCode = Result.OK;
//                        PushSetting setting = new PushSetting();
//                        setting.end_time = datajson.optInt("end_time");
//                        setting.start_time = datajson.optInt("start_time");
//                        setting.push = datajson.optBoolean("push");
//                        setting.shake = datajson.optBoolean("shake");
//                        setting.sound = datajson.optBoolean("sound");
//
//                        JSONArray jarray = datajson.optJSONArray("alarm_type");
//                        if (jarray != null) {
//                            setting.alarm_type = new ArrayList<PushSetting.AlarmType>();
//                            int size = jarray.length();
//                            for (int i = 0; i < size; i++) {
//                                JSONObject jobj = jarray.optJSONObject(i);
//                                if (jobj != null) {
//                                    PushSetting.AlarmType type = new PushSetting.AlarmType();
//                                    type.id = jobj.optInt("id");
//                                    type.name = jobj.optString("name");
//                                    type.push = jobj.optBoolean("push");
//
//                                    setting.alarm_type.add(type);
//                                }
//                            }
//                        }
//                        result.mResult = setting;
                    }
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result rectSearch(double toprightlng, double toprightlat, double bottomleftlng, double bottomleftlat,
        double centerlng, double cengerlat, double distance, String token) {
        Result result = new Result();
        StringBuilder requestUrl = new StringBuilder("/1/area/rectsearch?");
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;

        try {
            requestUrl.append("access_type=inner");
            requestUrl.append("&toprightlng=").append(toprightlng);
            requestUrl.append("&toprightlat=").append(toprightlat);
            requestUrl.append("&bottomleftlng=").append(bottomleftlng);
            requestUrl.append("&bottomleftlat=").append(bottomleftlat);
            requestUrl.append("&centerlat=").append(cengerlat);
            requestUrl.append("&centerlng=").append(centerlng);
            requestUrl.append("&distance=").append(distance);
            requestUrl.append("&map_type=BAIDU").append("&shownum=30");
            requestUrl.append("&access_token=").append(Uri.encode(token, "UTF-8"));
            requestUrl.append("&ver=").append(BaseApiClient.COMMUNITY_COOMIX_VERSION);
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, ">>> rectSearch >>>");
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    //ArrayList<Device> devices = new ArrayList<Device>();
                    //ArrayList<DeviceState> deviceStates = new ArrayList<DeviceState>();
                    //result.mResultTry = devices;
                    //result.mResult = deviceStates;
                    //JSONArray data = json.optJSONArray("data");
                    //
                    //if (data != null) {
                    //    int length = data.length();
                    //    for (int i = 0; i < length; i++) {
                    //        JSONArray tmp = data.optJSONArray(i);
                    //        if (tmp == null || tmp.length() != 23) {
                    //            continue;
                    //        }
                    //        Device device = new Device();
                    //        DeviceState deviceState = new DeviceState();
                    //
                    //        device.imei = tmp.getString(0);
                    //        device.name = tmp.optString(1);
                    //        device.number = tmp.optString(2);
                    //
                    //        device.phone = tmp.optString(3);
                    //        device.group_id = tmp.optInt(4);
                    //        device.group_name = tmp.optString(5);
                    //        device.dev_type = tmp.optString(6);
                    //        //device.enable_time = tmp.optLong("enable_time");
                    //        device.in_time = tmp.optLong(7);
                    //        device.out_time = tmp.optLong(8);
                    //        device.efence_support = tmp.optBoolean(9);
                    //        deviceState.device_info = tmp.optInt(10);
                    //        deviceState.gps_time = tmp.optLong(11);
                    //        deviceState.sys_time = tmp.optLong(12);
                    //        deviceState.heart_time = tmp.optLong(13);
                    //        deviceState.server_time = tmp.optLong(14);
                    //        deviceState.lng = tmp.optDouble(15, 0.0);
                    //        deviceState.lat = tmp.optDouble(16, 0.0);
                    //        deviceState.course = tmp.optInt(17);
                    //        deviceState.speed = tmp.optInt(18);
                    //        deviceState.acc = tmp.optInt(19);
                    //        deviceState.seconds = tmp.optInt(20);// 离线或者是静止时长
                    //        deviceState.power = tmp.optString(21);
                    //        deviceState.locationType = tmp.optString(22);
                    //        deviceState.imei = tmp.optString(0);
                    //        deviceState.device_info_new = tmp.optInt(10); // 获取设置的状态
                    //        device.state = deviceState;
                    //        devices.add(device);
                    //        deviceStates.add(deviceState);
                    //    }
                    //}
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result provinceList(String account, String token) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/efence?method=get_provinces&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("provinceList---------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    ArrayList<Province> provinces = new ArrayList<Province>();
                    result.mResult = provinces;
                    JSONArray data = json.optJSONArray("data");

                    if (data != null) {
                        int length = data.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject tmp = data.optJSONObject(i);
                            Province province = new Province();
                            province.id = tmp.optString("id");
                            province.name = tmp.optString("name");

                            provinces.add(province);
                        }
                    }
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result cityList(String provinceId, String account, String token) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/efence?method=get_cities&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&province=" + provinceId + "&access_token=" + Uri.encode(token, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("cityList---------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    ArrayList<City> cities = new ArrayList<City>();
                    result.mResult = cities;
                    JSONArray data = json.optJSONArray("data");

                    if (data != null) {
                        int length = data.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject tmp = data.optJSONObject(i);
                            City city = new City();
                            city.id = tmp.optString("id");
                            city.name = tmp.optString("name");

                            cities.add(city);
                        }
                    }
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result getOverspeed(String imei, String account, String token) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/get_alarminfo?method=get_overspeed" + "&imei=" + imei + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8");

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("getOverspeed---------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    Overspeed overspeed = new Overspeed();
                    JSONObject data = json.optJSONObject("data");
                    if (data != null) {
//                        if (data.optInt("overspeed_flag", 0) == 1) {
//                            overspeed.flag = true;
//                        } else {
//                            overspeed.flag = false;
//                        }
                        overspeed.speed = data.optInt("speed", 0);
                    }
                    result.mResult = overspeed;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result setOverspeed(String imei, boolean flag, int speed, String account, String token) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {

            if (flag) {
                requestUrl = "/1/account/devinfo?method=modifyUser" + "&speed=" + speed + "&overspeed_flag=1" + "&imei=" + imei + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8");
            } else {
                requestUrl = "/1/account/devinfo?method=modifyUser" + "&overspeed_flag=0" + "&imei=" + imei + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8");
            }
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("setOverspeed------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result getAreaFence(String imei, String account, String token, String alarmId) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/efence?method=get_area_fence" + "&imei=" + imei + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8");
            if(!TextUtils.isEmpty(alarmId))
            {
                requestUrl = requestUrl + "&alarm_id=" + alarmId;
            }

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("getAreaFence---------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    AreaFence areaFence = new AreaFence();
                    JSONObject data = json.optJSONObject("data");
                    if (data != null) {
//                        areaFence.id = data.optString("id", "");
//                        if (data.optInt("validate_flag", 0) == 1) {
//                            areaFence.flag = true;
//                        } else {
//                            areaFence.flag = false;
//                        }
//                        JSONObject shape_param = data.optJSONObject("shape_param");
//                        if (shape_param != null) {
//                            areaFence.province = shape_param.optString("province", "");
//                            areaFence.city = shape_param.optString("city", "");
//                            areaFence.district = "";
//                        }
                    }
                    result.mResult = areaFence;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result setAreaFence(String imei, boolean flag, String areaId, String account, String token) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            int flagInt = 0;
            if (flag) {
                flagInt = 1;
            }
            requestUrl = "/1/tool/efence?method=set_area_fence" + "&citycode=" + areaId + "&validate_flag=" + flagInt + "&imei=" + imei + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8");

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("setAreaFence---------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    String idString = "";
                    JSONObject data = json.optJSONObject("data");
                    if (data != null) {
                        idString = data.optString("id", "");
                    }
                    result.mResult = idString;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result switchAreaFence(String imei, String id, boolean flag, String account, String token) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            int flagInt = 0;
            if (flag) {
                flagInt = 1;
            }
            requestUrl = "/1/tool/efence?method=switch_area_fence" + "&id=" + id + "&validate_flag=" + flagInt + "&imei=" + imei + "&access_type=inner&account=" + Uri.encode(account, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8");

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("switchAreaFence---------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result uploadLocation(double lng, double lat, long gpsTime, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        long uploadTime = System.currentTimeMillis() / 1000;
        String appVersion = OSUtil.getAppVersionNameExtend(mContext);
        String appType = "goocar";

        String server = null;
        if (DomainManager.sRespDomainAdd != null) {
            server = DomainManager.sRespDomainAdd.domainGapi;
        }
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            // 域名写死了，注意。。。"http://bus.gapi.gpsoo.net"
            requestUrl = "/v1/bus/mbcommonservice?method=bus_user_upload_gps" + "&lat=" + lat + "&lng=" + lng + "&gpsTime=" + gpsTime + "&mapType=" + mapType + "&ver=" + appVersion + "&t=" + uploadTime + "&apptype=" + appType;

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("uploadLocation------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result queryDeviceSetting(String imei, String account, String token, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/efence?method=get_switch_status" + "&imei=" + imei + "&map_type=" + mapType + "&access_token=" + Uri.encode(token, "UTF-8") + "&account=" + Uri.encode(account, "UTF-8");

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("queryDeviceSetting---------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;

                    DeviceSetting deviceSetting = new DeviceSetting();

                    JSONObject data = json.optJSONObject("data");
                    if (data != null) {
                        JSONObject areaJson = data.optJSONObject("area");
                        AreaFence areaFence = new AreaFence();
                        if (areaJson != null) {
                            areaFence.id = areaJson.optString("id", "");
                            areaFence.validate_flag = areaJson.optInt("validate_flag", 0);
                            JSONObject shapeParam = areaJson.optJSONObject("shape_param");
//                            if (shape_param != null) {
//                                areaFence.province = shape_param.optString("province", "");
//                                areaFence.city = shape_param.optString("city", "");
//                                areaFence.district = "";
//                            }
                        }
//                        deviceSetting.area = areaFence;

                        JSONObject overspeedJson = data.optJSONObject("overspeed");
                        Overspeed overspeed = new Overspeed();
                        if (overspeedJson != null) {
                            overspeed.overspeed_flag = overspeedJson.optInt("overspeed_flag", 0);
                            overspeed.speed = overspeedJson.optInt("speed", 0);
                        }
//                        deviceSetting.overspeed = overspeed;

                        JSONObject fenceJson = data.optJSONObject("efence");
                        Fence fence = null;
//                        if (fenceJson != null) {
//                            String id = fenceJson.optString("id");
//                            if (!TextUtils.isEmpty(id)) {
//                                fence = new Fence(fenceJson);
//                            }
//                        }
//                        deviceSetting.efence = fence;
                    }
                    result.mResult = deviceSetting;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }


    public Result getAuthPages(String token) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/account?method=getAuthPages" + "&access_token=" + Uri.encode(token, "UTF-8");

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("getAuthPages------" + requestUrl + "\n" + content);
            }
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    JSONObject jo = json.optJSONObject("data");
                    AuthPages authPages = new Gson().fromJson(jo.toString(), AuthPages.class);
                    result.mResult = authPages;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result getAd(int width, int height, double lat, double lng, String mapType, String posMapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        long current = System.currentTimeMillis() / 1000;
        String appVersion = OSUtil.getAppVersionNameExtend(mContext);
        String appType = "goocar";
        String os = "andriod";

        String server = null;
        if (DomainManager.sRespDomainAdd != null) {
            server = DomainManager.sRespDomainAdd.domainAd;
        }
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            //http://ad.gpsoo.net
            requestUrl = "/v1/bus/mbcommonservice?method=getallad" + "&citycode=" + Constant.COMMUNITY_CITYCODE + "&width=" + width + "&height=" + height + "&t=" + current + "&ver=" + appVersion + "&apptype=" + appType + "&maptype=" + mapType + "&posmaptype=" + posMapType + "&cn=" + "gm" + "&lat=" + lat + "&lng=" + lng;
            if(!TextUtils.isEmpty(AllOnlineApp.sAccount))
            {
                requestUrl = requestUrl + "&login_name=" + getUrlEncoder(AllOnlineApp.sAccount);
            }
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("getAd------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    ArrayList<Adver> advers = new ArrayList<Adver>();

                    int cityCode = json.optInt("citycode", 0);

                    JSONArray data = json.optJSONArray("data");
                    if (data != null) {
                        int length = data.length();
                        for (int i = 0; i < length; i++) {
//                            JSONObject adObject = data.getJSONObject(i);
//
//                            Adver adver = new Adver();
//                            adver.cityCode = cityCode;
//                            adver.adverId = adObject.optInt("id", -1);
//                            int adtype = adObject.optInt("type", 1);
//                            if (adtype == 1) {
//                                adver.adverType = AdType.AdTypeLaunch;
//                            } else if (adtype == 2) {
//                                adver.adverType = AdType.AdTypeHome;
//                            } else if (adtype == 3) {
//                                adver.adverType = AdType.AdTypeTransfer;
//                            } else if (adtype == 4) {
//                                adver.adverType = AdType.AdTypeWindow;
//                            }
//
//                            adver.adverUrl = adObject.optString("picurl", "").trim();
//                            if (!CommonUtil.isTrimEmpty(adver.adverUrl) && !isContainHttp(isHttps, adver.adverUrl)) {
//                                adver.adverUrl = getDN(isHttps, adver.adverUrl);//"http://" + adver.adverUrl;
//                            }
//
//                            adver.adverJpumpUrl = adObject.optString("jumpurl", "").trim();
//                            if (!CommonUtil.isTrimEmpty(adver.adverJpumpUrl) && !isContainHttp(isHttps, adver.adverJpumpUrl)) {
//                                adver.adverJpumpUrl = getDN(isHttps, adver.adverJpumpUrl);//"http://" + adver.adverJpumpUrl;
//                            }
//                            adver.md5 = adObject.optString("md5");
//                            advers.add(adver);
                        }
                    }
                    result.mResult = getSortADList(advers);
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取排序好的adlist
     */
    private List<Adver> getSortADList(List<Adver> adList) {
        if (adList == null || adList.size() < 2) {
            return adList;
        }
        try {
            Collections.sort(adList, new Comparator<Adver>() {
                @Override
                public int compare(Adver lhs, Adver rhs) {
                    if (lhs.type > rhs.type) {
                        return 1;
                    } else if (lhs.type < rhs.type) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        } catch (Exception e) {
        }
        return adList;
    }

    //getad,保证安装https的状态装载广告接口
    public Result downloadImage(String url, String fileName) {
        Result result = new Result();
        result.statusCode = Result.FAIL;

        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            HttpGet request = new HttpGet(url);
            request.addHeader("Connection", "Close");
            HttpClient client = getShortHttpClient(mContext, isHttps);
            HttpResponse resp = client.execute(request);

            if (resp.getStatusLine() != null) {
                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    InputStream is = resp.getEntity().getContent();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    if (bitmap != null) {
                        File f = mContext.getFileStreamPath(fileName);
                        if (f.exists()) {
                            f.delete();
                        }
                        FileOutputStream os = new FileOutputStream(f);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        os.flush();
                        os.close();
                        result.mResult = bitmap;
                        result.statusCode = Result.OK;
                    }
                    is.close();
                } else {
                    result.statusCode = resp.getStatusLine().getStatusCode();
                }
            }
        } catch (Exception e) {
            processException(result, e);
            e.printStackTrace();
        }
        return result;
    }

    public Result clickAdvertiser(int adId, int cityCode, double lat, double lng, String mapType, String posMapType, int type) {
        // type type=1，点击一次，type=2，展现一次
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        long uploadTime = System.currentTimeMillis() / 1000;
        String appVersion = OSUtil.getAppVersionNameExtend(mContext);

        String appType = "goocar";

        PhoneInfo phoneInfo = new PhoneInfo(mContext);
        String phoneNumber = phoneInfo.getNativePhoneNumber();
        if (phoneNumber == null) {
            phoneNumber = "";
        }

        String server = null;
        if (DomainManager.sRespDomainAdd != null) {
            server = DomainManager.sRespDomainAdd.domainAd;
        }
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            // 域名写死了，注意。。。
            requestUrl = "/v1/bus/mbcommonservice?method=upload_ad_click" + "&apptype=" + appType + "&cn=" + "gm" + "&login=" + phoneNumber + "&posmaptype=" + posMapType + "&mapType=" + mapType + "&type=" + type + "&ver=" + appVersion + "&citycode=" + cityCode + "&t=" + uploadTime + "&ad_id=" + adId + "&lat=" + lat + "&lng=" + lng;
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("clickAdvertiser---------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }

        return result;
    }

    public Result getBlacklist(String condition, String account, String token) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/tool/blacklist?method=searchIncident" + "&account=" + Uri.encode(account, "UTF-8") + "&condition=" + Uri.encode(condition, "UTF-8") + "&detail=" + "1" + "&access_token=" + Uri.encode(token, "UTF-8") + "&map_type=" + Constant.MAP_BAIDU;

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("getBlacklist---------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;

                    ArrayList<Blacklist> blacklists = new ArrayList<Blacklist>();
                    JSONArray data = json.optJSONArray("data");
                    if (data != null) {
                        int length = data.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject tmp = data.optJSONObject(i);
                            Blacklist blacklist = new Blacklist();
                            blacklist.user_name = tmp.optString("user_name", "");
                            blacklist.sex = tmp.optString("sex", "");
                            blacklist.id_card_no = tmp.optString("id_card_no", "");
                            blacklist.drive_card_no = tmp.optString("drive_card_no", "");
                            blacklist.incident_date = tmp.optString("incident_date", "");
                            blacklist.incident_desp = tmp.optString("incident_desp", "");
                            blacklist.customer_id = tmp.optString("customer_id", "");
                            blacklist.customer_name = tmp.optString("customer_name", "");
                            blacklist.customer_phone = tmp.optString("customer_phone", "");
                            blacklist.create_time = tmp.optString("create_time", "");
                            blacklists.add(blacklist);
                        }
                    }
                    result.mResult = blacklists;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    //注意语言问题，
    //String lang = "zh-CN";   //1.8.3 暂只配置中文简体
    public Result getLatestVersion() {
        Result result = new Result();
        StringBuilder requestUrl = new StringBuilder();
        String content = null;
        String android_version = "";
        String patchcode = "";

        String lang = SettingDataManager.language;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;

        requestUrl.append("/1/tool/appupdate?");

        int versionCode = 1;
        try {
            versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            android_version = Build.VERSION.RELEASE;
            if (TextUtils.isEmpty(android_version)) {
                android_version = "";
            } else {
                android_version = "&android_version=" + android_version;
            }
            // 获取patch version
            Tinker tinker = Tinker.with(mContext);
            String patchVersion = tinker.getTinkerLoadResultIfPresent().getPackageConfigByName("patchVersion");
            if (TextUtils.isEmpty(patchVersion)) {
                patchVersion = "0";
            }
            patchcode = "&patchcode=" + patchVersion;

            requestUrl.append("language=").append(lang);
            requestUrl.append("&vercode=").append(versionCode);
            requestUrl.append("&apptype=allonline");
            requestUrl.append(android_version);
            requestUrl.append(patchcode);

            //requestUrl = "/1/tool/appupdate?language=" + lang + "&vercode=" + versionCode + "&apptype=" + apptype;
            content = httpToServerRequestOrUpData(requestUrl.toString(), server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    JSONObject dataObj = json.optJSONObject("data");
                    if (dataObj != null) {
                        result.mResult = new Gson().fromJson(dataObj.toString(), GoomeUpdateInfo.class);
                    }
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    //网络搜索
    public Result getDevSearch(String target, String account, String token, String mapType, String search_content, int stype) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {

            requestUrl = "/1/account/search?method=app_deviceSearch" + "&search_content=" + search_content + "&account=" + Uri.encode(account, "UTF-8") + "&target=" + Uri.encode(target, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&map_type=" + mapType + "&search_type=" + stype;

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("getDevSearch-------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;

//                    HashMap<String, ArrayList> devicesHashMap = new HashMap<String, ArrayList>();
//                    ArrayList<Device> devices = new ArrayList<Device>();
//                    ArrayList<DeviceState> states = new ArrayList<DeviceState>();
//                    JSONArray data = json.optJSONArray("data");
//                    if (data != null) {
//                        int length = data.length();
//                        for (int i = 0; i < length; i++) {
//                            JSONObject tmp = data.optJSONObject(i);
//
//                            Device device = new Device();
//                            DeviceState state = new DeviceState();
//
//                            device.imei = tmp.optString("imei");
//                            device.phone = tmp.optString("phone");
//
//                            if (account.equals(Constant.expName)) {
//                                device.name = "*****";
//                                device.number = "*****";
//                            } else {
//                                device.name = tmp.optString("name");
//                                device.number = tmp.optString("number");
//                            }
//
//                            //搜索出的id
//                            device.customer_id = tmp.optInt("customer_id");
//                            device.customer_name = tmp.optString("customer_name");
//                            device.customer_showname = tmp.optString("customer_showname");
//                            device.group_name = tmp.optString("customer_showname");
//                            device.group_id = tmp.optInt("customer_id");
//                            device.is_enable = tmp.optInt("is_enable");
//                            if(tmp.has("enable_time"))
//                            {
//                                device.enable_time = tmp.optString("enable_time");
//                            }
//                            else
//                            {
//                                device.enable_time = null;
//                            }
//                            device.in_time = tmp.optLong("in_time");
//                            device.out_time = tmp.optLong("out_time");
//                            device.dev_type = tmp.optString("dev_type");
//                            device.goome_card = tmp.optInt("goome_card", 0);
//                            device.is_iot_card = tmp.optInt("is_iot_card", 0);
//                            device.remark = tmp.optString("remark", "");
//                            device.deviceremark = tmp.optString("deviceremark", "");
//                            device.efence_support = tmp.optBoolean("efence_support");
//                            device.auto_id_phone = tmp.optString("auto_id_phone", "");
//                            device.auto_id_type = tmp.optInt("auto_id_type", 0);
//                            // device.efence_support = true;
//
//                            device.owner = tmp.optString("owner", "");
//                            device.tel = tmp.optString("tel", "");
//
//                            state.imei = tmp.optString("imei");
//                            state.course = tmp.optInt("course");
//                            state.device_info = tmp.optInt("device_info");
//                            state.gps_time = tmp.optLong("gps_time");
//                            state.heart_time = tmp.optLong("heart_time");
//                            state.lat = tmp.optDouble("lat", 0.0);
//                            state.lng = tmp.optDouble("lng", 0.0);
//                            state.acc = tmp.optInt("acc");
//                            state.server_time = tmp.optLong("server_time");
//                            state.speed = tmp.optInt("speed");
//                            state.sys_time = tmp.optLong("sys_time");
//                            state.device_info_new = tmp.optInt("device_info_new"); // 获取设置的状态
//                            state.seconds = tmp.optLong("seconds");// 离线或者是静止时长
//                            state.acc_seconds = tmp.optLong("acc_seconds"); // ACC开启/熄火时长
//                            state.power = tmp.optString("power");
//                            state.locationType = tmp.optString("locationType");
//                            device.state = state;
//                            devices.add(device);
//                            states.add(state);
//                        }
//                    }
//                    devicesHashMap.put("devices", devices);
//                    devicesHashMap.put("states", states);
//                    result.mResult = devicesHashMap;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result filterDevInfo(String target, String account, String token, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/account/devinfo?method=app_getDevInfoAndLocation" + "&account=" + Uri.encode(account, "UTF-8") + "&target=" + Uri.encode(target, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&map_type=" + mapType + "&with_not_enable=true";

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("filterDevInfo-------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);

            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;

//                    HashMap<String, ArrayList> devicesHashMap = new HashMap<String, ArrayList>();
//                    ArrayList<Device> devices = new ArrayList<Device>();
//                    ArrayList<DeviceState> states = new ArrayList<DeviceState>();
//                    JSONArray data = json.optJSONArray("data");
//                    if (data != null) {
//                        int length = data.length();
//                        for (int i = 0; i < length; i++) {
//                            JSONObject tmp = data.optJSONObject(i);
//
//                            Device device = new Device();
//                            DeviceState state = new DeviceState();
//
//                            device.imei = tmp.optString("imei");
//                            device.phone = tmp.optString("phone");
//
//                            if (account.equals(Constant.expName)) {
//                                device.name = "*****";
//                                device.number = "*****";
//                            } else {
//                                device.name = tmp.optString("name");
//                                device.number = tmp.optString("number");
//                            }
//
//                            //车辆列表增加字段用于辨别id
//                            device.group_id = tmp.optInt("customer_id");
//                            device.is_enable = tmp.optInt("is_enable");
//                            device.group_name = tmp.optString("customer_name");
//                            if(tmp.has("enable_time"))
//                            {
//                                device.enable_time = tmp.optString("enable_time");
//                            }
//                            else
//                            {
//                                device.enable_time = null;
//                            }
//                            device.in_time = tmp.optLong("in_time");
//                            device.out_time = tmp.optLong("out_time");
//                            device.dev_type = tmp.optString("dev_type");
//                            device.goome_card = tmp.optInt("goome_card", 0);
//                            device.is_iot_card = tmp.optInt("is_iot_card", 0);
//                            device.remark = tmp.optString("remark", "");
//                            device.deviceremark = tmp.optString("deviceremark", "");
//                            device.efence_support = tmp.optBoolean("efence_support");
//                            device.auto_id_phone = tmp.optString("auto_id_phone", "");
//                            device.auto_id_type = tmp.optInt("auto_id_type", 0);
//                            // device.efence_support = true;
//
//                            device.owner = tmp.optString("owner", "");
//                            device.tel = tmp.optString("tel", "");
//
//                            state.imei = tmp.optString("imei");
//                            state.course = tmp.optInt("course");
//                            state.device_info = tmp.optInt("device_info");
//                            state.gps_time = tmp.optLong("gps_time");
//                            state.heart_time = tmp.optLong("heart_time");
//                            state.lat = tmp.optDouble("lat", 0.0);
//                            state.lng = tmp.optDouble("lng", 0.0);
//                            state.acc = tmp.optInt("acc");
//                            state.server_time = tmp.optLong("server_time");
//                            state.speed = tmp.optInt("speed");
//                            state.sys_time = tmp.optLong("sys_time");
//                            state.device_info_new = tmp.optInt("device_info_new"); // 获取设置的状态
//                            state.seconds = tmp.optLong("seconds");// 离线或者是静止时长
//                            state.acc_seconds = tmp.optLong("acc_seconds"); // ACC开启/熄火时长
//                            state.power = tmp.optString("power");
//                            state.locationType = tmp.optString("locationType");
//                            device.state = state;
//                            devices.add(device);
//                            states.add(state);
//                        }
//                    }
//                    devicesHashMap.put("devices", devices);
//                    devicesHashMap.put("states", states);
//                    result.mResult = devicesHashMap;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result subFilterDevInfo(String target, String account, String token, String mapType) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/account/devinfo?method=app_getDevInfoAndLocation" + "&account=" + Uri.encode(account, "UTF-8") + "&target=" + Uri.encode(target, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8") + "&source=" + Constant.NEW_VERSION_TAG + "&map_type=" + mapType;

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("subfilterDevInfo-------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);

            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;

//                    HashMap<String, ArrayList> devicesHashMap = new HashMap<String, ArrayList>();
//                    ArrayList<Device> devices = new ArrayList<Device>();
//                    ArrayList<DeviceState> states = new ArrayList<DeviceState>();
//                    JSONArray data = json.optJSONArray("data");
//                    if (data != null) {
//                        int length = data.length();
//                        for (int i = 0; i < length; i++) {
//                            JSONObject tmp = data.optJSONObject(i);
//
//                            Device device = new Device();
//                            DeviceState state = new DeviceState();
//
//                            device.imei = tmp.optString("imei");
//                            device.phone = tmp.optString("phone");
//
//                            if (account.equals(Constant.expName)) {
//                                device.name = "*****";
//                                device.number = "*****";
//                            } else {
//                                device.name = tmp.optString("name");
//                                device.number = tmp.optString("number");
//                            }
//
//                            device.group_id = tmp.optInt("group_id");
//                            device.is_enable = tmp.optInt("is_enable");
//                            device.group_name = tmp.optString("group_name");
//                            if(tmp.has("enable_time"))
//                            {
//                                device.enable_time = tmp.optString("enable_time");
//                            }
//                            else
//                            {
//                                device.enable_time = null;
//                            }
//                            device.in_time = tmp.optLong("in_time");
//                            device.out_time = tmp.optLong("out_time");
//                            device.goome_card = tmp.optInt("goome_card", 0);
//                            device.is_iot_card = tmp.optInt("is_iot_card", 0);
//                            device.remark = tmp.optString("remark", "");
//                            device.deviceremark = tmp.optString("deviceremark", "");
//                            device.dev_type = tmp.optString("dev_type");
//                            device.efence_support = tmp.optBoolean("efence_support");
//                            device.auto_id_phone = tmp.optString("auto_id_phone", "");
//                            device.auto_id_type = tmp.optInt("auto_id_type", 0);
//                            // device.efence_support = true;
//
//                            device.owner = tmp.optString("owner", "");
//                            device.tel = tmp.optString("tel", "");
//
//                            state.imei = tmp.optString("imei");
//                            state.course = tmp.optInt("course");
//                            state.device_info = tmp.optInt("device_info");
//                            state.gps_time = tmp.optLong("gps_time");
//                            state.heart_time = tmp.optLong("heart_time");
//                            state.lat = tmp.optDouble("lat", 0.0);
//                            state.lng = tmp.optDouble("lng", 0.0);
//                            state.acc = tmp.optInt("acc");
//                            state.server_time = tmp.optLong("server_time");
//                            state.speed = tmp.optInt("speed");
//                            state.sys_time = tmp.optLong("sys_time");
//                            state.device_info_new = tmp.optInt("device_info_new"); // 获取设置的状态
//                            state.seconds = tmp.optLong("seconds");// 离线或者是静止时长
//                            state.acc_seconds = tmp.optLong("acc_seconds"); // ACC开启/熄火时长
//                            state.power = tmp.optString("power");
//                            state.locationType = tmp.optString("locationType");
//
//                            device.state = state;
//                            devices.add(device);
//                            states.add(state);
//                        }
//                    }
//                    devicesHashMap.put("devices", devices);
//                    devicesHashMap.put("states", states);
//                    result.mResult = devicesHashMap;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    //子账户的车辆信息
    public Result subDevinfo(String token, String target, String account) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/account/devinfo?access_type=inner&account=" + Uri.encode(account, "UTF-8")
                    + "&target=" + Uri.encode(target, "UTF-8") + "&access_token=" + Uri.encode(token, "UTF-8");

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("subDevinfo----: " + content);
            }


            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;

//                    HashMap<String, ArrayList> devicesHashMap = new HashMap<String, ArrayList>();
//                    ArrayList<Device> devices = new ArrayList<Device>();
//                    ArrayList<SubAccount> subAccounts = new ArrayList<SubAccount>();
//
//                    JSONArray data = json.optJSONArray("data");
//                    if (data != null) {
//                        int length = data.length();
//                        for (int i = 0; i < length; i++) {
//                            JSONObject tmp = data.optJSONObject(i);
//                            Device device = new Device();
//                            device.imei = tmp.optString("imei");
//                            device.phone = tmp.optString("phone");
//
//                            if (account.equals(Constant.expName)) {
//                                device.name = "*****";
//                                device.number = "*****";
//                            } else {
//                                device.name = tmp.optString("name");
//                                device.number = tmp.optString("number");
//                            }
//
//                            device.group_id = tmp.optInt("group_id");
//                            device.is_enable = tmp.optInt("is_enable");
//                            device.group_name = tmp.optString("group_name");
//                            if(tmp.has("enable_time"))
//                            {
//                                device.enable_time = tmp.optString("enable_time");
//                            }
//                            else
//                            {
//                                device.enable_time = null;
//                            }
//                            device.in_time = tmp.optLong("in_time");
//                            device.out_time = tmp.optLong("out_time");
//                            device.goome_card = tmp.optInt("goome_card", 0);
//                            device.is_iot_card = tmp.optInt("is_iot_card", 0);
//                            device.remark = tmp.optString("remark", "");
//                            device.deviceremark = tmp.optString("deviceremark", "");
//                            device.dev_type = tmp.optString("dev_type");
//                            device.efence_support = tmp.optBoolean("efence_support");
//                            device.auto_id_phone = tmp.optString("auto_id_phone", "");
//                            device.auto_id_type = tmp.optInt("auto_id_type", 0);
//                            // device.efence_support = true;
//
//                            device.owner = tmp.optString("owner", "");
//                            device.tel = tmp.optString("tel", "");
//
//                            devices.add(device);
//                        }
//                    }
//
//                    JSONArray children = json.optJSONArray("children");
//                    if (children != null) {
//                        int length = children.length();
//                        for (int i = 0; i < length; i++) {
//                            JSONObject tmp = children.optJSONObject(i);
//                            SubAccount subAccount = new SubAccount();
//                            subAccount.id = tmp.optString("id");
//                            subAccount.name = tmp.optString("name");
//                            subAccount.showname = tmp.optString("showname");
//                            subAccount.haschild = tmp.optBoolean("haschild");
//
//
//                            subAccounts.add(subAccount);
//                        }
//                    }
//
//                    devicesHashMap.put("devices", devices);
//                    devicesHashMap.put("subAccounts", subAccounts);
//
//                    result.mResult = devicesHashMap;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public Result getSingleDevInfo(String imei, String account, String token, String mapType) {
        Result result = new Result();
        StringBuilder requestUrl = new StringBuilder("/1/devices/tracking?");
        String content = null;
        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl.append("imeis=").append(imei);
            requestUrl.append("&map_type=").append(mapType);
            requestUrl.append("&access_token=").append(Uri.encode(token, "UTF-8"));
            requestUrl.append("&account=").append(Uri.encode(account, "UTF-8"));
            requestUrl.append("&ver=").append(BaseApiClient.COMMUNITY_COOMIX_VERSION);
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, ">>> getSingleDevInfo >>>");
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;

                    //JSONArray data = json.optJSONArray("data");
                    //DeviceState device = new DeviceState();
                    //if (data != null) {
                    //
                    //    JSONObject tmp = data.optJSONObject(0);
                    //
                    //    device.course = tmp.optInt("course");
                    //    device.device_info = tmp.optInt("device_info");
                    //    device.device_info_new = tmp.optInt("device_info_new");
                    //    device.gps_time = tmp.optLong("gps_time");
                    //    device.heart_time = tmp.optLong("heart_time");
                    //    device.imei = tmp.optString("imei");
                    //    device.lat = tmp.optDouble("lat", 0.0);
                    //    device.lng = tmp.optDouble("lng", 0.0);
                    //    device.server_time = tmp.optLong("server_time");
                    //    device.speed = tmp.optInt("speed");
                    //    device.acc = tmp.optInt("acc");
                    //    device.sys_time = tmp.optLong("sys_time");
                    //    device.seconds = tmp.optLong("seconds");
                    //    device.acc_seconds = tmp.optLong("acc_seconds"); // ACC开启/熄火时长
                    //    device.power = tmp.optString("power");
                    //    device.locationType = tmp.optString("location");
                    //
                    //}
                    //result.mResult = device;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public static String commandList(String imei, String name, String devtype, String offline, String account, String token, String version) {
        String requestUrl = null;
        long current = System.currentTimeMillis() / 1000;
        String lang = SettingDataManager.language;

        String domain = null;
        if (DomainManager.sRespDomainAdd != null) {
            domain = DomainManager.sRespDomainAdd.domainWeb;
        }
        boolean isHttps = true;//DomainManager.sRespDomainAdd.httpsFlag;
        //http://www.gpsoo.net
        requestUrl = getDN(isHttps, domain) + "/wx/command.shtml?account=" + Uri.encode(account, "UTF-8") + "&time=" + current + "&access_type=inner&source=app" + "&locale=" + lang + "&imei=" + imei + "&name=" + name + "&devtype=" + devtype + "&access_token=" + Uri.encode(token, "UTF-8") + "&offline=" + offline + "&appver=" + version + "&api_domain=" + domain;

        if (Constant.IS_DEBUG_MODE) {
            System.out.println("commandList---------" + requestUrl);
            //1.8.4以前域名www.gpsoo.net
        }
        //       String requestUrl = "http://www.gpsoo.net/wx/command.shtml?account="
        //       + Uri.encode(AllOnlineApp.sAccount, "UTF-8") + "&time=" + current + "&access_type=inner&source=app"
        //       + "&locale=" + lang + "&imei=" + device.imei + "&name=" + device.name + "&devtype=" + device.dev_type
        //       + "&access_token=" + Uri.encode(AllOnlineApp.sToken.access_token) + "&offline=" + offline;

        return requestUrl;
    }

    public static String instructions() {
        String domain = null;
        if (DomainManager.sRespDomainAdd != null) {
            domain = DomainManager.sRespDomainAdd.domainWeb;
        }
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        String requestUrl = getDN(isHttps, domain) + "/help/help_car_app_new.html";
        if (Constant.IS_DEBUG_MODE) {
            System.out.println("instructions-------" + requestUrl);
        }
        return requestUrl;
    }

    public static String helpAndFeedback() {
        String domain = null;
        if (DomainManager.sRespDomainAdd != null) {
            domain = DomainManager.sRespDomainAdd.domainWeb;
        }
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        String requestUrl = getDN(isHttps, domain) + "/webapp/help/index.shtml";
        if (Constant.IS_DEBUG_MODE) {
            System.out.println("helpAndFeedback-------" + requestUrl);
        }
        return requestUrl;
    }

    public static String help() {
        String domain = null;
        if (DomainManager.sRespDomainAdd != null) {
            domain = DomainManager.sRespDomainAdd.domainWeb;
        }
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        String requestUrl = getDN(isHttps, domain) + "/webapp/help/index.shtml";
        if (Constant.IS_DEBUG_MODE) {
            System.out.println("help-------" + requestUrl);
        }
        return requestUrl;
    }

    public static String newIndication() {
        String domain = null;
        if (DomainManager.sRespDomainAdd != null) {
            domain = DomainManager.sRespDomainAdd.domainGps;
        }
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        String requestUrl = getDN(isHttps, domain);
        if (Constant.IS_DEBUG_MODE) {
            System.out.println("newIndication-------" + requestUrl);
        }
        return requestUrl;
    }

    public static String weizhang() {
        String domain = null;
        if (DomainManager.sRespDomainAdd != null) {
            domain = DomainManager.sRespDomainAdd.domainWeb;
        }
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        String requestUrl = getDN(isHttps, domain) + "/tool/wzcx.html";
        if (Constant.IS_DEBUG_MODE) {
            System.out.println("weizhang-------" + requestUrl);
        }
        return requestUrl;
    }

    public Result pictureUpload(String account, String token, int index) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            // 域名写死了，注意。。。
            requestUrl = "/1/tool/modify_icon?icon=" + index + "&access_token=" + Uri.encode(token, "UTF-8");
            if (Constant.IS_DEBUG_MODE) {
                System.out.println("pictureUpload---------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            e.printStackTrace();
        }
        return result;
    }

    public synchronized Result logUpload(LogUploadInfo info) {
        Result result = new Result();
        String content = null;
        String requestUrl = null;
        String accessBy = NetworkUtil.isWifiExtend(mContext) ? "wifi" : "mobile";

        long current = System.currentTimeMillis() / 1000;
        String server = null;
        if (DomainManager.sRespDomainAdd != null) {
            server = DomainManager.sRespDomainAdd.domainLog;
        }
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            //"http://applog.gpsoo.net"
            requestUrl = "/1/log?method=upload" + "&production=" + Uri.encode(info.getProduction(), "UTF-8") + "&devname=" + Uri.encode(info.getDevname(), "UTF-8") + "&osver=" + info.getOsver() + "&osextra=" + Uri.encode(info.getOsextra(), "UTF-8") + "&appid=" + info.getAppid() + "&access_by=" + accessBy + "&extra=" + Uri.encode(info.getExtra(), "UTF-8");

            if (info.getLocal_path() == null) {
                if (Constant.IS_DEBUG_MODE) {
                    System.out.println("logUpload error!上传的本地无log");
                }
                return result;
            }
            for (int i = 0; i < info.getLocal_path().length; i++) {
                try {
                    StringBuilder url = new StringBuilder(requestUrl);
                    String localPath = info.getLocal_path()[i];
                    if (localPath == null) {
                        continue;
                    }
                    String path = GoomeLog.getInstance().getLogGZipPath(localPath);
                    if (path == null) {
                        continue;
                    }
                    MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                    File file = new File(path);
                    if (file == null || !file.exists()) {
                        if (Constant.IS_DEBUG_MODE) {
                            System.out.println("logUpload error!上传的本地log不存在！" + i);
                        }
                        result.statusCode = Result.FAIL;

                        continue;
                    }
                    url.append("&md5=");
                    url.append(Md5Util.fileMD5(path));
                    url.append("&size=");
                    url.append(file.length());
                    reqEntity.addPart("content", new FileBody(file));
                    if (Constant.IS_DEBUG_MODE) {
                        System.out.println("logUpload-----------");//+ url.toString()
                    }

                    content = httpToServerRequestOrUpData(url.toString(), server, result, false, current, true, reqEntity, isHttps);
                    file.deleteOnExit(); // 删除压缩包
                    if (content != null && !content.equals("{}")) {
                        JSONObject json = new JSONObject(content);
                        if (result.success) {
                            result.statusCode = Result.OK;
                            GoomeLog.getInstance().deleteFile(i);
                        } else {
                            result.statusCode = json.optInt("errcode");
                            result.errorMessage = json.optString("msg");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//for
        } catch (Exception e) {
            processException(result, e, content);
        }
        GoomeLogUtil.setUploadFailedErrorLog(!result.success);
        return result;
    }

    public Result logGetConfig(LogUploadInfo info) {
        Result result = new Result();
        String content = null;
        String requestUrl = null;
        String accessBy = NetworkUtil.isWifiExtend(mContext) ? "wifi" : "mobile";
        String server = null;
        if (DomainManager.sRespDomainAdd != null) {
            server = DomainManager.sRespDomainAdd.domainCfg;
        }
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            //http://appcfg.gpsoo.net
            requestUrl = "/1/config?method=get" + "&production=" + Uri.encode(info.getProduction(), "UTF-8") + "&devname=" + Uri.encode(info.getDevname(), "UTF-8") + "&osver=" + info.getOsver() + "&osextra=" + Uri.encode(info.getOsextra(), "UTF-8") + "&appid=" + info.getAppid() + "&access_by=" + accessBy + "&extra=" + Uri.encode(info.getExtra(), "UTF-8");

            if (Constant.IS_DEBUG_MODE) {
                System.out.println("logGetConfig-------------");
            }

            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    JSONObject data = json.optJSONObject("data");
                    AppConfigs appConfigs = new AppConfigs();
                    if (data != null) {
                        appConfigs = new Gson().fromJson(data.toString(), AppConfigs.class);
                    }
                    result.mResult = appConfigs;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    public Result performUpload(String datas) {
        Result result = new Result();
        String content = null;
        String accessBy = NetworkUtil.isWifiExtend(mContext) ? "wifi" : "mobile";
        String server = null;
        if (DomainManager.sRespDomainAdd != null) {
            server = DomainManager.sRespDomainAdd.domainLog;//"test-applog.gpsoo.net";
        }
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        long current = System.currentTimeMillis() / 1000;
        int appId = Constant.COOMIX_APP_ID;
        String extra = AllOnlineApp.sChannelID;

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        NameValuePair nameValuePair = new BasicNameValuePair("content", datas);
        nameValuePairs.add(nameValuePair);

        String requestUrl = "/1/perf?method=uploadperf&access_by=" + accessBy + "&apptype=allonline" + "&appid=" + appId + "&osextra=" + Uri.encode(extra, "UTF-8");
        if (Constant.IS_DEBUG_MODE) {
            System.out.println("performUpload--------");
        }
        try {
            UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
            Header header = new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

            content = httpToServerRequestOrUpData(requestUrl, server, result, false, current, true, reqEntity, isHttps, header);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    JSONObject data = json.optJSONObject("data");
                    result.mResult = data;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
            //			HttpResponse resp = excute(mContext.getApplicationContext(), requestUrl, isHttps, header);
            //			if (resp.getStatusLine() != null) {
            //			    if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            //			        InputStream is = getUngzippedContent(resp.getEntity());
            //			        content = convertStreamToString(is);
            //			        JSONObject json = new JSONObject(content);
            //			        boolean success = json.optBoolean("success");
            //			        if (success) {
            //			            result.statusCode = Result.OK;
            //			            JSONObject data = json.optJSONObject("data");
            //			            result.mResult = data;
            //			        } else {
            //			            result.errorMessage = json.optString("msg");
            //			            result.statusCode = json.optInt("errcode");
            //			        }
            //			    } else {
            //			        result.statusCode = resp.getStatusLine().getStatusCode();
            //			    }
            //			}
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    //----------------
    public static String getFirstIp() {
        String firstIp = PreferenceUtil.getString(Constant.PREFERENCE_DOMAIN_IP, Constant.DOMAIN_ADDRESS_FIRST);
        return firstIp;
    }

    /**
     * 首次取域名缓存IP
     */
    public static void firstSavedIps(String bip) {
        PreferenceUtil.commitString(Constant.PREFERENCE_DOMAIN_IP, bip);
    }

    public static String getCookie() {
        String cookiesString = PreferenceUtil.getString(Constant.PREFERENCE_CAR_COOKIE, "");
        return cookiesString;
    }

    /****************
     *  社区相关接口
     ***************/
    /**
     * 修改用户信息
     *
     * @param map
     */
    public Result userModify(HashMap<String, Object> map) {
        Result resp = new Result();
        String content = null;
        try {
            String requestUrl;
            requestUrl = "/1/user?method=modify";
            for (String str : map.keySet()) {
                requestUrl += "&" + str + "=" + map.get(str);
            }
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl);
            }
            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 获取用户信息
     *
     * @param uid
     * @param ticket
     * @return
     */
    public Result userDetailInfo(String uid, String ticket) {
        Result resp = new Result();
        String content = null;
        try {
            String requestUrl;
            // uid = 0时上传"",表示获取用户本人信息，uid不为0则获取指定uid用户的信息
            requestUrl = "/1/user?method=detailInfo&uid=" + uid + "&ticket=" + (ticket != null ? URLEncoder.encode(ticket, "UTF-8") : "");
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl);
            }
            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);

            JSONObject json = new JSONObject(content);
            if (resp.success) {
                JSONObject jsonObj = json.optJSONObject("data");
                CommunityUser data = new Gson().fromJson(jsonObj.toString(), CommunityUser.class);
                if ("0".equals(uid)) {
                    data.setAccount(AllOnlineApp.sAccount);
                    data.setTicket(ticket);
                }
                resp.mResult = data;
            }
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    public Result queryBaseProfile(int hashcode, String ticket, String uids) {
        Result resp = new Result();
        String content = null;
        try {
            String requestUrl;
            requestUrl = "/1/user?method=queryBaseProfile&ticket=" + (ticket != null ? URLEncoder.encode(ticket, "UTF-8") : "") + "&uids=" + uids;

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);

            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false);

            JSONObject json = new JSONObject(content);
            if (resp.success) {
                JSONObject dataObj = json.optJSONObject("data");
                JSONArray jArray = dataObj.optJSONArray("profiles");
                ArrayList<CommunityUser> users = new ArrayList<CommunityUser>();
                JSONObject tmp = null;
                Gson gson = new Gson();
                for (int i = 0; i < jArray.length(); i++) {
                    tmp = jArray.getJSONObject(i);
                    if (tmp != null) {
                        CommunityUser user = gson.fromJson(tmp.toString(), CommunityUser.class);
                        users.add(user);
                    }
                }
                resp.mResult = users;
            }
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 图片上传
     *
     * @param ticket
     * @param path
     * @param mimeType
     * @return
     */
    public Result pictureUpload(String ticket, String path, String mimeType) {
        Result resp = new Result();
        String content = null;
        try {
            String requestUrl;
            requestUrl = "/1/picture?method=upload";
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl);
            }
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            File file = new File(path);
            if (file == null || !file.exists()) {
                if (Constant.IS_DEBUG_MODE) {
                    Log.e(TAG, "pictureUpload error!上传的本地图片出问题了！--不存在！");
                }
                return resp;
            }
            reqEntity.addPart("content", new FileBody(file, file.getName(), mimeType, "UTF-8"));
            if (!CommonUtil.isEmptyTrimStringOrNull(ticket)) {
                reqEntity.addPart("ticket", new StringBody(ticket));
            }
            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY_PICUP, resp, false, t, true, reqEntity, false);

            JSONObject json = new JSONObject(content);
            CommunityImageInfo info = null;
            if (resp.success) {
                JSONObject jsonObj = json.optJSONObject("data");
                info = new Gson().fromJson(jsonObj.toString(), CommunityImageInfo.class);
            }
            resp.mResult = info;
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 获取活动列表
     *
     * @param type
     * @param citycode
     * @param last_pointer
     * @param last_id
     * @param num
     * @return
     */
    public Result getActivityList(int type, String citycode, int category_id, double last_pointer, String last_id, long num) {
        Result resp = new Result();
        String content = null;
        try {
            String requestUrl;
            requestUrl = "/1/activity?method=getList&type=" + type + "&citycode=" + citycode + "&last_pointer=" + (last_pointer == 0 ? "0" : CommunityUtil.doubleToString(last_pointer, 6)) + (num > 0 ? "&num=" + num : "") + (CommunityUtil.isEmptyTrimStringOrNull(last_id) ? "" : "&last_id=" + last_id);
            if (category_id >= 0) {
                requestUrl += "&category_id=" + category_id;
            }
            if (Constant.IS_DEBUG_MODE) {
                Log.d(TAG, "getActivityList requestUrl=" + requestUrl);
            }
            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);

            JSONObject json = new JSONObject(content);
            if (resp.success) {
                JSONObject dataObj = json.optJSONObject("data");
                CommunityActs acts = new Gson().fromJson(dataObj.toString(), CommunityActs.class);
                resp.mResult = acts;
            }
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 获取活动详情
     *
     * @param ticket
     * @param aid
     * @return
     */
    public Result getActivityDetail(String ticket, int aid) {
        Result resp = new Result();
        String content = null;
        try {
            String requestUrl;
            requestUrl = "/1/activity?method=getDetail&id=" + aid + "&ticket=" + (ticket != null ? URLEncoder.encode(ticket, "UTF-8") : "");
            requestUrl += "&maptype=" + MAP_TYPE_GOOGLE;
            if (Constant.IS_DEBUG_MODE) {
                Log.d(TAG, "getActivityDetail requestUrl=" + requestUrl);
            }
            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);

            JSONObject json = new JSONObject(content);
            if (resp.success) {
                JSONObject dataObj = json.optJSONObject("data");
                CommunityActDetail communityAct = new Gson().fromJson(dataObj.toString(), CommunityActDetail.class);
                resp.mResult = communityAct;
            }
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 报名活动
     *
     * @param ticket
     * @param type
     * @param aid
     * @param name
     * @param tel
     * @param qqorwx
     * @return
     */
    public Result sendSignInfo(String ticket, int type, int aid, long order_id, String name, String tel, String qqorwx, String addr, String extend_items, double lat, double lon) {
        Result resp = new Result();
        String content = null;
        try {
            String requestUrl;
            requestUrl = "/1/activity?method=apply&type=" + type + "&aid=" + aid + "&name=" + getUrlEncoder(name) + "&tel=" + tel + "&qqorwx=" + qqorwx + "&addr=" + getUrlEncoder(addr) + "&ticket=" + (ticket != null ? URLEncoder.encode(ticket, "UTF-8") : "");
            requestUrl += "&maptype=" + MAP_TYPE_GOOGLE;
            if (Constant.IS_DEBUG_MODE) {
                Log.d(TAG, "sendSignInfo requestUrl=" + requestUrl);
            }
            if (order_id >= 0) {
                requestUrl += ("&order_id=" + order_id);
            }
            if (!TextUtils.isEmpty(extend_items)) {
                requestUrl += ("&extend_items=" + getUrlEncoder(extend_items));
            }
            /*MultipartEntity params = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            params.addPart("type", new StringBody(String.valueOf(type)));
            params.addPart("aid", new StringBody(String.valueOf(aid)));
            params.addPart("name", new StringBody(name));
            params.addPart("tel", new StringBody(tel));
            params.addPart("qqorwx", new StringBody(qqorwx));
            params.addPart("addr", new StringBody(addr));*/

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);

            JSONObject json = new JSONObject(content);
            if (resp.success) {
                try {
                    JSONObject dataObj = json.optJSONObject("data");
                    ActOrderInfo actOrderInfo = new Gson().fromJson(dataObj.toString(), ActOrderInfo.class);
                    resp.mResult = actOrderInfo;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 获取我报名的活动信息
     *
     * @param ticket
     * @param aid
     * @return
     */
    public Result getMySignedInfo(String ticket, int aid) {
        Result resp = new Result();
        String content = null;
        try {
            String requestUrl;
            requestUrl = "/1/activity?method=getApplyInfo&aid=" + aid + "&ticket=" + (ticket != null ? URLEncoder.encode(ticket, "UTF-8") : "");
            requestUrl += "&maptype=" + MAP_TYPE_GOOGLE;
            if (Constant.IS_DEBUG_MODE) {
                Log.d(TAG, "getMySignedInfo requestUrl=" + requestUrl);
            }
            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);

            JSONObject json = new JSONObject(content);
            if (resp.success) {
                JSONObject dataObj = json.optJSONObject("data");
                CommunitySignedInfo signedInfo = new Gson().fromJson(dataObj.toString(), CommunitySignedInfo.class);
                JSONObject addrObj = dataObj.optJSONObject("extend");
                if (addrObj != null) {
                    signedInfo.setAddr(addrObj.optString("addr"));
                    signedInfo.setLat(addrObj.optString("lat"));
                    signedInfo.setLon(addrObj.optString("lon"));
                }
                resp.mResult = signedInfo;
            }
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }


    public Result getAllAd(int sWidth, int sHeight, double lat, double lng, String cityCode) {
        Result response = new Result();
        String content = null;
        try {
            String requestUrl;
            requestUrl = "/v1/bus/mbcommonservice?method=getallad" + "&width=" + sWidth + "&height=" + sHeight + "&lat=" + lat + "&lng=" + lng + "&citycode=" + cityCode;
            if(!TextUtils.isEmpty(AllOnlineApp.sAccount))
            {
                requestUrl = requestUrl + "&login_name=" + getUrlEncoder(AllOnlineApp.sAccount);
            }
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl);
            }
            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            String domain = Constant.DOMAIN_ADDRESS_AD;
            if (DomainManager.sRespDomainAdd != null && !CommunityUtil.isEmptyTrimStringOrNull(DomainManager.sRespDomainAdd.domainAd)) {
                domain = DomainManager.sRespDomainAdd.domainAd;
            }

            //Todo For test only
            //domain = "bustest.gpsoo.net";

            content = httpToServerRequestOrUpData(requestUrl, domain, response, false, t);

            JSONObject json = new JSONObject(content);
            if (response.success) {
                response.statusCode = Result.OK;

                JSONArray jsonArray = json.optJSONArray("data");
                List<Adver> adverList = new ArrayList<Adver>();
                if (jsonArray != null) {
                    for (int i = 0, len = jsonArray.length(); i < len; i++) {
//                        JSONObject jsonObjItem = jsonArray.getJSONObject(i);
//                        Adver adver = new Adver();
//                        adver.adverId = jsonObjItem.optInt("id");
//                        adver.adverUrl = jsonObjItem.optString("picurl").trim();
//                        adver.adverJpumpUrl = jsonObjItem.optString("jumpurl").trim();
//                        adver.name = jsonObjItem.optString("name");
//                        adver.type = Integer.valueOf(jsonObjItem.optString("type"));
//                        if (adver.type == 1) {
//                            adver.adverType = AdType.AdTypeLaunch;
//                        } else if (adver.type == 2) {
//                            adver.adverType = AdType.AdTypeHome;
//                        } else if (adver.type == 3) {
//                            adver.adverType = AdType.AdTypeTransfer;
//                        } else if (adver.type == 4) {
//                            adver.adverType = AdType.AdTypeWindow;
//                        }
//                        adver.md5 = jsonObjItem.optString("md5");
//                        adverList.add(adver);
                    }
                }
                response.mResult = getSortADList(adverList);
            }
        } catch (Exception e) {
            processException(response, e, content);
        }
        return response;
    }


    /**
     * 登录
     *
     * @return response
     */
    public Result login(String access_token, String cid, String loginname) {
        Result resp = new Result();

        String content = null;
        try {
            String requestUrl;
            requestUrl = "/1/login?method=bycaronline&access_token=" + getUrlEncoder(access_token)
                    + "&cid=" + getUrlEncoder(cid) + "&loginname=" + getUrlEncoder(loginname);
            requestUrl += "&posmaptype=" + MAP_TYPE_GOOGLE;
            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl);
            }

            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);
            JSONObject json = new JSONObject(content);

            CommunityUser user = null;
            if (resp.success) {
                resp.statusCode = Result.OK;
                JSONObject jsonObj = json.optJSONObject("data");
                if (jsonObj != null) {
                    String ticket = jsonObj.optString("ticket");
                    String hxAccount = jsonObj.optString("hxAccount");
                    String hxPwd = jsonObj.optString("hxPwd");

                    //save customerId and avatar to sharedPreference
                    String customerId = jsonObj.optString(CommonService.CUSTOMER_ID);
                    if (customerId != null) {
                        PreferenceUtil.commitString(CommonService.CUSTOMER_ID, customerId);
                    }
                    String customerAvatar = jsonObj.optString(CommonService.CUSTOMER_AVATAR);
                    if (customerAvatar != null) {
                        PreferenceUtil.commitString(CommonService.CUSTOMER_AVATAR, customerAvatar);
                    }

//                    JSONObject dataObj = jsonObj.getJSONObject("userinfo");
//                    if (dataObj != null) {
//                        user = new Gson().fromJson(dataObj.toString(), CommunityUser.class);
//                        user.setTicket(ticket);
//                        user.setHxAccount(hxAccount);
//                        user.setHxPwd(hxPwd);
//                        user.setAccount(AllOnlineApp.sAccount);
//                        AllOnlineApp.getInstantce().setCommunityUser(user);
//                        CommunityDbHelper.getInstance(AllOnlineApp.mApp).saveAccountInfo(user);
//                        resp.mResult = user;
//                    }
                }
            } else {
                String fileMethodLine = "";
                fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
                fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
                fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
                GoomeLog.getInstance().logE(fileMethodLine, "URL:  " + resp.debugUrl + " content: " + content, 0);
            }
        } catch (Exception e) {
            Log.e("ttt", "Exception", e);
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 绑定gpns推送cid
     *
     * @param ticket
     * @param cid
     * @return
     */
    public Result bindChannelId(String ticket, String cid) {
        Result resp = new Result();
        String content = null;
        try {
            StringBuilder requestUrl = new StringBuilder();
            requestUrl.append("/1/user?method=bindPush");
            requestUrl.append("&ticket=").append(getUrlEncoder(ticket));
            requestUrl.append("&cid=").append(getUrlEncoder(cid));

            long t = System.currentTimeMillis() / 1000;
            requestUrl.append(getUrlSign(t));
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 运营账号登陆
     *
     * @param name
     * @param pwd
     * @param regist
     * @return
     */
    public Result loginInner(String name, String pwd, int regist) {
        Result resp = new Result();
        String content = null;
        try {
            String requestUrl;
            requestUrl = "/1/login?method=byname&name=" + URLEncoder.encode(name, "UTF-8") + "&pwd=" + URLEncoder.encode(pwd, "UTF-8") + "&regist=" + regist;

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl);
            }

            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);

            JSONObject json = new JSONObject(content);
            CommunityUser user = null;
            if (resp.success) {
                JSONObject jsonObj = json.optJSONObject("data");
                if (jsonObj != null) {
                    String ticket = jsonObj.optString("ticket");
                    String hxAccount = jsonObj.optString("hxAccount");
                    String hxPwd = jsonObj.optString("hxPwd");
                    JSONObject dataObj = jsonObj.getJSONObject("userinfo");
//                    if (dataObj != null) {
//                        user = new Gson().fromJson(dataObj.toString(), CommunityUser.class);
//                        user.setTicket(ticket);
//
//                        user.setHxAccount(hxAccount);
//                        user.setOperationSpecialist(true);
//                        user.setHxPwd(hxPwd);
//                        AllOnlineApp.sToken = new Token();
//                        AllOnlineApp.sToken.name = "运营人员" + user.getUid();
//                        AllOnlineApp.sAccount = Constant.INNER_ACCOUNT + user.getUid();
//                        user.setAccount(AllOnlineApp.sAccount);
//                        AllOnlineApp.getInstantce().setCommunityUser(user);
//                        CommunityDbHelper.getInstance(AllOnlineApp.mApp).saveAccountInfo(user);
//                    }
                }
            }
            resp.mResult = user;
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 退出登录
     *
     * @param ticket    登录签名
     * @param channelId 推送的channelID
     * @return response
     */
    public Result logoutCommunity(String ticket, String channelId) {
        Result resp = new Result();
        String content = null;
        try {
            StringBuilder requestUrl = new StringBuilder();
            requestUrl.append("/1/login?method=logout");
            requestUrl.append("&ticket=").append(getUrlEncoder(ticket));
            requestUrl.append("&cid=").append(getUrlEncoder(channelId));
            requestUrl.append("&lang=zh-CN");
            long t = System.currentTimeMillis() / 1000;
            requestUrl.append(getUrlSign(t));

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 获取随机码
     *
     * @return response
     */
    public Result getRandomCode() {
        Result resp = new Result();
        String content = null;
        try {
            String requestUrl = "/1/login?method=getCaptchaRandom";
            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl);
            }
            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);
            JSONObject json = new JSONObject(content);
            String random = "";
            if (resp.success) {
                JSONObject jsonObj = json.optJSONObject("data");
                if (jsonObj != null) {
                    random = jsonObj.optString("random");
                }
            }
            resp.mResult = random;
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 获取验证码
     *
     * @return response
     */
    public Result getSmsCode(String phone, String randomCode) {
        Result resp = new Result();
        String content = null;
        try {
            long t = System.currentTimeMillis() / 1000;
            String encryptStr = OSUtil.toMD5(t + Constant.PRIVATE_KEY + Constant.PRIVATE_PACKAGE);
            String telsec = security.getSecInfo(new StringBuilder().append(phone).append("|").append(randomCode).toString(), encryptStr, COMMUNITY_COOMIX_VERSION);
            String requestUrl = "/1/login?method=applycaptcha&tel=" + phone + "&telsec=" + getUrlEncoder(telsec);
            requestUrl += getUrlSign(t);
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl);
            }
            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);

            JSONObject json = new JSONObject(content);
            int cd = 60;
            if (resp.success) {
                JSONObject jsonObj = json.optJSONObject("data");
                if (jsonObj != null) {
                    cd = jsonObj.optInt("cd");
                }
            }
            resp.mResult = cd;
        } catch (Exception e) {
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 绑定手机号
     *
     * @return response
     */
    public Result bindPhone(String access_token, String ticket, String cid, String phone, String smsCode, boolean force) {
        Result resp = new Result();
        String content = null;
        try {
            StringBuilder sb = new StringBuilder("/1/login?method=bindphone");
            sb.append("&access_token=").append(getUrlEncoder(access_token)).append("&ticket=").append(getUrlEncoder(ticket)).append("&cid=").append(getUrlEncoder(cid)).append("&phone=").append(phone).append("&captcha=").append(smsCode).append("&cover=").append(force);
            long t = System.currentTimeMillis() / 1000;
            sb.append(getUrlSign(t));
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, sb.toString());
            }
            content = httpToServerRequestOrUpData(sb.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);

            JSONObject json = new JSONObject(content);
            CommunityUser user = null;
            if (resp.success) {
                JSONObject jsonObj = json.optJSONObject("data");
//                if (jsonObj != null && jsonObj.has("userinfo")) {
//                    String resTicket = jsonObj.optString("ticket");
//                    if (CommunityUtil.isEmptyTrimStringOrNull(resTicket)) {
//                        resTicket = ticket;
//                    }
//                    JSONObject dataObj = jsonObj.getJSONObject("userinfo");
//                    if (dataObj != null) {
//                        user = new Gson().fromJson(dataObj.toString(), CommunityUser.class);
//                        user.setTicket(resTicket);
//                        user.setAccount(AllOnlineApp.sAccount);
//                        AllOnlineApp.getInstantce().setCommunityUser(user);
//                        CommunityDbHelper.getInstance(AllOnlineApp.mApp).saveAccountInfo(user);
//                        resp.mResult = user;
//                    }
//                    /*JSONObject dataObj = jsonObj.getJSONObject("userinfo");
//                    if (dataObj != null) {
//                        user = new Gson().fromJson(dataObj.toString(), CommunityUser.class);
//                        user.setTicket(ticket);
//                        user.setAccount(AllOnlineApp.sAccount);
//                        AllOnlineApp.getInstantce().setCommunityUser(user);
//                        CommunityDbHelper.getInstance(AllOnlineApp.getInstantce()).saveAccountInfo(user);
//                        resp.mResult = user;
//                    }*/
//                } else {
//                    if (AllOnlineApp.getCommunityUser().getAccount() != null) {
//                        CommunityDbHelper.getInstance(AllOnlineApp.mApp).updateTel(AllOnlineApp.getCommunityUser().getAccount(), phone);
//                    }
//                    AllOnlineApp.getCommunityUser().setTel(phone);
//                }
            }
        } catch (Exception e) {
            if (Constant.IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 更换手机号
     *
     * @return response
     */
    public Result modifyPhone(String access_token, String ticket, String phone, String smsCode) {
        Result resp = new Result();
        String content = null;
        try {
            StringBuilder sb = new StringBuilder("/1/login?method=modifyphone");
            sb.append("&access_token=").append(getUrlEncoder(access_token)).append("&ticket=").append(getUrlEncoder(ticket)).append("&phone=").append(phone).append("&captcha=").append(smsCode);
            long t = System.currentTimeMillis() / 1000;
            sb.append(getUrlSign(t));
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, sb.toString());
            }
            content = httpToServerRequestOrUpData(sb.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, resp, false, t);

            /*JSONObject json = new JSONObject(content);
            CommunityUser user = null;*/
//            if (resp.success) {
//                if (AllOnlineApp.getCommunityUser().getAccount() != null) {
//                    CommunityDbHelper.getInstance(AllOnlineApp.mApp).updateTel(AllOnlineApp.getCommunityUser().getAccount(), phone);
//                }
//                AllOnlineApp.getCommunityUser().setTel(phone);
//            }
        } catch (Exception e) {
            if (Constant.IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            processException(resp, e, content);
        }
        return resp;
    }

    /**
     * 红包-----界面配置
     */
    public Result getRedPacketConfig(int hashcode, String ticket, int display_type) {
        Result result = new Result();
        String content = null;
        try {
            String requestUrl = "/1/redpacket?method=getConfig&display_type=" + display_type;
            if (!TextUtils.isEmpty(ticket)) {
                requestUrl += "&ticket=" + getUrlEncoder(ticket);
            }

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    RedPacketConfig redPacketConfig = (RedPacketConfig) new Gson().fromJson(dataObj.toString(), RedPacketConfig.class);
                    result.mResult = redPacketConfig;
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    /**
     * 钱包-----查询零钱余额
     */
    public Result getPocketBalance(int hashcode, String ticket) {
        Result result = new Result();
        String content = null;
        try {
            String requestUrl = "/1/wallet?method=getBalance";
            if (!TextUtils.isEmpty(ticket)) {
                requestUrl += "&ticket=" + getUrlEncoder(ticket);
            }

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl);
            }

            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    result.mResult = new Gson().fromJson(dataObj.toString(), BalanceInfo.class);
                    //result.mResult = dataObj.optLong("balance");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    public static final String MAP_TYPE_GOOGLE = "GOOGLE";

    /**
     * 红包-----包红包--支付
     */
    public Result createRedPacket(int hashcode, String ticket, CreateRedPacketInfo createRedPacketInfo) {
        Result result = new Result();
        String content = null;
        try {
            String requestUrl = "/1/redpacket?method=create";
            if (!TextUtils.isEmpty(ticket)) {
                requestUrl += "&ticket=" + getUrlEncoder(ticket);
            }
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("&display_type=" + createRedPacketInfo.getDisplay_type());
            stringBuffer.append("&packet_type=" + createRedPacketInfo.getPacket_type());
            stringBuffer.append("&amount=" + createRedPacketInfo.getAmount());
            stringBuffer.append("&pay_platform=" + createRedPacketInfo.getPay_platform());
            stringBuffer.append("&pay_manner=" + createRedPacketInfo.getPay_manner());
            stringBuffer.append("&packet_num=" + createRedPacketInfo.getPacket_num());
            stringBuffer.append("&hello_words=" + getUrlEncoder(createRedPacketInfo.getHello_words()));
            stringBuffer.append("&alloc_range=" + createRedPacketInfo.getAlloc_range());
            stringBuffer.append("&posmaptype=").append(MAP_TYPE_GOOGLE);
            stringBuffer.append("&extend_item=" + getUrlEncoder(new Gson().toJson(createRedPacketInfo.getExtend_item())));

            requestUrl += stringBuffer.toString();

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl, Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    CoomixPayRsp coomixPayRsp = new Gson().fromJson(dataObj.toString(), CoomixPayRsp.class);
                    result.mResult = coomixPayRsp;
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    /**
     * 红包-----查询未发送的红包 display_type--1群红包，2社区红包，3私信红包
     */
    public Result getUnsendRedPacketsByType(int hashcode, String ticket, int display_type) {
        Result result = new Result();
        String content = null;
        try {
            StringBuilder requestUrl = new StringBuilder("/1/redpacket?method=getUnsentByType");
            requestUrl.append("&display_type=").append(display_type);
            requestUrl.append("&maptype=").append(MAP_TYPE_GOOGLE);
            if (!TextUtils.isEmpty(ticket)) {
                requestUrl.append("&ticket=").append(getUrlEncoder(ticket));
            }

            long t = System.currentTimeMillis() / 1000;
            requestUrl.append(getUrlSign(t));

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    result.mResult = new Gson().fromJson(dataObj.toString(), UnsendRedPackets.class);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    /**
     * 红包-----拆红包
     */
    public Result openRedPacket(int hashcode, String ticket, String redpacket_id, String mapType, String lat, String lng) {
        Result result = new Result();
        String content = null;
        try {
            StringBuilder requestUrl = new StringBuilder("/1/redpacket?method=allocate");
            requestUrl.append("&redpacket_id=").append(redpacket_id);
            if (!TextUtils.isEmpty(ticket)) {
                requestUrl.append("&ticket=").append(getUrlEncoder(ticket));
            }
            requestUrl.append("&posmaptype=").append(mapType);
            requestUrl.append("&maptype=").append(MAP_TYPE_GOOGLE);
            requestUrl.append("&lat=").append(lat);
            requestUrl.append("&lng=").append(lng);
            requestUrl.append("&extend=").append(RpLocationUtil.getExtendInfo(mContext));

            long t = System.currentTimeMillis() / 1000;
            requestUrl.append(getUrlSign(t));

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    result.mResult = new Gson().fromJson(dataObj.toString(), RedPacketInfo.class);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    /**
     * 红包-----根据红包id查询红包信息
     */
    public Result getRedPacketInfoById(int hashcode, String ticket, String redpacket_id, double last_pointer, int num, String mapType, String lat, String lng, int alloc_num) {
        Result result = new Result();
        String content = null;
        try {
            StringBuilder requestUrl = new StringBuilder("/1/redpacket?method=getInfoById");
            requestUrl.append("&redpacket_id=").append(redpacket_id);
            requestUrl.append("&last_pointer=").append(last_pointer);
            requestUrl.append("&num=").append(num);
            if (!TextUtils.isEmpty(ticket)) {
                requestUrl.append("&ticket=").append(getUrlEncoder(ticket));
            }
            requestUrl.append("&posmaptype=").append(mapType);
            requestUrl.append("&maptype=").append(MAP_TYPE_GOOGLE);
            if (lat != null && lng != null) {
                requestUrl.append("&lat=").append(lat);
                requestUrl.append("&lng=").append(lng);
            }
            if (alloc_num > 0) {
                requestUrl.append("&alloc_num=").append(alloc_num);
            }

            long t = System.currentTimeMillis() / 1000;
            requestUrl.append(getUrlSign(t));

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    RedPacketInfo redPacketInfo = new Gson().fromJson(dataObj.toString(), RedPacketInfo.class);
                    result.mResult = redPacketInfo;
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    public Result rechargeBalance(int hashcode, String ticket, long amount, int pay_platform, int pay_manner) {
        Result result = new Result();
        String content = null;
        try {
            StringBuilder requestUrl = new StringBuilder("/1/wallet?method=recharge");
            requestUrl.append("&ticket=").append(getUrlEncoder(ticket));
            requestUrl.append("&amount=").append(amount);
            requestUrl.append("&pay_platform=").append(pay_platform);
            requestUrl.append("&pay_manner=").append(pay_manner);

            long t = System.currentTimeMillis() / 1000;
            requestUrl.append(getUrlSign(t));

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    result.mResult = new Gson().fromJson(dataObj.toString(), CoomixPayRsp.class);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    public Result withdrawBalance(int hashcode, String ticket, String account, long amount) {
        Result result = new Result();
        String content = null;
        try {
            StringBuilder requestUrl = new StringBuilder("/1/wallet?method=withdraw");
            requestUrl.append("&ticket=").append(getUrlEncoder(ticket));
            requestUrl.append("&account=").append(account);
            requestUrl.append("&amount=").append(amount);

            long t = System.currentTimeMillis() / 1000;
            requestUrl.append(getUrlSign(t));

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    JSONObject error = dataObj.optJSONObject("error");
                    if (error != null) {
                        result.mResult = new Gson().fromJson(error.toString(), Error.class);
                    }
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    /**
     * 更新显示单个活动费用是否已支付的状态
     */
    public Result getRefreshPayOrderStatus(int hashcode, int aid, long order_id) {
        Result result = new Result();
        String content = null;
        try {
            String requestUrl = "/1/activity?method=refresh_pay_order_status&aid=" + aid + "&order_id=" + order_id;
            String ticket = CommonUtil.getTicket();
            if (!TextUtils.isEmpty(ticket)) {
                requestUrl += "&ticket=" + getUrlEncoder(ticket);
            }

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    OrderStatusRsp orderStatusRsp = new Gson().fromJson(dataObj.toString(), OrderStatusRsp.class);
                    result.mResult = orderStatusRsp;
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    public Result getRechargeStatus(int hashcode, String ticket, long order_id) {
        Result result = new Result();
        String content = null;
        try {
            StringBuilder requestUrl = new StringBuilder("/1/wallet?method=refreshOrder");
            requestUrl.append("&ticket=").append(getUrlEncoder(ticket));
            requestUrl.append("&order_id=").append(order_id);

            long t = System.currentTimeMillis() / 1000;
            requestUrl.append(getUrlSign(t));
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);


            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    result.mResult = new Gson().fromJson(dataObj.toString(), OrderStatusRsp.class);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    /**
     * 红包-----发送红包
     */
    public Result sendRedPacket(int hashcode, String ticket, String redpacket_id) {
        Result result = new Result();
        String content = null;
        try {
            String requestUrl = "/1/redpacket?method=send&redpacket_id=" + redpacket_id;
            if (!TextUtils.isEmpty(ticket)) {
                requestUrl += "&ticket=" + getUrlEncoder(ticket);
            }

            long t = System.currentTimeMillis() / 1000;
            requestUrl += (getUrlSign(t));
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    public Result getMyActivityList(int hashcode, String ticket, double lastPointer, String lastId, long num) {
        Result result = new Result();
        String content = null;
        try {
            StringBuilder requestUrl = new StringBuilder();
            requestUrl.append("/1/activity?method=getJoinList");
            requestUrl.append("&ticket=").append(getUrlEncoder(ticket));
            requestUrl.append("&last_pointer=").append(lastPointer);
            requestUrl.append("&last_id=").append(lastId);
            requestUrl.append("&num=").append(num);

            long t = System.currentTimeMillis() / 1000;
            requestUrl.append(getUrlSign(t));
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    result.mResult = new Gson().fromJson(dataObj.toString(), MyActivities.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 支付活动费用
     */
    public Result prepay(int hashcode, int aid, long order_id, int pay_platform, int pay_manner) {
        Result result = new Result();
        String content = null;
        try {
            String requestUrl = "/1/activity?method=prepay&aid=" + aid + "&pay_platform=" + pay_platform + "&pay_manner=" + pay_manner + "&order_id=" + order_id;
            String ticket = CommonUtil.getTicket();
            if (!TextUtils.isEmpty(ticket)) {
                requestUrl += "&ticket=" + getUrlEncoder(ticket);
            }

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    CoomixPayRsp coomixPayRsp = new Gson().fromJson(dataObj.toString(), CoomixPayRsp.class);
                    result.mResult = coomixPayRsp;
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    public Result getOrderInfo(int hashcode, String ticket, int actId, long order_id, String mapType) {
        Result result = new Result();
        String content = null;
        try {
            StringBuilder requestUrl = new StringBuilder();
            requestUrl.append("/1/activity?method=getOrderInfo");
            requestUrl.append("&ticket=").append(getUrlEncoder(ticket));
            requestUrl.append("&id=").append(actId);
            requestUrl.append("&order_id=").append(order_id);
            requestUrl.append("&maptype=").append(mapType);

            long t = System.currentTimeMillis() / 1000;
            requestUrl.append(getUrlSign(t));
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(ActCommitItem.class, new JsonDeserializer<ActCommitItem>() {
                        @Override
                        public ActCommitItem deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                            if (jsonElement.isJsonObject()) {
                                JsonObject jsonObject = jsonElement.getAsJsonObject();
                                ActCommitItem actCommitItem = new ActCommitItem();
                                actCommitItem.parseValue(jsonObject);
                                return actCommitItem;
                            }
                            return null;
                        }
                    });
                    gsonBuilder.registerTypeAdapter(CommitExtendItem.class, new JsonDeserializer<CommitExtendItem>() {
                        @Override
                        public CommitExtendItem deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                            if (jsonElement.isJsonObject()) {
                                JsonObject jsonObject = jsonElement.getAsJsonObject();
                                CommitExtendItem item = new CommitExtendItem();
                                item.parseObjectValue(jsonObject);
                                return item;
                            }
                            return null;
                        }
                    });
                    Gson gs = gsonBuilder.create();
                    result.mResult = gs.fromJson(dataObj.toString(), ActOrderInfo.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Result getDailyRedpacket(int hashcode, String citycode, String posmaptype, String lat, String lng) {
        Result result = new Result();
        String content = null;
        if (TextUtils.isEmpty(posmaptype)) {
            posmaptype = MAP_TYPE_GOOGLE;
        }
        try {
            String requestUrl = "/1/redpacket?method=getDailyRedpacket&posmaptype=" + posmaptype + "&lat=" + lat + "&lng=" + lng + "&citycode=" + citycode;

            String ticket = CommonUtil.getTicket();
            if (!TextUtils.isEmpty(ticket)) {
                requestUrl += "&ticket=" + getUrlEncoder(ticket);
            }

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                result.statusCode = Result.OK;
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    result.mResult = new Gson().fromJson(dataObj.toString(), RedPacketInfo.class);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    public Result painting(int hashcode, String paintType, String mapType) {
        Result result = new Result();
        String content = null;
        if (TextUtils.isEmpty(mapType)) {
            mapType = MAP_TYPE_GOOGLE;
        }
        try {
            String requestUrl = "/1/topic?method=painting&type=" + paintType + "&posmaptype=" + mapType;

            if (CommonUtil.isLogin()) {
                requestUrl += ("&ticket=" + getUrlEncoder(CommonUtil.getTicket()));
            }

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_NATIVEACTIVITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                result.statusCode = Result.OK;
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    result.mResult = new Gson().fromJson(dataObj.toString(), Paint.class);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    public Result rectPaint(int hashcode, String paintType, String mapType, double lat, double lng, int limit) {
        Result result = new Result();
        String content = null;
        if (TextUtils.isEmpty(mapType)) {
            mapType = MAP_TYPE_GOOGLE;
        }
        try {
            String requestUrl = "/1/topic?method=" + paintType + "&posmaptype=" + mapType + "&lat=" + lat + "&lng=" + lng + "&limit=" + limit;

            if (CommonUtil.isLogin()) {
                requestUrl += ("&ticket=" + getUrlEncoder(CommonUtil.getTicket()));
            }

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_NATIVEACTIVITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                result.statusCode = Result.OK;
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null) {
                    result.mResult = new Gson().fromJson(dataObj.toString(), PeopleForActRect.class);
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    public Result getPushAdv(int hashcode) {
        Result result = new Result();
        String content = null;

        try {
            String requestUrl = "/1/login?method=checkpopup";

            if (CommonUtil.isLogin()) {
                requestUrl += ("&ticket=" + getUrlEncoder(CommonUtil.getTicket()));
            }

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);
            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_COMMUNITY, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                result.statusCode = Result.OK;
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null && !dataObj.toString().equals("{}")) {
                    result.mResult = new Gson().fromJson(dataObj.toString(), PushAdv.class);
                }
            } else {
                String fileMethodLine = "";
                fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
                fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
                fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
                GoomeLog.getInstance().logE(fileMethodLine, "getPushAdv Error," + "errcode = " + result.errcode + ",msg = " + result.msg, 0);
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    public Result senUserInfo(String ticket, String uid, String acttype, String recordhis) {
        Result result = new Result();
        String content = null;

        try {
            String requestUrl = "/1/community?method=senduserinfo";

            requestUrl += ("&uid=" + uid);
            requestUrl += ("&ticket=" + getUrlEncoder(ticket));

            long t = System.currentTimeMillis() / 1000;
            requestUrl += getUrlSign(t);

            requestUrl += ("&acttype=" + acttype);
            requestUrl += ("&recordhis=" + recordhis);

            requestUrl += getPhoneInfo();

            if (Constant.IS_DEBUG_MODE) {
                Log.i(TAG, requestUrl.toString());
            }

            content = httpToServerRequestOrUpData(requestUrl.toString(), Constant.DOMAIN_ADDRESS_USERINFO, result, false, t);

            JSONObject json = new JSONObject(content);
            if (result.success) {
                result.statusCode = Result.OK;
                JSONObject dataObj = json.optJSONObject("data");
                if (dataObj != null && !dataObj.toString().equals("{}")) {
                    //result.mResult = new Gson().fromJson(dataObj.toString(), PushAdv.class);
                }
            } else {
                String fileMethodLine = "";
                fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
                fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
                fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
                GoomeLog.getInstance().logE(fileMethodLine, "senUserInfo Error," + "errcode = " + result.errcode + ",msg = " + result.msg, 0);
            }
        } catch (Exception e) {
            processException(result, e, content);
        }
        return result;
    }

    /**
     * 设备信息接口
     */
    protected String getPhoneInfo() {
        String info = "";
        try {
            info += "&production=" + getUrlEncoder(Build.MODEL);//硬件类型，如Oppo 4，Huawei P8
            info += "&devname=" + getUrlEncoder(Build.DISPLAY);//设备名（用户自己填的）
            info += "&osver=" + Build.VERSION.SDK_INT;//操作系统的版本信息
            info += "&osextra=" + Build.VERSION.RELEASE;//操作系统相关的额外信息
            info += "&extra=" + AllOnlineApp.sChannelID;// gpns通道id
            info += "&isjb=" + (isRoot() ? "1" : "0");//root权限
            //判断手机GPS是否已开启
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSOpened = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
            //app是否开启应用权限
            boolean isLocationOpened = (PermissionChecker.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION, android.os.Process.myPid(), android.os.Process.myUid(), mContext.getPackageName()) == PackageManager.PERMISSION_GRANTED);

            info += "&isGPSOpened=" + ((isGPSOpened && isLocationOpened) ? "1" : "0");
        } catch (Exception e) {
            String fileMethodLine = "";
            fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
            fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
            fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
            GoomeLog.getInstance().logE(fileMethodLine, " info: " + info + "errorMessage: " + e.toString(), 0);
            e.printStackTrace();
        }
        return info;
    }

    /**
     * 判断是否有root权限
     */
    public boolean isRoot() {
        boolean bool = false;
        try {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())) {
                bool = false;
            } else {
                bool = true;
            }
        } catch (Exception e) {
        }
        return bool;
    }

    /**
     * 获取Mac地址
     */

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) {
                    continue;
                }

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 获取微信分享URL
     */
    public Result getLocationShareUrl(String token, String account, final String duration,
                                      String imei, final String logintype) {
        Result result = new Result();
        String requestUrl = null;
        String content = null;

        String server = DomainManager.sRespDomainAdd.domainMain;
        boolean isHttps = DomainManager.sRespDomainAdd.httpsFlag;
        try {
            requestUrl = "/1/account/devinfo?method=app_getWeixinShared" +
                    "&access_type=inner&account=" + Uri.encode(account, "UTF-8") +
                    "&access_token=" + Uri.encode(token, "UTF-8") +
                    "&duration=" + duration +
                    "&imei=" + imei +
                    "&login_type=" + logintype;
            if (Constant.IS_DEBUG_MODE) {
                Log.e("ttt", "getAlarmCategoryList------");
            }
            content = httpToServerRequestOrUpData(requestUrl, server, result, isHttps);
            if (content != null && !content.equals("{}")) {
                JSONObject json = new JSONObject(content);
                if (result.success) {
                    result.statusCode = Result.OK;
                    //JSONObject jo = json.optJSONObject("data");
                    String shareUrl = json.getString("data");//new Gson().fromJson(jo.toString(), String.class);
                    result.mResult = shareUrl;
                } else {
                    result.statusCode = json.optInt("errcode");
                    result.errorMessage = json.optString("msg");
                }
            }
        } catch (Exception e) {
            processException(result, e, content);
            //            Log.e("ttt","Exception",e);
        }
        return result;
    }

    /**
     * URLEncoder.encode(param, "UTF-8").replace("+", "%20")
     *
     * @param param
     * @return
     */
    private String getUrlEncoder(String param) {
        if (param != null) {
            try {
                param = URLEncoder.encode(param, "UTF-8").replace("+", "%20");
                //param = (param != null ? URLEncoder.encode(param, "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return param;
    }
}
