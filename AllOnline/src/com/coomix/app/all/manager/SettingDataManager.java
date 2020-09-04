package com.coomix.app.all.manager;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.ui.history.AMapHistoryActivity;
import com.coomix.app.all.ui.history.BMapHistoryActivity;
import com.coomix.app.all.ui.history.TMapHistoryActivity;
import com.coomix.app.all.ui.main.AMainActivity;
import com.coomix.app.all.ui.main.BMainActivity;
import com.coomix.app.all.ui.main.TMainActivity;
import com.coomix.app.all.ui.main.GMainActivity;
import com.coomix.app.all.ui.panorama.BPanoramaActivity;
import com.coomix.app.all.ui.panorama.TPanoramaActivity;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.google.gson.Gson;
import java.util.ArrayList;
import org.json.JSONArray;

/**
 * Created by ssl on 2017/10/16.
 */
public class SettingDataManager {
    private static SettingDataManager instance = null;
    private static final String KEY_SHP_MAP_CHOOSED = "map_choosed_allonline";
    //围栏半径
    public static final String KEY_lIST_PREFERENCE_FENCE_RADIUS = "key_list_preference_fence_radius";
    // 退出标志
    public static final String KEY_lIST_PREFERENCE_LOGOUT = "key_list_preference_logout";
    //搜索关键字的历史。总计十条
    public static final String KEY_SEARCH_HISTORY = "key_search_history";

    private Context mContext = null;
    public static String strMapType = Constant.MAP_AMAP;
    public static String language = "zh-CN";

    private SettingDataManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public synchronized static SettingDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingDataManager(context);
        }
        return instance;
    }

    public void init() {
        PreferenceUtil.init(mContext);

        //系统语言
        language = CommonUtil.initLang();

        initMapNew();
    }

    //由于语言设置会影响地图，所以语言设置必须在前
    private void initMapNew() {
        int iMapType = getMapTypeInt();
        if (iMapType == Constant.MAP_TYPE_BAIDU) {
            //baidu
            strMapType = Constant.MAP_BAIDU;
        } else if (iMapType == Constant.MAP_TYPE_AMAP) {
            // 高德地图
            strMapType = Constant.MAP_AMAP;
        } else if (iMapType == Constant.MAP_TYPE_TENCENT) {
            // 腾讯地图
            strMapType = Constant.MAP_TENCENT;
        } else {
            // 默认使用baidu地图
            strMapType = Constant.MAP_BAIDU;
            iMapType = Constant.MAP_TYPE_BAIDU;
        }
        setMapTypeInt(iMapType);
    }

    public int getMapTypeInt() {
        return PreferenceUtil.getInt(KEY_SHP_MAP_CHOOSED, Constant.MAP_TYPE_BAIDU);
    }

    public void setMapTypeInt(int type) {
        switch (type) {
            case Constant.MAP_TYPE_BAIDU:
                strMapType = Constant.MAP_BAIDU;
                break;

            case Constant.MAP_TYPE_AMAP:
                strMapType = Constant.MAP_AMAP;
                break;

            case Constant.MAP_TYPE_TENCENT:
                strMapType = Constant.MAP_TENCENT;
                break;
            case Constant.MAP_TYPE_GOOGLE:
                strMapType = Constant.MAP_GOOGLE;
                break;

            default:
                strMapType = Constant.MAP_BAIDU;
                break;
        }
        PreferenceUtil.commitInt(KEY_SHP_MAP_CHOOSED, type);
    }

    public String getMapNameByType() {
        String name = "百度";
        switch (getMapTypeInt()) {
            case Constant.MAP_TYPE_BAIDU:
                name = "百度";
                break;

            case Constant.MAP_TYPE_AMAP:
                name = "高德";
                break;

            case Constant.MAP_TYPE_TENCENT:
                name = "腾讯";
                break;

            default:
                name = "高德";
                break;
        }
        return name;
    }

    /**
     * 0:英语
     * 1：中文简体
     * 2：中文繁体
     */
    public int getLanguageInt() {
        if (language.equals("zh-HK") || language.equals("zh-TW")) {
            return 2;
        } else if (language.equals("zh-CN")) {
            return 1;
        }
        return 0;
    }

    public int getMapTypeNormalDrawableId() {
        int iResId = 0;
        switch (getLanguageInt()) {
            case 1:
                iResId = R.drawable.nav_more_map_normal_rcn;
                break;

            case 2:
                iResId = R.drawable.nav_more_map_normal_rhk;
                break;

            default:
                iResId = R.drawable.nav_more_map_normal;
                break;
        }
        return iResId;
    }

    public int getMapTypeSatDrawableId() {
        int iResId = 0;
        switch (getLanguageInt()) {
            case 1:
                iResId = R.drawable.nav_more_map_press_rcn;
                break;

            case 2:
                iResId = R.drawable.nav_more_map_press_rhk;
                break;

            default:
                iResId = R.drawable.nav_more_map_press;
                break;
        }
        return iResId;
    }

    public int getListDrawableId() {
        int iResId = 0;
        switch (getLanguageInt()) {
            case 1:
                iResId = R.drawable.list_icon_cn;
                break;

            case 2:
                iResId = R.drawable.list_icon_hk;
                break;

            default:
                iResId = R.drawable.list_icon;
                break;
        }
        return iResId;
    }

    public int getStreetDrawableId() {
        int iResId = 0;
        switch (getLanguageInt()) {
            case 1:
                iResId = R.drawable.nav_panorama_rcn;
                break;

            case 2:
                iResId = R.drawable.nav_panorama_rcn;
                break;

            default:
                iResId = R.drawable.nav_panorama;
                break;
        }
        return iResId;
    }

    public void goToMainByMap(Context context, String imei) {
        Intent intent = null;
        switch (getMapTypeInt()) {
            case Constant.MAP_TYPE_AMAP:
                intent = new Intent(context, AMainActivity.class);
                break;

            case Constant.MAP_TYPE_TENCENT:
                intent = new Intent(context, TMainActivity.class);
                break;

            case Constant.MAP_TYPE_BAIDU:
                intent = new Intent(context, BMainActivity.class);
                break;
            case Constant.MAP_TYPE_GOOGLE:
                intent = new Intent(context, GMainActivity.class);
                break;
            default:
                intent = new Intent(context, BMainActivity.class);
                break;
        }
        if (!TextUtils.isEmpty(imei)) {
            intent.putExtra(AMainActivity.IMEI, imei);
        }
        context.startActivity(intent);
    }

    public Intent getHistoryActIntent(Context context) {
        Intent i = null;
        switch (getMapTypeInt()) {
            case Constant.MAP_TYPE_AMAP:
                i = new Intent(context, AMapHistoryActivity.class);
                break;

            case Constant.MAP_TYPE_TENCENT:
                i = new Intent(context, TMapHistoryActivity.class);
                break;

            case Constant.MAP_TYPE_BAIDU:
            default:
                i = new Intent(context, BMapHistoryActivity.class);
                break;
        }
        return i;
    }

    public int getFenceRange() {
        return PreferenceUtil.getInt(KEY_lIST_PREFERENCE_FENCE_RADIUS, 200);
    }

    public void setFenceRange(int radius) {
        PreferenceUtil.commitInt(KEY_lIST_PREFERENCE_FENCE_RADIUS, radius);
    }

    public void goToPanoramaActivity(Context context, DeviceInfo deviceInfo) {
        Intent intent = null;
        switch (getMapTypeInt()) {
            case Constant.MAP_TYPE_TENCENT:
            case Constant.MAP_TYPE_AMAP:
                //高德地图没有街景，因和腾讯地图坐标系相同，故暂时采用腾讯地图的街景
                intent = new Intent(context, TPanoramaActivity.class);
                break;

            case Constant.MAP_TYPE_BAIDU:
                intent = new Intent(context, BPanoramaActivity.class);
                break;

            default:
                return;
        }
        intent.putExtra(Constant.KEY_DEVICE, deviceInfo);
        context.startActivity(intent);
    }

    public void saveSearchHistory(ArrayList<String> historys) {
        if (historys == null) {
            return;
        }
        String data = new Gson().toJson(historys);
        PreferenceUtil.commitString(KEY_SEARCH_HISTORY, data);
        //PreferenceUtil.commitSet(KEY_SEARCH_HISTORY, set);
    }

    public ArrayList<String> getSearchHistory() {
        String data = PreferenceUtil.getString(KEY_SEARCH_HISTORY, null);
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        ArrayList<String> list = new ArrayList<String>();

        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.get(i).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
