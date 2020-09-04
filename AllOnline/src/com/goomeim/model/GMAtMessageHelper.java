package com.goomeim.model;

import android.content.Context;
import android.text.TextUtils;


import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.google.gson.JsonArray;
import com.goomeim.GMAppConstant;
import com.goomeim.utils.GMCommonUtils;

import net.goome.im.chat.GMConstant.ConversationType;
import net.goome.im.chat.GMMessage;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GMAtMessageHelper
{
	/**
	 * At时候存储当前聊天的ID，以及那条At消息的ID（存在At消息撤回）
	 * 格式：聊天ID:消息ID
	 * */
	private Set<String> atMeGroupList = null;
	private static GMAtMessageHelper instance = null;
	private List<AtInfo> listAtInfos = new ArrayList<AtInfo>();

	public synchronized static GMAtMessageHelper get()
	{
		if (instance == null)
		{
			instance = new GMAtMessageHelper();
		}
		return instance;
	}

	private GMAtMessageHelper()
	{
		atMeGroupList = GMPreferenceManager.getInstance().getAtMeGroups();
		if (atMeGroupList == null)
		{
			atMeGroupList = new HashSet<String>();
		}
	}

    /**
     * add user you want to @
     */
    public void addAtUser(long userId, String name)
    {
        synchronized (listAtInfos)
        {
            if (listAtInfos == null)
            {
                listAtInfos = new ArrayList<AtInfo>();
            }
            AtInfo atInfo = new AtInfo();
            atInfo.id = userId;
            atInfo.name = name;
            listAtInfos.add(atInfo);
        }
    }

    /**
     * check if be mentioned(@) in the content
     */
    public boolean containsAtUsername(String content)
    {
        if (TextUtils.isEmpty(content))
        {
            return false;
        }
        synchronized (listAtInfos)
        {
            for (AtInfo atInfo : listAtInfos)
            {
                if (content.contains("@" + atInfo.name))
                {
                    return true;
                }
            }
        }
        return false;
    }

	/**
	 * check if be mentioned(@) in the content
	 *
	 * @param content
	 * @return
	 */
	public boolean containsAtUserId(String content)
	{
		if (TextUtils.isEmpty(content))
		{
			return false;
		}
		synchronized (listAtInfos)
		{
			for(AtInfo atInfo : listAtInfos)
			{
				if(content.contains(String.valueOf(atInfo.id)))
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsAtAll(String content, Context context)
	{
		String atAll = context.getString(R.string.group_at_all);
		if (content.contains(atAll) || content.contains("@All") || content.contains("@ALL")  || content.contains("@all"))
		{
			return true;
		}
		return false;
	}

	/**
	 * get the users names be mentioned(@)
	 * 
	 * @param content
	 * @return
	 */
	public List<String> getAtMessageUserNames(String content)
	{
		if (TextUtils.isEmpty(content))
		{
			return null;
		}
		synchronized (listAtInfos)
		{
			List<String> list = null;
			for (AtInfo atInfo : listAtInfos)
			{
				if (content.contains(atInfo.name))
				{
					if (list == null)
					{
						list = new ArrayList<String>();
					}
					list.add(atInfo.name);
				}
			}
			return list;
		}
	}

    /**
     * get the users ids be mentioned(@)
     */
    public List<Long> getAtMessageUserIds(String content)
    {
        if (TextUtils.isEmpty(content))
        {
            return null;
        }
        synchronized (listAtInfos)
        {
            List<Long> list = null;
            for (AtInfo atInfo : listAtInfos)
            {
                if (content.contains("@" + atInfo.name + " "))
                {
                    if (list == null)
                    {
                        list = new ArrayList<Long>();
                    }
                    list.add(atInfo.id);
                }
            }
            return list;
        }
    }

    /**
     * parse the message, get and save group id if I was mentioned(@)
     *
     * @param messages
     */
    public void parseMessages(List<GMMessage> messages)
    {
        int size = atMeGroupList.size();
        GMMessage[] msgs = messages.toArray(new GMMessage[]{});
        for (GMMessage msg : msgs)
        {
            if (msg.getChatType() == ConversationType.CHATROOM || msg.getChatType() == ConversationType.CLASSICROOM)
            {
                long groupId = msg.getTo();
                try
                {
                    JSONArray atArray = GMCommonUtils.getMessageExtArray(msg, GMAppConstant.GOOME_AT_LIST);
                    for (int i = 0; i < atArray.length(); i++)
                    {
                        long uid = atArray.getLong(i);
                        if (uid == AllOnlineApp.sToken.community_id)
                        {
                            atMeGroupList.add(groupId + ":" + msg.getMsgId());
                        }
                    }
                }
                catch (Exception e)
                {
                    //e.printStackTrace();
                }

                try
                {
                    // Determine whether is @ all message
                    String atAllStr = GMCommonUtils.getMessageExtString(msg, GMAppConstant.GOOME_AT_ALL, "");
                    if (atAllStr != null && atAllStr.toLowerCase().equals("all"))
                    {
                        atMeGroupList.add(groupId + ":" + msg.getMsgId());
                    }
                }
                catch (Exception e)
                {
                    //e.printStackTrace();
                }

                if (atMeGroupList.size() != size)
                {
                    GMPreferenceManager.getInstance().setAtMeGroups(atMeGroupList);
                }
            }
        }
    }

    public boolean isAtAllMessage(GMMessage msg)
    {
        if (msg == null)
        {
            return false;
        }
        try
        {
            // Determine whether is @ all message
            String usernameStr = GMCommonUtils.getMessageExtString(msg, GMAppConstant.GOOME_AT_ALL, "");
            if (usernameStr != null && usernameStr.toLowerCase().equals("all"))
            {
                return true;
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        return false;
    }

    public boolean isAtMeMessage(GMMessage msg)
    {
        if (msg == null)
        {
            return false;
        }
        try
        {
            // Determine whether is @ me message
            JSONArray jsonArray = GMCommonUtils.getMessageExtArray(msg, GMAppConstant.GOOME_AT_LIST);
            if (jsonArray != null)
            {
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    if (jsonArray.get(i) != null && jsonArray.get(i).toString().equals(String.valueOf(AllOnlineApp.sToken.community_id)))
                    {
                        return true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        return false;
    }

	/**
	 * get groups which I was mentioned
	 * 
	 * @return
	 */
	public Set<String> getAtMeGroups()
	{
		return atMeGroupList;
	}

    /**
     * remove group from the list
     */
    public void removeAtMeGroup(String groupId)
    {
        if (atMeGroupList == null)
        {
            return;
        }
        Iterator<String> it = atMeGroupList.iterator();
        while (it.hasNext())
        {
            String str = it.next();
            if (str != null)
            {
                if (str.contains(":") && str.split(":")[0].equals(groupId))
                {
                    it.remove();
                }
            }
        }
        GMPreferenceManager.getInstance().setAtMeGroups(atMeGroupList);
    }

    public void removeAtMeGroup(String groupId, String msgId)
    {
        if (atMeGroupList != null && atMeGroupList.contains(groupId + ":" + msgId))
        {
            atMeGroupList.remove(groupId + ":" + msgId);
            GMPreferenceManager.getInstance().setAtMeGroups(atMeGroupList);
        }
    }

	/**
	 * check if the input groupId in atMeGroupList
	 * 
	 * @param groupId
	 * @return
	 */
	public boolean hasAtMeMsg(String groupId)
	{
		//return atMeGroupList.contains(groupId);
		if(atMeGroupList == null)
		{
			return false;
		}
		Iterator<String> it = atMeGroupList.iterator();
		while (it.hasNext())
		{
			String str = it.next();
			if(str != null)
			{
				if(str.contains(":") && str.split(":")[0].equals(groupId))
				{
					return true;
				}
			}
		}
		return false;
	}

    public JsonArray atListToJsonArray(List<Long> atList)
    {
        JsonArray jArray = new JsonArray();
        int size = atList.size();
        for (int i = 0; i < size; i++)
        {
            long uid = atList.get(i);
            jArray.add(uid);
        }
        return jArray;
    }

	public void cleanToAtUserList()
	{
		synchronized (listAtInfos)
		{
			listAtInfos.clear();
		}
	}

    public boolean isAtUserName(String name)
    {
        if (TextUtils.isEmpty(name))
        {
            return false;
        }
        for (AtInfo atInfo : listAtInfos)
        {
            if (name.equals(atInfo.name))
            {
                return true;
            }
        }

        return false;
    }

    public void removeAtUserInfo(String userName)
    {
        synchronized (listAtInfos)
        {
            for (AtInfo atInfo : listAtInfos)
            {
                if (userName.equals(atInfo.name))
                {
                    listAtInfos.remove(atInfo);
                    break;
                }
            }
        }
    }

    class AtInfo
    {
        long id;
        String name;
    }
}
