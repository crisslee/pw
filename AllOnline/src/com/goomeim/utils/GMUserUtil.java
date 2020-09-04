package com.goomeim.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.GlideApp;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.performReport.DBHelper;
import com.coomix.app.all.service.AllOnlineApiClient;
import com.coomix.app.all.model.bean.HxDBUser;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.NetworkUtil;
import com.coomix.app.all.model.response.CommunityUser;
import com.coomix.app.all.util.CommunityUtil;
import com.goomeim.GMAppConstant;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.goome.im.chat.GMClient;
import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMConstant.MsgDirection;
import net.goome.im.chat.GMCursorResult;
import net.goome.im.chat.GMGroup;
import net.goome.im.chat.GMMessage;

public class GMUserUtil
{
    private final String TAG = GMUserUtil.class.getSimpleName();

    public static final String GMUSER_LOGOUT_ACTION = "com.coomix.app.bus.GMUSER_LOGOUT";
    private HandlerThread handlerThread = null;
    private Handler workHandler = null;
    private DBHelper databaseHelper = null;
    private AllOnlineApiClient allOnlineAPIClient = null;
    /**
     * 环信用户id，与用户资料对应关系键值对
     */
    private static HashMap<String, CommunityUser> emUserMaps = new HashMap<String, CommunityUser>();

    //一次向服务器请求的最多用户个数
    private final int REQUEST_PER_MAX = 500;

    private void initSonThread()
    {
        if (handlerThread != null && workHandler != null)
        {
            return;
        }
        handlerThread = new HandlerThread("get_hx_user_info");
        handlerThread.start();
        workHandler = new Handler(handlerThread.getLooper());
    }

    private void destroySonThread()
    {
        if (workHandler != null)
        {
            workHandler.removeCallbacksAndMessages(null);
            workHandler = null;
        }
        if (handlerThread != null)
        {
            handlerThread.quit();
            handlerThread = null;
        }
    }

    public static GMUserUtil getInstance()
    {
        return EmUserUtilHolder.instance;
    }

    // 用于线程安全的单例模式
    private static class EmUserUtilHolder
    {
        public final static GMUserUtil instance = new GMUserUtil();
    }

    private static Handler mainHandler;

    private GMUserUtil()
    {
        allOnlineAPIClient = new AllOnlineApiClient(AllOnlineApp.mApp.getApplicationContext());
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public DBHelper getHelper()
    {
        if (databaseHelper == null)
        {
            databaseHelper = OpenHelperManager.getHelper(AllOnlineApp.mApp.getApplicationContext(), DBHelper.class);
        }
        return databaseHelper;
    }
    private synchronized void addOrUpdateDB(ArrayList<CommunityUser> arrayList)
    {
        if (arrayList != null && arrayList.size() > 0)
        {
            try
            {
                Dao dao = getHelper().getDao(HxDBUser.class);
                HxDBUser hxUser;
                for (CommunityUser user : arrayList)
                {
                    putUserInMemoryCache(user.getUid(), user);
                    hxUser = new HxDBUser();
                    hxUser.setUserId(user.getUid());
                    hxUser.setUser(user);
                    List<HxDBUser> list = (List<HxDBUser>) dao.queryForEq("user_id", user.getUid());
                    if (list != null && list.size() > 0)
                    {
                        hxUser.setId((list.get(0)).getId());
                        dao.update(hxUser);
                    }
                    else
                    {
                        dao.create(hxUser);
                    }
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static void callbackOnMainThread(final GMUserUtilCallBackSingnle callback, final CommunityUser user)
    {
        mainHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (callback != null)
                {
                    callback.callbackUser(user);
                }
            }
        });
    }
    public interface GmUserUtilCallBackList
    {
        void callbackUsers(ArrayList<CommunityUser> users);
    }

    public interface GmUserUtilCallBackHxUsersList
    {
        void callbackUsers(List<HxDBUser> users);
    }

    public interface GMUserUtilCallBackSingnle
    {
        void callbackUser(CommunityUser user);
    }
    /**
     * 本地数据库请求--单个环信用户信息
     *
     * @param userId
     * @return
     */
    public HxDBUser queryUser(String userId)
    {
        if (userId == null)
        {
            return null;
        }
        try
        {
            Dao dao = getHelper().getDao(HxDBUser.class);
            List<HxDBUser> listHDBUsers = dao.queryForEq("user_id", userId);
            if (listHDBUsers != null && listHDBUsers.size() > 0)
            {
                return listHDBUsers.get(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 网络请求，异步回调
     */
    public void requestUserFromServer(final List<String> listIds, final GmUserUtilCallBackHxUsersList callback)
    {
        final ArrayList<CommunityUser> listUsers = requestServerUserInfo(listIds);
        if(callback != null)
        {
            List<HxDBUser> listHXUsers = new ArrayList<HxDBUser>();
            for (CommunityUser user : listUsers)
            {
                HxDBUser hxUser = new HxDBUser();
                hxUser.setUserId(user.getUid());
                hxUser.setUser(user);
                listHXUsers.add(hxUser);
            }
            callback.callbackUsers(listHXUsers);
        }

        if(listUsers != null && listUsers.size() > 10)
        {
            //数据较多就新开一个线程保存
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    addOrUpdateDB(listUsers);
                }
            }).start();
        }
        else
        {
            addOrUpdateDB(listUsers);
        }
    }

    /**
     * 网络请求，异步回调
     */
    public void requestUserFromServer(final List<String> list, final GmUserUtilCallBackList callback)
    {
        final ArrayList<CommunityUser> listUsers = requestServerUserInfo(list);
        if(callback != null)
        {
            callback.callbackUsers(listUsers);
        }

        if(listUsers != null && listUsers.size() > 10)
        {
            //数据较多就另外新开一个线程保存
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    addOrUpdateDB(listUsers);
                }
            }).start();
        }
        else
        {
            addOrUpdateDB(listUsers);
        }
    }

    /**
     * 判断用户个数，分段请求
     */
    private ArrayList<CommunityUser> requestServerUserInfo(List<String> list)
    {
        ArrayList<CommunityUser> listUsers = new ArrayList<CommunityUser>();
        if (list != null && list.size() > 0)
        {
            int iListSize = list.size();
            int iMainCount = 0;
            List<String> listOne = null;
            do
            {
                if (iListSize - iMainCount * REQUEST_PER_MAX > REQUEST_PER_MAX)
                {
                    int iCurrentCount = iMainCount;
                    ++iMainCount;
                    listOne = list.subList(iCurrentCount * REQUEST_PER_MAX, iMainCount * REQUEST_PER_MAX);
                }
                else
                {
                    listOne = list.subList(iMainCount * REQUEST_PER_MAX, iListSize);
                    ++iMainCount;
                }

                ArrayList<CommunityUser> listTemp = sendRequestToServer(listOne);
                if(listTemp != null && listTemp.size() > 0)
                {
                    listUsers.addAll(listTemp);
                }

            } while (iListSize - iMainCount * REQUEST_PER_MAX > 0);
            if(listOne != null)
            {
                listOne.clear();
            }
        }

        return listUsers;
    }

    /***一次最多请求REQUEST_PER_MAX条***/
    private ArrayList<CommunityUser> sendRequestToServer(List<String> list)
    {
        if (list != null && list.size() > 0)
        {
            final StringBuilder sb = new StringBuilder("");
            for (String uid : list)
            {
                if (!CommunityUtil.isEmptyString(uid))
                {
                    sb.append(uid);
                    sb.append(",");
                }
            }
            if (sb.length() <= 0)
            {
                return null;
            }
            else
            {
                // 移除最后一个逗号
                sb.deleteCharAt(sb.length() - 1);
            }
            if (allOnlineAPIClient == null)
            {
                allOnlineAPIClient = new AllOnlineApiClient(AllOnlineApp.mApp.getApplicationContext());
            }

            if (!NetworkUtil.getInstance().isNetWorkConnected())
            {
                mainHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        CommunityUtil.showMsg(R.string.network_error);
                    }
                });
                return null;
            }
            Result response = allOnlineAPIClient.queryBaseProfile(this.hashCode(), CommonUtil.getTicket(), sb.toString());
            if(response != null)
            {
                if(response.success)
                {
                    return (ArrayList<CommunityUser>) response.mResult;
                }
            }
        }
        return null;
    }

    public synchronized static void putUserInMemoryCache(String userId, CommunityUser user)
    {
        if (emUserMaps != null && emUserMaps.size() > 2000)
        {
            emUserMaps.clear();
        }
        if (emUserMaps == null)
        {
            emUserMaps = new HashMap<String, CommunityUser>();
        }
        emUserMaps.put(userId, user);
    }

    public static CommunityUser getUserInMemoryCache(String userId)
    {
        if (emUserMaps != null)
        {
            return emUserMaps.get(userId);
        }
        return null;
    }
    public void getUserSync(final String userId, final GMMessage message, final GMUserUtilCallBackSingnle callBackSingnle)
    {
        if(TextUtils.isEmpty(userId) || userId.toLowerCase().equals("admin"))
        {
            return;
        }
        initSonThread();
        workHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                HxDBUser hxDBUser = queryUser(userId);
                if (hxDBUser != null)
                {
                    final CommunityUser user = hxDBUser.getUser();
                    if (user != null)
                    {
                        callbackOnMainThread(callBackSingnle, user);
                        putUserInMemoryCache(userId, user);
                        return;
                    }
                }

                //从网络获取数据
                ArrayList<String> listIds = new ArrayList<String>();
                listIds.add(userId);
                requestUserFromServer(listIds, new GmUserUtilCallBackList()
                {
                    @Override
                    public void callbackUsers(ArrayList<CommunityUser> users)
                    {
                        if (users != null && users.size() > 0 && users.get(0) != null)
                        {
                            callbackOnMainThread(callBackSingnle, users.get(0));
                        }
                    }
                });
            }
        });
    }
    public String getCacheOrAttributeNickname(String userId, GMMessage message)
    {
        if (emUserMaps != null && emUserMaps.containsKey(userId) && emUserMaps.get(userId) != null)
        {
            return emUserMaps.get(userId).getName();
        }
        else if (message != null)
        {
            String nickname = GMCommonUtils.getMessageExtString(message, GMAppConstant.GOOME_NICKNAME, "");
            if (!TextUtils.isEmpty(nickname))
            {
                return nickname;
            }
        }

        return "";
    }

    public Handler getSonWorkHandler()
    {
        initSonThread();
        return workHandler;
    }

    /**
     * 从数据库获取的。所以最好在访问的时候只调用一次；需要多次使用的，在调用的地方用全局变量保存然后多次使用
     * */
    public static String getGroupManagerUserId(String toChatGroupId)
    {
        if (toChatGroupId != null)
        {
            GMGroup group = GMClient.getInstance().groupManager().getGroup(toChatGroupId);
            if (group != null)
            {
                return group.getOwner();
            }

        }
        return "";
    }

    public void sendLogoutBroadcast(Context context)
    {
        if(context == null)
        {
            return;
        }
        context.sendBroadcast(new Intent(GMUSER_LOGOUT_ACTION));
    }

    /**
     * 释放资源
     */
    public void release()
    {
        destroySonThread();

        if (emUserMaps != null)
        {
            emUserMaps.clear();
            emUserMaps = null;
        }

        try
        {
            if(workHandler != null)
            {
                workHandler.removeCallbacksAndMessages(null);
                workHandler = null;
            }

            if(handlerThread != null)
            {
                handlerThread.quit();
                handlerThread = null;
            }

            //可能退出后的那几秒内还有数据库访问，导致崩溃--干脆不关闭数据库了，因为app已经退出
//            if (databaseHelper != null)
//            {
//                OpenHelperManager.releaseHelper();
//                databaseHelper = null;
//            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** 如果群成员较多，需要多次从服务器获取完成 */
    public List<String> getMembersFromServer(String groupId) {
        List<String> memberList = new ArrayList<String>();
        GMCursorResult<String> result = null;
        final int pageSize = 200;
        try {
            do {
                result = GMClient.getInstance().groupManager().fetchGroupMembers(groupId, result != null ?
                    result.getCursor() : "", pageSize);
                memberList.addAll(result.getData());
            } while (result.getData().size() == pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return memberList;
    }

    /**
     * 如果是自己，在外部一般是adapter中另外单独处理
     */
    public static void setUserInfo(final Context context, final TextView textView, final ImageView imageView,
        int avatorSize, GMMessage message) {
        String headIcon = null;
        String nickName = null;
        if (message != null) {
            try {
                headIcon = GMCommonUtils.getMessageExtString(message, GMAppConstant.GOOME_AVATAR, "");
                nickName = GMCommonUtils.getMessageExtString(message, GMAppConstant.GOOME_NICKNAME, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (textView != null) {
            if (message != null && message.getDirection() == MsgDirection.SEND) {
                textView.setText("我");
            } else {
                textView.setText(nickName);
            }
        }
        if (message.getChatType() == GMConstant.ConversationType.CAROLVOICEROOM) {
            imageView.setImageResource(R.drawable.im_voice_default_avatar);
        } else {
            GlideApp.with(context).load(headIcon)
                .override(avatorSize, avatorSize)
                .placeholder(R.drawable.login_icon_large)
                .error(R.drawable.login_icon_large)
                .into(imageView);
        }
    }

    public static void loadUserHead(Context context, ImageView imageView, CommunityUser user)
    {
        if(imageView == null)
        {
            return;
        }
        int px = context.getResources().getDimensionPixelOffset(R.dimen.space_7x);

        GlideApp.with(context).load(user.getImg())
            .override(px, px)
            .placeholder(R.drawable.login_icon_large)
            .error(R.drawable.login_icon_large)
            .into(imageView);
    }
}
