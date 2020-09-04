package com.coomix.app.all.webview;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.service.AllOnlineApiClient;
import com.coomix.app.framework.util.OSUtil;
import com.coomix.app.all.util.CommunityUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ReWebViewClient extends WebViewClient {
    private static final String TAG = ReWebViewClient.class.getSimpleName();
    public static final String APPVER = "appver=";
    public static final String APPID = "appid=";
    public static final String OS = "os=";
    public static final String N = "n=";
    public static final String LAT = "lat=";
    public static final String LNG = "lng=";
    public static final String MAPTYPE = "maptype=";
    public static final String CITY_CODE = "citycode=";
    public static final String TICKET = "ticket=";
    public static final String NBS = "nbs=";
    public static final String NBS_NAME = "nbsname=";

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
        try {
            url = modifyUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        view.loadUrl(url);
        return true;
    }

    /**
     * URL中添加一些私有信息，例如：ticket、经纬度、设备指纹等。在分享出去的URL中必须去掉
     **/
    public static String modifyUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }

        String ver = "";
        String appid = "";
        String n = "";
        String lat = "";
        String lng = "";
        String maptype = AllOnlineApiClient.MAP_TYPE_GOOGLE;
        String cityCode = "";
        String os = "";
        try {
            os = URLEncoder.encode("android", "utf-8");
            ver = URLEncoder.encode(CommunityUtil.getAppVersionNameNoV(AllOnlineApp.mApp), "utf-8");
            appid = URLEncoder.encode(String.valueOf(GlobalParam.getInstance().get_appID()), "utf-8");
            n = URLEncoder.encode(OSUtil.getUdid(AllOnlineApp.mApp), "utf-8");
            lat = URLEncoder.encode(String.valueOf(GlobalParam.getInstance().get_lat()), "utf-8");
            lng = URLEncoder.encode(String.valueOf(GlobalParam.getInstance().get_lng()), "utf-8");
            cityCode = URLEncoder.encode(Constant.COMMUNITY_CITYCODE, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!url.contains(APPVER)) {
            if (!url.contains("?")) {
                url += "?";
            } else if (!url.endsWith("&")) {
                url += "&";
            }
            url = url + APPVER + ver;
        }

        if (!url.contains(APPID)) {
            if (!url.contains("?")) {
                url += "?";
            } else if (!url.endsWith("&")) {
                url += "&";
            }
            url = url + APPID + appid;
        }

        if (!url.contains(OS)) {
            if (!url.contains("?")) {
                url += "?";
            } else if (!url.endsWith("&")) {
                url += "&";
            }
            url = url + OS + os;
        }
        if (!url.contains(N)) {
            if (!url.contains("?")) {
                url += "?";
            } else if (!url.endsWith("&")) {
                url += "&";
            }
            url = url + N + n;
        }
        if (!url.contains(LAT)) {
            if (!url.contains("?")) {
                url += "?";
            } else if (!url.endsWith("&")) {
                url += "&";
            }
            url = url + LAT + lat;
        }
        if (!url.contains(LNG)) {
            if (!url.contains("?")) {
                url += "?";
            } else if (!url.endsWith("&")) {
                url += "&";
            }
            url = url + LNG + lng;
        }
        if (!url.contains(MAPTYPE)) {
            if (!url.contains("?")) {
                url += "?";
            } else if (!url.endsWith("&")) {
                url += "&";
            }
            url = url + MAPTYPE + maptype;
        }
        if (!url.contains(CITY_CODE)) {
            if (!url.contains("?")) {
                url += "?";
            } else if (!url.endsWith("&")) {
                url += "&";
            }
            url = url + CITY_CODE + cityCode;
        }

        // 如果用户登录了则携带ticket信息
        String ticket = AllOnlineApp.sToken.ticket;
        if (!TextUtils.isEmpty(ticket) && !url.contains(TICKET)) {
            try {
                if (!url.contains("?")) {
                    url += "?";
                } else if (!url.endsWith("&")) {
                    url += "&";
                }
                url = url + TICKET + URLEncoder.encode(ticket, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return url;
    }

    /**
     * 移除URL中添加上去的私有信息，例如：ticket、经纬度、设备指纹等
     **/
    public static String removeSelfInfo(String url) {
        if (!TextUtils.isEmpty(url)) {
            StringBuffer stringBuffer = new StringBuffer();
            if (url.contains("&")) {
                String[] soures = url.split("&");

                if (soures != null && soures[0] != null) {
                    if (soures[0].contains(APPVER)) {
                        soures[0] = soures[0].substring(0, soures[0].indexOf(APPVER) - 1);
                    } else if (soures[0].contains(OS)) {
                        soures[0] = soures[0].substring(0, soures[0].indexOf(OS) - 1);
                    } else if (soures[0].contains(N)) {
                        soures[0] = soures[0].substring(0, soures[0].indexOf(N) - 1);
                    } else if (soures[0].contains(LAT)) {
                        soures[0] = soures[0].substring(0, soures[0].indexOf(LAT) - 1);
                    } else if (soures[0].contains(LNG)) {
                        soures[0] = soures[0].substring(0, soures[0].indexOf(LNG) - 1);
                    } else if (soures[0].contains(TICKET)) {
                        soures[0] = soures[0].substring(0, soures[0].indexOf(TICKET) - 1);
                    } else if (soures[0].contains(NBS)) {
                        soures[0] = soures[0].substring(0, soures[0].indexOf(NBS) - 1);
                    } else if (soures[0].contains(NBS_NAME)) {
                        soures[0] = soures[0].substring(0, soures[0].indexOf(NBS_NAME) - 1);
                    }
                    stringBuffer.append(soures[0]);
                }

                if (soures != null && soures.length > 1) {
                    for (int i = 1; i < soures.length; i++) {
                        if (!soures[i].contains(APPVER) && !soures[i].contains(OS) && !soures[i].contains(N)
                            && !soures[i].contains(LAT) && !soures[i].contains(LNG) && !soures[i].contains(TICKET)
                            && !soures[i].contains(NBS) && !soures[i].contains(NBS_NAME)) {
                            if (stringBuffer.toString().contains("?")) {
                                stringBuffer.append("&");
                            } else {
                                stringBuffer.append("?");
                            }
                            stringBuffer.append(soures[i]);
                        }
                    }
                }
            }

            if (!TextUtils.isEmpty(stringBuffer.toString())) {
                url = stringBuffer.toString();
            }
        }
        if (Constant.IS_DEBUG_MODE) {
            Log.i(TAG, "==移除私有信息的url： " + url);
        }
        return url;
    }

    public static String addOsAndVersion(String url) {
        if (!TextUtils.isEmpty(url)) {
            if (url.contains("?")) {
                url = url + "&";
            } else {
                url = url + "?";
            }
            if (!url.contains(OS)) {
                url = url + OS + "android";
            }
            if (!url.contains(APPVER)) {
                if (!url.endsWith("&") && !url.endsWith("?")) {
                    url = url + "&";
                }
                try {
                    url = url + APPVER + URLEncoder.encode(CommunityUtil.getAppVersionNameNoV(AllOnlineApp.mApp),
                        "utf-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return url;
    }
}
