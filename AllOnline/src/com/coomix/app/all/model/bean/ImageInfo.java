package com.coomix.app.all.model.bean;

import com.coomix.app.all.util.CommunityPictureUtil;
import java.io.Serializable;

/**
 * 图片对像
 *
 * @author zengxiaofeng
 */
public class ImageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    // @SerializedName("img_path")
    public String source_image; // 源图
    public boolean isAddButton = false; // 是否是添加按钮
    private int width = CommunityPictureUtil.MAX_WIDTH / 2;
    private int height = CommunityPictureUtil.MAX_WIDTH / 2;
    private boolean isNet = false; // 是否网络图片（发帖时，图片上传完后source_image会替换为网络图片地址）
    private String domain; // 网络图片域名
    private String img_path; // 网络图片

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

    public String getSource_image() {
        return source_image;
    }

    public void setSource_image(String source_image) {
        this.source_image = source_image;
    }

    public boolean isAddButton() {
        return isAddButton;
    }

    public void setAddButton(boolean isAddButton) {
        this.isAddButton = isAddButton;
    }

    public void setNet(boolean isNet) {
        this.isNet = isNet;
    }

    public boolean isNet() {
        return isNet;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public String getImg_path() {
        return img_path;
    }
}
