package com.coomix.app.all.model.bean;

import com.coomix.app.all.model.response.CommunityUser;
import java.io.Serializable;
import java.util.ArrayList;

public class CommunityUsers implements Serializable {
    private static final long serialVersionUID = 5561847147533962777L;
    private CommunityReadpos readpos;
    private ArrayList<CommunityUser> users;
    private ArrayList<CommunityUser> user;
    private double fans_read_pos;
    private int new_fans_count;

    public void setReadpos(CommunityReadpos readpos) {
        this.readpos = readpos;
    }

    public CommunityReadpos getReadpos() {
        return readpos;
    }

    public void setUsers(ArrayList<CommunityUser> users) {
        this.users = users;
    }

    public ArrayList<CommunityUser> getUsers() {
        return users == null ? user : users;
    }

    public void setUser(ArrayList<CommunityUser> user) {
        this.user = user;
    }

    public ArrayList<CommunityUser> getUser() {
        return user == null ? users : user;
    }

    public void setFans_read_pos(double fans_read_pos) {
        this.fans_read_pos = fans_read_pos;
    }

    public double getFans_read_pos() {
        return fans_read_pos;
    }

    public void setNew_fans_count(int new_fans_count) {
        this.new_fans_count = new_fans_count;
    }

    public int getNew_fans_count() {
        return new_fans_count;
    }
}
