package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * Share.java
 *
 * @author shishenglong
 * @since 2016年8月22日 下午7:20:48
 */
public class Share implements Serializable {
    private static final long serialVersionUID = 8812230002932406529L;

    private String url;
    private String title;
    private String description;
    private String pic;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}
