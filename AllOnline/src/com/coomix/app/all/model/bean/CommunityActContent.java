package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * @author shishenglong
 * @since 2016年8月17日 上午11:14:04
 */
public class CommunityActContent implements Serializable {

    private static final long serialVersionUID = 354565681216888L;

    private int type;
    private String value;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "CommunityActContent{" +
            "type=" + type +
            ", value='" + value + '\'' +
            '}';
    }
}
