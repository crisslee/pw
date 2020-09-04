package com.coomix.app.all.markColection.baidu;

import com.baidu.mapapi.model.inner.GeoPoint;

public class MBound {

    private int rightTopLat;
    private int rightTopLng;
    private int leftBottomLat;
    private int leftBottomLng;

    private GeoPoint rightTop;
    private GeoPoint leftBottom;

    public MBound(GeoPoint rightTop, GeoPoint leftBottom) {
        super();
        this.rightTop = rightTop;
        this.leftBottom = leftBottom;
        rightTopLat = (int) (rightTop.getLatitudeE6() * 1E6);
        rightTopLng = (int) (rightTop.getLongitudeE6() * 1E6);
        leftBottomLat = (int) (leftBottom.getLatitudeE6() * 1E6);
        leftBottomLng = (int) (leftBottom.getLongitudeE6() * 1E6);

    }

    public MBound(int rightTopLat, int rightTopLng, int leftBottomLat, int leftBottomLng) {
        this.rightTopLat = rightTopLat;
        this.rightTopLng = rightTopLng;
        this.leftBottomLat = leftBottomLat;
        this.leftBottomLng = leftBottomLng;
        rightTop = new GeoPoint(rightTopLat, rightTopLng);
        leftBottom = new GeoPoint(leftBottomLat, leftBottomLng);

    }

    public int getRightTopLat() {
        return rightTopLat;
    }

    public void setRightTopLat(int rightTopLat) {
        this.rightTopLat = rightTopLat;
    }

    public int getRightTopLng() {
        return rightTopLng;
    }

    public void setRightTopLng(int rightTopLng) {
        this.rightTopLng = rightTopLng;
    }

    public int getLeftBottomLat() {
        return leftBottomLat;
    }

    public void setLeftBottomLat(int leftBottomLat) {
        this.leftBottomLat = leftBottomLat;
    }

    public int getLeftBottomLng() {
        return leftBottomLng;
    }

    public void setLeftBottomLng(int leftBottomLng) {
        this.leftBottomLng = leftBottomLng;
    }

    public GeoPoint getRightTop() {
        return rightTop;
    }

    public void setRightTop(GeoPoint rightTop) {
        this.rightTop = rightTop;
    }

    public GeoPoint getLeftBottom() {
        return leftBottom;
    }

    public void setLeftBottom(GeoPoint leftBottom) {
        this.leftBottom = leftBottom;
    }

}
