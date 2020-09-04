package com.coomix.app.all.model.bean;

public class OfflineMapItem {

    private int cityId;
    private String cityName;
    private int downloadState;
    private int size;
    private int ratio;
    private boolean hasUpdate;

    public OfflineMapItem(int cityId, String cityName, int downloadState,
        boolean hasUpdate, int size, int ratio) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.downloadState = downloadState;
        this.hasUpdate = hasUpdate;
        this.size = size;
        this.ratio = ratio;
    }

    public OfflineMapItem() {
        //this.cityId = -1;
        //this.cityName = "";
        //this.downloadState = NORMAL;
        //this.hasUpdate = false;
    }

    @Override
    public String toString() {
        return "OfflineMapItem [cityId=" + cityId + ", cityName=" + cityName + ", downloadState="
            + downloadState + ", size=" + size + ", ratio=" + ratio + ", hasUpdate="
            + hasUpdate + "]";
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(int downloadState) {
        this.downloadState = downloadState;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public boolean isHasUpdate() {
        return hasUpdate;
    }

    public void setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }
}
