package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 活动详情中提交信息
 *
 * @author shishenglong
 * @since 2016年8月17日 上午10:58:39
 */
public class ActCommitItemOption implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name; //名称
    private ArrayList<ActCommitItemOption> next_level; //一般用于地区选择

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ActCommitItemOption> getNext_level() {
        return next_level;
    }

    public void setNext_level(ArrayList<ActCommitItemOption> next_level) {
        this.next_level = next_level;
    }
}
