package com.coomix.app.all.model.response;

import java.io.Serializable;

public class RespCommunityUser extends RespBase implements Serializable {

  private CommunityUser data;

    public CommunityUser getData()
    {
        return data;
    }

    public void setData(CommunityUser data)
    {
        this.data = data;
    }
}
