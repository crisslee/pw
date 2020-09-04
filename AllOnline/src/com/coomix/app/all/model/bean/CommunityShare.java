package com.coomix.app.all.model.bean;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

/**
 * @author shishenglong
 * @since 2016年8月22日 下午7:20:48
 */
public class CommunityShare implements Serializable {

    private static final long serialVersionUID = 3545646281216888L;
    @Expose
    private String url;
    @Expose
    private String title;
    @Expose
    private String description;
    @Expose
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

    @Override
    public String toString() {
        return "CommunityShare{" +
            "url='" + url + '\'' +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", pic='" + pic + '\'' +
            '}';
    }
}
