package com.coomix.app.all.model.bean;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * 广告对象
 *
 * @author goome
 */
public class ActCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    //for the purpose of display
    private int position = 0;

    @Expose
    private int id; //123,
    @Expose
    private String title; // "亲子",
    @Expose
    private String pic;  // "1.jpeg" 图片
    @Expose
    private String selected_pic;  // "1.jpeg" 图片

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getSelected_pic() {
        return selected_pic;
    }

    public void setSelected_pic(String selected_pic) {
        this.selected_pic = selected_pic;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
