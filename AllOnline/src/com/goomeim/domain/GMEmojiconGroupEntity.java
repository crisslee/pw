package com.goomeim.domain;

import java.util.List;


/**
 * 一组表情所对应的实体类
 *
 */
public class GMEmojiconGroupEntity
{
	/**
	 * 表情数据
	 */
	private List<GMEmojicon> emojiconList;
	/**
	 * 图片
	 */
	private int icon;
	/**
	 * 组名
	 */
	private String name;
	/**
	 * 表情类型
	 */
	private GMEmojicon.Type type;

	public GMEmojiconGroupEntity()
	{
	}

	public GMEmojiconGroupEntity(int icon, List<GMEmojicon> emojiconList)
	{
		this.icon = icon;
		this.emojiconList = emojiconList;
		type = GMEmojicon.Type.NORMAL;
	}

	public GMEmojiconGroupEntity(int icon, List<GMEmojicon> emojiconList, GMEmojicon.Type type)
	{
		this.icon = icon;
		this.emojiconList = emojiconList;
		this.type = type;
	}

	public List<GMEmojicon> getEmojiconList()
	{
		return emojiconList;
	}

	public void setEmojiconList(List<GMEmojicon> emojiconList)
	{
		this.emojiconList = emojiconList;
	}

	public int getIcon()
	{
		return icon;
	}

	public void setIcon(int icon)
	{
		this.icon = icon;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public GMEmojicon.Type getType()
	{
		return type;
	}

	public void setType(GMEmojicon.Type type)
	{
		this.type = type;
	}

}
