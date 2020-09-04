package com.coomix.app.all.model.bean;

import android.util.SparseArray;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.model.inner.GeoPoint;

import java.util.ArrayList;

public class OfflineCityItem {
    public final static int COUNTRY = 0;
    public final static int PROVINCE = 1;
    public final static int CITY = 2;
    public final static int HEADER = 3;

    public static final int DOWNLOADING = 1;
    public static final int eOLDSIOError = 7;
    public static final int eOLDSMd5Error = 5;
    public static final int eOLDSMissData = 9;
    public static final int eOLDSNetError = 6;
    public static final int eOLDSWifiError = 8;
    public static final int FINISHED = 4;
    public static final int SUSPENDED = 3;
    public static final int UNDEFINED = 0;
    public static final int WAITING = 2;

    public String label;

    public String cityName;

    public int groupId;

    public int cityId;

    public int cityType;

    public int size;

    public ArrayList<OfflineCityItem> childCitites;

    public GeoPoint geoPt;
    public int level;
    public int ratio;
    public int serversize;
    public int updateSize;;
    public int status;
    public boolean update;

    @Override
    public String toString() {
        return "OfflineMapItem [label=" + label + ", cityName=" + cityName + ", groupId=" + groupId + ", cityId=" + cityId + ", cityType=" + cityType + ", size=" + size + ", geoPt=" + geoPt + ", level=" + level + ", ratio=" + ratio
                + ", serversize=" + serversize + ", updateSize=" + updateSize + ", status=" + status + ", update=" + update + "]";
    }

    public OfflineCityItem() {

    }

    public OfflineCityItem(MKOLSearchRecord r) {
        this.label = "";
        this.cityName = r.cityName;
        this.cityId = r.cityID;
        this.cityType = r.cityType;
        this.size = r.size;
        // if (r.childCities != null && r.childCities.size() > 0) {
        // childCitites = new ArrayList<OfflineMapItem>();
        // for (MKOLSearchRecord rc : r.childCities) {
        // childCitites.add(new OfflineMapItem(rc));
        // }
        // }
    }

    public OfflineCityItem(MKOLSearchRecord r, SparseArray<OfflineCityItem> sparseArrayOfflineItem) {
        this.label = "";
        this.cityName = r.cityName;
        this.cityId = r.cityID;
        this.cityType = r.cityType;
        this.size = r.size;
        if (r.childCities != null && r.childCities.size() > 0) {
            childCitites = new ArrayList<OfflineCityItem>();
            for (MKOLSearchRecord rc : r.childCities) {
                OfflineCityItem childItem = null;
                if (sparseArrayOfflineItem.indexOfKey(rc.cityID) < 0) {
                    childItem = new OfflineCityItem(rc);
                    sparseArrayOfflineItem.put(childItem.cityId, childItem);
                } else {
                    childItem = sparseArrayOfflineItem.get(rc.cityID);
                }
                childCitites.add(childItem);
            }
        }
    }

    public void clearUpdateData() {
        geoPt = null;
        level = 0;
        ratio = 0;
        serversize = 0;
        updateSize = 0;
        status = 0;
        update = false;
    }

    public void updateStatus(MKOLUpdateElement u) {
        geoPt = new GeoPoint(u.geoPt.longitude, u.geoPt.latitude);
        level = u.level;
        ratio = u.ratio;
        serversize = u.serversize;
        updateSize = u.size;
        status = u.status;
        update = u.update;
    }

}
