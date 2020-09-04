package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author shishenglong
 * @since 2016年8月17日 上午11:14:04
 */
public class CommunityActContents implements Serializable {

    private static final long serialVersionUID = 354561564986888L;

    private ArrayList<CommunityActContent> list;

    public ArrayList<CommunityActContent> getList() {
        return list == null ? (list = new ArrayList<CommunityActContent>()) : list;
    }

    public void setList(ArrayList<CommunityActContent> list) {
        this.list = list;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "CommunityActContents{" +
            "list=" + list +
            '}';
    }
}
