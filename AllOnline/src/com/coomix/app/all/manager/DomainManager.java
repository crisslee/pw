package com.coomix.app.all.manager;

import android.text.TextUtils;

import com.coomix.app.all.Constant;
import com.coomix.app.all.model.bean.RespDomainAdd;
import com.coomix.app.framework.util.PreferenceUtil;
import com.google.gson.Gson;

/**
 * Created by ly on 2017/12/23 14:07.
 */
public class DomainManager {

    private final boolean TEST = false;

    private static DomainManager mDomainManager;
    private static final String FIRST_HOST = "lite.gmiot.net";
    private static final String GOOGLE_ADDRESS_HOST = "maps.google.com";//maps.googleapis.com
    private static final String PRD_COMMUNITY_HOST_DEFAULT = "carolhome.gpsoo.net";
    private static final String PRD_COMMUNITY_PICUP_DEFAULT = "picup.gpsoo.net";

    private static final String PRD_ALLONLINE_DEFAULT_DY = "litin.gmiot.net";
    private static final String PRD_ALLONLINE_DEFAULT_WEB = "lite.gmiot.net";
    private static final String PRD_ALLONLINE_DEFAULT_AD = "ad.gpsoo.net";
    private static final String PRD_ALLONLINE_DEFAULT_GPS = "gps.gpsoo.net";
    private static final String PRD_ALLONLINE_DEFAULT_LOG = "applog.gpsoo.net";
    private static final String PRD_ALLONLINE_DEFAULT_CFG = "appcfg.gpsoo.net";
    private static final String PRD_ALLONLINE_DEFAULT_GAPI = "busapi.gpsoo.net";
    private static final String PRD_ALLONLINE_DEFAULT_CARD = "in.gpsoo.net";
    /***设备安装信息上报host***/
    private static final String PRD_ALLONLINE_DEFAULT_UPLOADINFO = "cloudapi.gpsoo.net";

    public static RespDomainAdd sRespDomainAdd;

    private DomainManager() {
    }

    public synchronized static DomainManager getInstance() {
        if (mDomainManager == null) {
            mDomainManager = new DomainManager();
        }
        if (sRespDomainAdd == null) {
            initDomain();
        }
        return mDomainManager;
    }

    public static void initDomain() {
        try {
            // 网络请求返回的缓存于本地的域名列表
            String domainStr = PreferenceUtil.getString(Constant.PREFERENCE_DOMAINS, null);
            if (!TextUtils.isEmpty(domainStr)) {
                sRespDomainAdd = new Gson().fromJson(domainStr, RespDomainAdd.class);
                //时间以当前系统最新时间为准。因为可能缓存的时间已经比较久了
                sRespDomainAdd.timestamp = System.currentTimeMillis() / 1000;
            }

            if (sRespDomainAdd == null) {
                // 不存在， 则使用默认域名
                sRespDomainAdd = new RespDomainAdd();
                sRespDomainAdd.domainWeb = PRD_ALLONLINE_DEFAULT_WEB;
                sRespDomainAdd.domainMain = PRD_ALLONLINE_DEFAULT_DY;
                sRespDomainAdd.domainAd = PRD_ALLONLINE_DEFAULT_AD;
                sRespDomainAdd.domainGps = PRD_ALLONLINE_DEFAULT_GPS;
                sRespDomainAdd.domainLog = PRD_ALLONLINE_DEFAULT_LOG;
                sRespDomainAdd.domainCfg = PRD_ALLONLINE_DEFAULT_CFG;
                sRespDomainAdd.domainGapi = PRD_ALLONLINE_DEFAULT_GAPI;
                sRespDomainAdd.domainCard = PRD_ALLONLINE_DEFAULT_CARD;
                sRespDomainAdd.httpsFlag = false;
                sRespDomainAdd.timestamp = System.currentTimeMillis() / 1000;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String addHeaderHost(String host) {
        if (TEST) {
            return "https://test-" + host + "/";
        }
        return "https://" + host + "/";
    }

    public String getInitGpnsHost() {
        return "http://" + sRespDomainAdd.domainMain;
    }

    public String getCartrackingHost() {
        return addHeaderHost(sRespDomainAdd.domainMain);
    }

    public String getCommunityHost() {
        return addHeaderHost(PRD_COMMUNITY_HOST_DEFAULT);
    }

    public String getCommunityPicup() {
        return addHeaderHost(PRD_COMMUNITY_PICUP_DEFAULT);
    }

    public String getFirstHost() {
        return addHeaderHost(FIRST_HOST);
    }

    public String getGoogleAddressHost() {
        return addHeaderHost(GOOGLE_ADDRESS_HOST);
    }

    public String getWebHost() {
        return addHeaderHost(DomainManager.sRespDomainAdd.domainWeb);
    }

    public String getAdverHost() {
        return addHeaderHost(sRespDomainAdd.domainAd);
    }

    public String getConfigHost() {
        return addHeaderHost(sRespDomainAdd.domainCfg);
    }

    public String getGoomeLogHost() {
        return addHeaderHost(sRespDomainAdd.domainLog);
    }

    public String getUploadInfoHost() {
        return addHeaderHost(PRD_ALLONLINE_DEFAULT_UPLOADINFO);
    }

    public String getCardHost() {
        return addHeaderHost(PRD_ALLONLINE_DEFAULT_CARD);
    }
}
