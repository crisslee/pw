package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class CommunityUpReadpos implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private double pointer;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPointer() {
        return pointer;
    }

    public void setPointer(double pointer) {
        this.pointer = pointer;
    }
}
