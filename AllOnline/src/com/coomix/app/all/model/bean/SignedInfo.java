package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author shishenglong
 */
public class SignedInfo implements Serializable {

    private static final long serialVersionUID = 35486695321332158L;

    private String name;
    private String tel;
    private String qqorwx;
    private ArrayList<CommitExtendItem> extend_items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getQqorwx() {
        return qqorwx == null ? "" : qqorwx;
    }

    public void setQqorwx(String qqorwx) {
        this.qqorwx = qqorwx;
    }

    public ArrayList<CommitExtendItem> getExtend_items() {
        return extend_items;
    }

    public void setExtend_items(ArrayList<CommitExtendItem> extend_items) {
        this.extend_items = extend_items;
    }
}
