package com.coomix.app.all.model.response;

import java.io.Serializable;

/**
 * 图片对像
 * 
 * @author zengxiaofeng
 * 
 */
public class RespCommunityImageInfo extends RespBase implements Serializable
{

	private CommunityImageInfo data;

	public CommunityImageInfo getData()
	{
		return data;
	}

	public void setData(CommunityImageInfo data)
	{
		this.data = data;
	}
}
