package com.coomix.app.all.model.bean;

import com.coomix.app.all.model.response.CommunityUser;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.io.Serializable;

@DatabaseTable(tableName = "HX_USER")
public class HxDBUser implements Serializable {

    private static final long serialVersionUID = -7129697646441348609L;
    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField
    private String userId;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private CommunityUser user;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public CommunityUser getUser() {
        return user;
    }

    public void setUser(CommunityUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "HxUserDBDao [user_id=" + userId + ", user=" + user + "]";
    }
}
