package com.goomeim.utils;

import android.content.Context;
import android.util.Log;


import com.coomix.app.all.AllOnlineApp;

import net.goome.im.chat.GMMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * File Description:
 * 管理聊天室已读语音
 *
 * @author felixqiu
 * @since 2017/11/9.
 */

public class VoiceMessageUtils
{
    private static final String FILENAME = "listenedMessages.dat";
    private static final String SORTEDFILE = "sortedListened.dat";
    // conversationID, <svrMsgId, svrTime>
    static Map<String, Map<Long, Long>> listenedMsgs = new HashMap<>();
    // conversationId, <svrTime, svrMsgId> 有序
    static Map<String, TreeMap<Long, Long>> sortedListened = new HashMap<>();
    private static final int MAX_COUNT_PER_CONVERSATION = 50;

    public static boolean isListened(GMMessage message)
    {
        if (message == null)
        {
            return false;
        }
        String conversationId = message.getConversationId();
        if (listenedMsgs.containsKey(conversationId))
        {
            if (listenedMsgs.get(conversationId).containsKey(message.getSvrMsgId()))
            {
                return true;
            }
            return false;
        }
        return false;
    }

    public static void saveListenedMessage(GMMessage message)
    {
        if (message == null || isListened(message))
        {
            return;
        }
        String conversationId = message.getConversationId();
        if (listenedMsgs.containsKey(conversationId))
        {
            Map<Long, Long> map = listenedMsgs.get(conversationId);
            Map<Long, Long> sortedMap = sortedListened.get(conversationId);
            if (map.containsKey(message.getSvrMsgId()))
            {
                return;
            }
            if (map.values().size() > MAX_COUNT_PER_CONVERSATION)
            {
                List<Long> list = new ArrayList<>();
                list.addAll(sortedMap.values());
                // svrMsgId
                Long key = list.get(0);
                // svrTime
                Long sortedKey = map.get(key);
                map.remove(key);
                sortedMap.remove(sortedKey);
            }
            map.put(message.getSvrMsgId(), message.getTimestamp() / 1000);
            sortedMap.put(message.getTimestamp() / 1000, message.getSvrMsgId());
        }
        else
        {
            Map<Long, Long> map = new HashMap<>();
            map.put(message.getSvrMsgId(), message.getTimestamp() / 1000);
            listenedMsgs.put(conversationId, map);

            TreeMap<Long, Long> sortedMap = new TreeMap<>();
            sortedMap.put(message.getTimestamp() / 1000, message.getSvrMsgId());
            sortedListened.put(conversationId, sortedMap);
        }
    }

    public static void initRecords()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                listenedMsgs = (Map<String, Map<Long, Long>>) readDataObject(FILENAME);
                if (listenedMsgs == null)
                {
                    listenedMsgs = new HashMap<String, Map<Long, Long>>();
                }
                sortedListened = (Map<String, TreeMap<Long, Long>>) readDataObject(SORTEDFILE);
                if (sortedListened == null)
                {
                    sortedListened = new HashMap<String, TreeMap<Long, Long>>();
                }
            }
        }).start();
    }

    public static void saveRecordsToFile()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                saveDataObject(FILENAME, listenedMsgs);
                saveDataObject(SORTEDFILE, sortedListened);
            }
        }).start();
    }

    public static void saveDataObject(String fileName, Object object)
    {
        try
        {
            Context appContext = AllOnlineApp.mApp;
            File file = appContext.getFileStreamPath(fileName);
            if (file.exists())
            {
                file.delete();
            }
            FileOutputStream fos = appContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Object readDataObject(String fileName)
    {
        Object object = null;
        try
        {
            Context appContext = AllOnlineApp.mApp;
            File file = appContext.getFileStreamPath(fileName);
            if (file.exists())
            {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                object = ois.readObject();
                ois.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return object;
    }

    public static void printMap()
    {
        if (listenedMsgs != null)
        {
            Log.i("felix", "listenedMsgs :");
            for (Map.Entry<String, Map<Long, Long>> entry : listenedMsgs.entrySet())
            {
                Log.i("felix", "conversationid=" + entry.getKey());
                Map<Long, Long> map = entry.getValue();
                if (map != null)
                {
                    for (Map.Entry<Long, Long> entry1 : map.entrySet())
                    {
                        Log.i("felix", "svrmsgid=" + entry1.getKey() + ", svrtime=" + entry1.getValue());
                    }
                }
            }
        }
        if (sortedListened != null)
        {
            Log.i("felix", "sortedListened :");
            for (Map.Entry<String, TreeMap<Long, Long>> entry : sortedListened.entrySet())
            {
                Log.i("felix", "conversationid=" + entry.getKey());
                Map<Long, Long> map = entry.getValue();
                if (map != null)
                {
                    for (Map.Entry<Long, Long> entry1 : map.entrySet())
                    {
                        Log.i("felix", "svrmsgid=" + entry1.getKey() + ", svrtime=" + entry1.getValue());
                    }
                }
            }
        }
    }
}
