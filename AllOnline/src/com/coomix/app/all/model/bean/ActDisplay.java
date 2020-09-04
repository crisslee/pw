package com.coomix.app.all.model.bean;

import android.text.TextUtils;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by goome on 2017/6/19.
 */
public class ActDisplay implements Serializable {
    private static final long serialVersionUID = 1210180608663867068L;

    /***
     * 姓名，qq/微信等正常输入的文本内容
     */
    public static final int INPUT_TEXT_NORMAL = 1;

    /***
     * 电话，纯数字内容
     */
    public static final int INPUT_NUMBERS = 2;

    /***
     * 电话，纯数字内容
     */
    public static final int INPUT_CERTIFICATE = 3;

    private int seq;

    private String param;

    private String name;

    private String placeholder;

    private int type;

    private ArrayList<ImageInfo> imageList;

    /**
     * 错误消息，用于显示
     */
    private String errMsg;

    private Object value; //可能是数字也可能是数组

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    /**
     * 用户的输入
     */
    private String input;

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPlaceHolder() {
        return placeholder;
    }

    public void setPlaceHolder(String placeHolder) {
        this.placeholder = placeHolder;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object object) {
        this.value = object;
    }

    public ArrayList<ImageInfo> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<ImageInfo> imageList) {
        this.imageList = imageList;
    }

    public void addImageList(ArrayList<ImageInfo> imageList) {
        if (this.imageList == null) {
            this.imageList = imageList;
        } else {
            this.imageList.addAll(imageList);
        }
    }

    /**
     * 已经上传的网络图片["url1","url2","url3"...]
     */
    public ArrayList<ImageInfo> getImagesListValue() {
        ArrayList<ImageInfo> list = new ArrayList<ImageInfo>();

        if (getValue() == null || !(getValue() instanceof ArrayList)) {
            return list;
        }
        String content = "";
        try {
            ArrayList<String> listData = (ArrayList<String>) getValue();
            if (listData != null) {
                for (int i = 0; i < listData.size(); i++) {
                    String url = listData.get(i);
                    if (!TextUtils.isEmpty(url)) {
                        ImageInfo imageInfo = new ImageInfo();
                        imageInfo.setDomain("");
                        imageInfo.setImg_path(url);
                        imageInfo.setNet(true);
                        list.add(imageInfo);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
