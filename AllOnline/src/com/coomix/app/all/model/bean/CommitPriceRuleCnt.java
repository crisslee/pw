package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class CommitPriceRuleCnt implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int cnt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }
}
