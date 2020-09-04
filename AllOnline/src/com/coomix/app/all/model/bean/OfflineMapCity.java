package com.coomix.app.all.model.bean;

import android.provider.BaseColumns;

import java.io.Serializable;

public class OfflineMapCity implements Serializable {
    
    public static final int UNDOWNLOADED = 0;
    public static final int DOWNLOADED = 1;
    public static final int DOWNLOADING = 2;
    
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public int getCityID() {
        return cityID;
    }

    public void setCityID(int cityID) {
        this.cityID = cityID;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    
    public OfflineMapCity(){}
    
    public OfflineMapCity(String cityName, int cityID,int ratio,int status, long size){
        setCityID(cityID);
        setCityName(cityName);
        setRatio(ratio);
        setStatus(status);
        setSize(size);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    private int status = UNDOWNLOADED;//0δ����,1������,2��������
    private int ratio;
    private int cityID;
    private String cityName;
    private long size;

    public static final class OfflineMapCityColumns implements BaseColumns {
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "_ratio ASC";

        public static final String STATUS ="_status";
        public static final String RATIO ="_ratio";
        public static final String CITY_ID = "_city_id";
        public static final String CITY_SIZE = "_city_size";
        public static final String CITY_NAME = "_city_name";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass())
            return false;
        if (this.cityID== ((OfflineMapCity) o).cityID) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        try {
            return cityID;
        } catch (NumberFormatException e) {
            return super.hashCode();
        }
    }
}
