package com.coomix.app.all.model.response;

import java.io.Serializable;

public class Picture implements Serializable {

    private static final long serialVersionUID = 2323618279989257632L;
    private String picture;
    private int width;
    private int height;
    private String thumbnailUrl;

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public String toString() {
        return "Picture [picture=" + picture + ", width=" + width + ", height=" + height + ", thumbnailUrl="
            + thumbnailUrl + "]";
    }
}
