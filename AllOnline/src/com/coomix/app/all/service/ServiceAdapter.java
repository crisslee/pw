package com.coomix.app.all.service;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.log.LogUploadInfo;
import com.coomix.app.all.model.event.SwitchUserEvent;
import com.coomix.app.all.model.response.CommunityUser;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.NetworkUtil;
import com.coomix.app.redpacket.util.CreateRedPacketInfo;
import com.tencent.bugly.crashreport.CrashReport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;

public class ServiceAdapter
{
    private AllOnlineApiClient mClient;
    private Context mContext;
    protected static final int MAX_POOL_SIZE = 10;
    protected static final int CORE_POOL_SIZE = 5;
    private static ServiceAdapter instance = null;
    protected ExecutorService pool;
    protected Map<Integer, ServiceAdapterCallback> observers;
    protected int messageId = 1000;
//    public static final int REQUEST_CALLBACK = 0;
    protected MyCallBackHandler mHandler;

    private ServiceAdapter(Context context)
    {
        this.mContext = context.getApplicationContext();
        mHandler = new MyCallBackHandler();
        pool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardPolicy());
        mClient = new AllOnlineApiClient(context.getApplicationContext());
        observers = Collections.synchronizedMap(new ConcurrentHashMap<Integer, ServiceAdapterCallback>());
    }

    public synchronized static ServiceAdapter getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new ServiceAdapter(context);
        }
        return instance;
    }

    public void release()
    {
        try
        {
            if (pool != null && !pool.isShutdown())
            {
                pool.shutdown();
            }
            observers.clear();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Integer.MAX_VALUE = 2147483647
    public synchronized int generateID()
    {
        if (messageId >= Integer.MAX_VALUE - 1)
        {
            messageId = 1000;
        }
        messageId++;
        return messageId;
    }

    public void registerServiceCallBack(ServiceAdapterCallback callback)
    {
        if(observers == null)
        {
            observers = Collections.synchronizedMap(new ConcurrentHashMap<Integer, ServiceAdapterCallback>());
        }
        if(callback != null)
        {
            observers.put(callback.hashCode(), callback);
        }
    }

    public void unregisterServiceCallBack(ServiceAdapterCallback callback)
    {
        if(observers != null && callback != null && observers.containsKey(callback.hashCode()))
        {
            observers.remove(callback.hashCode());
        }
    }

    public interface ServiceAdapterCallback
    {
        public void callback(int messageId, Result result);
    }

    private class MyCallBackHandler extends Handler
    {
        protected MyCallBackHandler()
        {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
        }
    }

    private synchronized void startCallback(final Result result, final int messageId, final int hashCode)
    {
        try
        {
            if(mHandler == null)
            {
                mHandler = new MyCallBackHandler();
            }
            //主线程中回调
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    ServiceAdapterCallback callbackByHashCode = observers.get(hashCode);
                    if(callbackByHashCode != null)
                    {
                        callbackByHashCode.callback(messageId, result);
                    }
                    else
                    {
                        for (ServiceAdapterCallback callback : observers.values())
                        {
                            if(callback != null)
                            {
                                callback.callback(messageId, result);
                            }
                        }
                    }
                }
            });
        }
        catch (Exception e)
        {
            if (Constant.IS_DEBUG_MODE)
            {
                e.printStackTrace();
            }
        }
    }

    public int qQHttpLogin(final int hashcode, final String nickname, final String openid)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.QQLOGIN_WEB);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.qQHttpLogin(nickname, openid);
                    result.apiCode = Constant.QQLOGIN_WEB;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }

        return id;
    }

    /**
     * QQ体验
     *
     * @param hashcode
     * @param account
     * @return
     */
    public int experienceQQUser(final int hashcode, final String account)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.experienceQQUser);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.experienceQQUser(account);
                    result.apiCode = Constant.experienceQQUser;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }

        return id;
    }


    // 绑定QQ号
    public int bindQQCount(final int hashcode, final String account, final String password, final String nickname, final String openid)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.QQBIND_WEB);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.bindQQCount(account, password, nickname, openid);
                    result.apiCode = Constant.QQBIND_WEB;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }

        return id;
    }

    // 解除qq绑定登录
    public int unBindQQCount(final int hashcode, final String openid, final String account, final String accessToken)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.UNQQBIND_WEB);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.unBindQQCount(openid, account, accessToken);
                    result.apiCode = Constant.UNQQBIND_WEB;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }

        return id;
    }

    // FORGET PASSWORD INTERFACE
    public int forgetBindPhoneSms(final int hashcode, final String accessToken, final String phoneNum, final String account)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FORGET_BIND_PHONE_SMS);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.forgetBindPhoneSms(accessToken, phoneNum, account);
                    result.apiCode = Constant.FORGET_BIND_PHONE_SMS;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int forgetPwBind(final int hashcode, final String accessToken, final String phoneNum, final String smsCode, final String account)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FORGETPW_BIND);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.forgetPwBind(accessToken, phoneNum, smsCode, account);
                    result.apiCode = Constant.FORGETPW_BIND;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int forgetPwValidate(final int hashcode, final String account)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FORGETPW_VALIDATE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.forgetPwValidate(account);
                    result.apiCode = Constant.FORGETPW_VALIDATE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int forgetPwReset(final int hashcode, final String validateNum, final String newPw, final String account)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FORGETPW_RESET);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.forgetPwReset(validateNum, newPw, account);
                    result.apiCode = Constant.FORGETPW_RESET;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    protected void processNetwork(final int hashcode, final int id, final int apiCode)
    {
        Result result = new Result();
        result.statusCode = Result.ERROR_NETWORK;
        result.apiCode = apiCode;
        startCallback(result, id, hashcode);
    }

    public int monitor(final int hashcode, final String target, final String account, final String access_token, final String mapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_MONITOR);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.monitor(target, account, access_token, mapType);
                    result.apiCode = Constant.FM_APIID_MONITOR;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int tracking(final int hashcode, final String[] imeis, final String account, final String access_token,
        final String mapType) {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            processNetwork(hashcode, id, Constant.FM_APIID_TRACKING);
        } else {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Result result = mClient.tracking(imeis, account, access_token, mapType);
                    result.apiCode = Constant.FM_APIID_TRACKING;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown()) {
                pool.execute(task);
            }
        }
        return id;
    }

    public int history(final int hashcode, final String access_token, final String imei, final String account, final long begin_time, final long end_time, final long cur_time, final String mapType, final int limit)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_HISTROY);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.history(access_token, imei, account, begin_time, end_time, cur_time, mapType, limit);
                    result.apiCode = Constant.FM_APIID_HISTROY;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int reverseGeo(final int hashcode, final String access_token, final double lng, final double lat, final String account, final String mapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_REVERSE_GEO);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.reverseGeo(access_token, lng, lat, account, mapType);
                    result.apiCode = Constant.FM_APIID_REVERSE_GEO;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int reverseGeo(final int hashcode, final int signId, final String access_token, final double lng, final double lat, final String account, final String mapType)
    {
        final int id = signId;
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_REVERSE_GEO);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.reverseGeo(access_token, lng, lat, account, mapType);
                    result.apiCode = Constant.FM_APIID_REVERSE_GEO;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 修改设备信息
     *
     * @param hashcode
     * @param access_token
     * @param account
     * @return
     */
    public int modifyUser(final int hashcode, final String access_token, final String user_name, final String phone, final String sex, final String owner, final String tel,
                          final String user_id, final String account, final String remark, final String deviceremark, final int isdealer)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.MODIFUUSER);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.modifyUser(access_token, user_name, phone, sex, owner, tel, user_id, account, remark, deviceremark, isdealer);                    result.apiCode = Constant.MODIFUUSER;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int devinfo(final int hashcode, final String access_token, final String target, final String account)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_DEVICE_INFO);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.devinfo(access_token, target, account);
                    result.apiCode = Constant.FM_APIID_DEVICE_INFO;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int adjust(final int hashcode, final String access_token, final String account, final String mapType, final Location location)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_ADJUST_LATLNG);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.adjust(access_token, account, mapType, location);
                    result.apiCode = Constant.FM_APIID_ADJUST_LATLNG;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int queryFence(final int hashcode, final String access_token, final String account, final String mapType, final String imei , final String ... alarmId)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_QUERY_FENCE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.queryFence(access_token, imei, account, mapType, alarmId);
                    result.apiCode = Constant.FM_APIID_QUERY_FENCE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int addFence(final int hashcode, final String access_token, final int shapeType, final String shapeParam, final int validateFlag, final String account, final String mapType, final String imei)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_ADD_FENCE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.AddFence(access_token, imei, shapeType, shapeParam, validateFlag, account, mapType);
                    result.apiCode = Constant.FM_APIID_ADD_FENCE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int switchFence(final int hashcode, final String access_token, final String fenceId, final int validateFlag, final String account, final String mapType, final String imei)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_SWITCH_FENCE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.switchFence(access_token, imei, fenceId, validateFlag, account, mapType);
                    result.apiCode = Constant.FM_APIID_SWITCH_FENCE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int modifyPwd(final int hashcode, final String token, final String account, final String oldpwd, final String newpwd)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_MODIFY_PWD);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.modifyPwd(token, account, oldpwd, newpwd);
                    result.apiCode = Constant.FM_APIID_MODIFY_PWD;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getAlarmInfo(final int hashcode, final String token, final String target, final String account, final int pagesize, final String type, final String filter, final long timestamp, final String imei, final String mapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GEI_ALARMS);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getAlarmInfo(token, target, account, pagesize, type, filter, timestamp, imei, mapType);
                    result.apiCode = Constant.FM_APIID_GEI_ALARMS;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getAlarmCategoryList(final int hashCode, final String token, final String account, final long timestamp, final String imei, final String logintype, final String mapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashCode, id, Constant.FM_APIID_alarmCategoryList);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getAlarmCategoryList(token, account, timestamp, imei, logintype, mapType);
                    result.apiCode = Constant.FM_APIID_alarmCategoryList;
                    startCallback(result, id, hashCode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getAllAlarmCount(final int hashCode, final String token, final String account, final long timestamp, final String imei, final String logintype, final String mapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashCode, id, Constant.FM_APIID_AllAlarmCount);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getAllAlarmCount(token, account, timestamp, imei, logintype, mapType);
                    result.apiCode = Constant.FM_APIID_AllAlarmCount;
                    startCallback(result, id, hashCode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getAlarmCategoryItemList(final int hashcode, final String token, final String account, final long timestamp, final String pageDir, final int pageSize, final int alarmType, final String imei, final String logintype, final String mapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_alarmCategoryItemList);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getAlarmCategoryItemList(token, account, timestamp, pageDir, pageSize, alarmType, imei, logintype, mapType);
                    result.apiCode = Constant.FM_APIID_alarmCategoryItemList;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getDeviceCommand(final int hashcode, final String token, final String account, final String dev_type, final String imei)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GEI_COMMAND);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getDeviceCommand(token, account, dev_type, imei);
                    result.apiCode = Constant.FM_APIID_GEI_COMMAND;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int sendCommand(final int hashcode, final String token, final String account, final String imei, final String cmdID, final String cmdParams)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_SET_COMMAND);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.sendCommand(token, account, imei, cmdID, cmdParams);
                    result.apiCode = Constant.FM_APIID_SET_COMMAND;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getCommandResponse(final int hashcode, final String token, final String account, final String respID, final String imei)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GEI_COMMAND_RESP);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getCommandResponse(token, account, respID, imei);
                    result.apiCode = Constant.FM_APIID_GEI_COMMAND_RESP;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int setAlarmPhoneNum(final int hashcode, final String token, final String account, final String phoneNum, final String imei, final String fenceId)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_SET_ALARM_PHONE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.setAlarmPhoneNum(token, account, phoneNum, imei, fenceId);
                    result.apiCode = Constant.FM_APIID_SET_ALARM_PHONE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int setRead(final int hashcode, final String token, final String login_type, final String account,final String imei,
                       final ArrayList<String> ids, Long timeStamp,String except, final String alarm_type)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_SET_ALARM_READ);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.setRead(token, login_type, account, imei,ids, timeStamp,except, alarm_type);
                    result.apiCode = Constant.FM_APIID_SET_ALARM_READ;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getAlarmTypes(final int hashcode, final String token, final String account, final String alias)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_ALARM_SETTING);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getAlarmTypes(token, account, alias);
                    result.apiCode = Constant.FM_APIID_GET_ALARM_SETTING;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int setAlarmTypes(final int hashcode, final String token, final String account, final String alias, final boolean push, final int startTime, final int endTime, final String alarmTypeIds, final boolean shake, final boolean sound)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_SET_ALARM_SETTING);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.setAlarmTypes(token, account, alias, push, startTime, endTime, alarmTypeIds, shake, sound);
                    result.apiCode = Constant.FM_APIID_SET_ALARM_SETTING;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int rectSearch(final int hashcode, final double toprightlng, final double toprightlat,
        final double bottomleftlng, final double bottomleftlat, final double centerlng, final double centerlat,
        final double distance, final String access_token) {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            processNetwork(hashcode, id, Constant.FM_APIID_RECTSEARCH);
        } else {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Result result = mClient.rectSearch(toprightlng, toprightlat, bottomleftlng, bottomleftlat,
                        centerlng, centerlat, distance, access_token);
                    result.apiCode = Constant.FM_APIID_RECTSEARCH;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown()) {
                pool.execute(task);
            }
        }
        return id;
    }

    public int provinceList(final int hashcode, final String account, final String access_token)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_PROVINCE_LIST);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.provinceList(account, access_token);
                    result.apiCode = Constant.FM_APIID_PROVINCE_LIST;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int cityList(final int hashcode, final String provinceId, final String account, final String access_token)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_CITY_LIST);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.cityList(provinceId, account, access_token);
                    result.apiCode = Constant.FM_APIID_CITY_LIST;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getOverspeed(final int hashcode, final String imei, final String account, final String access_token)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_OVERSPEED);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getOverspeed(imei, account, access_token);
                    result.apiCode = Constant.FM_APIID_GET_OVERSPEED;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int setOverspeed(final int hashcode, final String imei, final boolean flag, final int speed, final String account, final String access_token)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_SET_OVERSPEED);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.setOverspeed(imei, flag, speed, account, access_token);
                    result.apiCode = Constant.FM_APIID_SET_OVERSPEED;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getAreaFence(final int hashcode, final String imei, final String account, final String access_token, final String alarmId)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_AREAFENCE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getAreaFence(imei, account, access_token, alarmId);
                    result.apiCode = Constant.FM_APIID_GET_AREAFENCE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int setAreaFence(final int hashcode, final String imei, final boolean flag, final String areaId, final String account, final String access_token)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_SET_AREAFENCE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.setAreaFence(imei, flag, areaId, account, access_token);
                    result.apiCode = Constant.FM_APIID_SET_AREAFENCE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int switchAreaFence(final int hashcode, final String imei, final String idString, final boolean flag, final String account, final String access_token)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_SWITCH_AREAFENCE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.switchAreaFence(imei, idString, flag, account, access_token);
                    result.apiCode = Constant.FM_APIID_SWITCH_AREAFENCE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int uploadLocation(final int hashcode, final double lng, final double lat, final long gpsTime, final String mapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_UPLOAD_LOCATION);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.uploadLocation(lng, lat, gpsTime, mapType);
                    result.apiCode = Constant.FM_APIID_UPLOAD_LOCATION;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int queryDeviceSetting(final int hashcode, final String imei, final String account, final String accessToken, final String mapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_QUERY_DEVICESETTING);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.queryDeviceSetting(imei, account, accessToken, mapType);
                    result.apiCode = Constant.FM_APIID_QUERY_DEVICESETTING;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getAuthPages(final int hashcode, final String accessToken)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_AUTH_PAGES);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getAuthPages(accessToken);
                    result.apiCode = Constant.FM_APIID_GET_AUTH_PAGES;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getAd(final int hashcode, final int width, final int height, final double lat, final double lng, final String mapType, final String posMapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_AD);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getAd(width, height, lat, lng, mapType, posMapType);
                    result.apiCode = Constant.FM_APIID_GET_AD;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int downloadImage(final int hashcode, final String url, final String fileName)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_DOWNLOAD_IMG);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.downloadImage(url, fileName);
                    result.apiCode = Constant.FM_APIID_DOWNLOAD_IMG;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int clickAdvertiser(final int hashcode, final int adId, final int cityCode, final double lat, final double lng, final String mapType, final String posMapType, final int type)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_UPLOAD_AD_CLICK);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.clickAdvertiser(adId, cityCode, lat, lng, mapType, posMapType, type);
                    result.apiCode = Constant.FM_APIID_UPLOAD_AD_CLICK;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getBlacklist(final int hashcode, final String condition, final String account, final String access_token)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_BLACKLIST);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getBlacklist(condition, account, access_token);
                    result.apiCode = Constant.FM_APIID_BLACKLIST;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getLatestVersion(final int hashcode)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_LATEST_VERSION);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getLatestVersion();
                    result.apiCode = Constant.FM_APIID_LATEST_VERSION;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int filterDevInfo(final int hashcode, final String target, final String account, final String access_token, final String mapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_FILTER_DEVINFO);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.filterDevInfo(target, account, access_token, mapType);
                    result.apiCode = Constant.FM_APIID_FILTER_DEVINFO;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    //    public int subFilterDevInfo(final int hashcode,final String target, final String account, final String access_token, final String mapType) {
    //        final int id = generateID();
    //        if (!NetworkUtil.isNetworkConnected(mContext)) {
    //        	 processNetwork(hashcode, id, Constant.FM_APIID_FILTER_DEVINFO);
    //        } else {
    //            Runnable task = new Runnable() {
    //                @Override
    //                public void run() {               
    //                    Result result = mClient.subFilterDevInfo(target, account, access_token, mapType);
    //                    result.apiCode = Constant.FM_APIID_FILTER_DEVINFO;
    //                    startCallback(result, id, hashcode);
    //                }
    //            };
    //            pool.execute(task);
    //        }
    //        return id;
    //    }


    public int getDevSearch(final int hashcode, final String target, final String account, final String access_token, final String mapType, final String value, final int stype)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_DEV_SEARCH_SEARCH);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getDevSearch(target, account, access_token, mapType, value, stype);
                    result.apiCode = Constant.FM_APIID_DEV_SEARCH_SEARCH;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int getSingleDevInfo(final int hashcode, final String imei, final String account, final String access_token,
        final String mapType) {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            processNetwork(hashcode, id, Constant.FM_APIID_DEV_SINGLE);
        } else {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Result result = mClient.getSingleDevInfo(imei, account, access_token, mapType);
                    result.apiCode = Constant.FM_APIID_DEV_SINGLE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown()) {
                pool.execute(task);
            }
        }
        return id;
    }

    public int subDevinfo(final int hashcode, final String access_token, final String target, final String account)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_DEV_SUB);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.subDevinfo(access_token, target, account);
                    result.apiCode = Constant.FM_APIID_DEV_SUB;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int readFirstHost(final int hashcode, final String domain)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_FIRST_HOST);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.readFirstHost(domain);
                    result.apiCode = Constant.FM_APIID_FIRST_HOST;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int pictureUpload(final int hashcode, final String account, final String token, final int index)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_PICTURE_UPLOAD);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.pictureUpload(account, token, index);
                    result.apiCode = Constant.FM_APIID_PICTURE_UPLOAD;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int logUpload(final int hashcode, final LogUploadInfo info)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_LOG_UPLOAD);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.logUpload(info);
                    result.apiCode = Constant.FM_APIID_LOG_UPLOAD;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int performUpload(final int hashcode, final String data)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_UPLOAD_PERFORMANCE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.performUpload(data);
                    result.apiCode = Constant.FM_APIID_UPLOAD_PERFORMANCE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int logGetConfig(final int hashcode, final LogUploadInfo info)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_LOG_GETCONFIG);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.logGetConfig(info);
                    result.apiCode = Constant.FM_APIID_LOG_GETCONFIG;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /************
     * 社区相关接口
     ************/
    /**
     * 修改用户信息
     *
     * @param map
     */
    public int userModify(final int hashcode, final HashMap<String, Object> map)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_userModify);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.userModify(map);
                    result.apiCode = Constant.FM_APIID_userModify;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 获取用户信息
     *
     * @param uid
     * @param ticket
     * @return
     */
    public int userDetailInfo(final int hashcode, final String uid, final String ticket)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_userDetailInfo);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.userDetailInfo(uid, ticket);
                    result.apiCode = Constant.FM_APIID_userDetailInfo;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 图片上传
     *
     * @param ticket
     * @param path
     * @param mimeType
     * @return
     */
    public int pictureUpload(final int hashcode, final String ticket, final String path, final String mimeType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_pictureUpload);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.pictureUpload(ticket, path, mimeType);
                    result.apiCode = Constant.FM_APIID_pictureUpload;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }


    /**
     * 获取活动列表
     *
     * @param type
     * @param citycode
     * @param last_pointer
     * @param last_id
     * @param num
     * @return
     */
    public int getActivityList(final int hashcode, final int type, final String citycode, final int category_id, final double last_pointer, final String last_id, final long num)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_getActivityList);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getActivityList(type, citycode, category_id, last_pointer, last_id, num);
                    result.apiCode = Constant.FM_APIID_getActivityList;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 获取活动详情
     *
     * @param hashcode
     * @param ticket
     * @param aid
     * @return
     */
    public int getActivityDetail(final int hashcode, final String ticket, final int aid)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_getActivityDetail);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getActivityDetail(ticket, aid);
                    result.apiCode = Constant.FM_APIID_getActivityDetail;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 活动报名
     *
     * @param hashcode
     * @param ticket
     * @param type
     * @param aid
     * @param name
     * @param tel
     * @param qqorwx
     * @return
     */
    public int sendSignInfo(final int hashcode, final String ticket, final int type, final int aid, final long order_id, final String name, final String tel, final String qqorwx, final String addr, final String extend_items, final double lat, final double lon)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_sendSignInfo);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.sendSignInfo(ticket, type, aid, order_id, name, tel, qqorwx, addr, extend_items, lat, lon);
                    result.apiCode = Constant.FM_APIID_sendSignInfo;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 获取我的报名的活动信息
     *
     * @param hashcode
     * @param ticket
     * @param aid
     * @return
     */
    public int getMySignedInfo(final int hashcode, final String ticket, final int aid)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_getMySignedInfo);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getMySignedInfo(ticket, aid);
                    result.apiCode = Constant.FM_APIID_getMySignedInfo;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 获取获取所有广告
     *
     * @param hashcode
     * @param sWidth
     * @param sHeight
     * @param lat
     * @param lng
     * @param cityCode
     * @return
     */
    public int getAllAd(final int hashcode, final int sWidth, final int sHeight, final double lat, final double lng, final String cityCode)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_getAllAd);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getAllAd(sWidth, sHeight, lat, lng, cityCode);
                    result.apiCode = Constant.FM_APIID_getAllAd;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public int switchCommunityUserAndHX(final int hashcode, final String access_token, final String cid, final String loginName)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            EventBus.getDefault().post(new SwitchUserEvent(false, 502, ""));
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.login(access_token, cid, loginName);
                    result.apiCode = Constant.FM_APIID_login;
                    if (result.success)
                    {
                        //switch huanxin
                        final CommunityUser user = (CommunityUser) result.mResult;
                        AllOnlineApp.loginGMIm(mContext);

                        try
                        {
                            String ticket = ((CommunityUser) result.mResult).getTicket();
                            if (!CommunityUtil.isEmptyTrimStringOrNull(ticket))
                            {
                                // 綁定gpns id
                                bindChannelId(0, ticket, AllOnlineApp.channelId(AllOnlineApp.mApp));
                            }
                        }
                        catch (Exception e)
                        {
                            CrashReport.postCatchedException(e);
                            EventBus.getDefault().post(new SwitchUserEvent(false, 502, ""));

                        }
                        EventBus.getDefault().post(new SwitchUserEvent(true, 0, ""));

                    }
                    else
                    {
                        EventBus.getDefault().post(new SwitchUserEvent(false, 502, ""));

                    }

                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 登录社区
     *
     * @return int
     */
    public int loginCommunity(final int hashcode, final String access_token, final String cid, final String loginName)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_login);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.login(access_token, cid, loginName);
                    result.apiCode = Constant.FM_APIID_login;
                    startCallback(result, id, hashcode);
                    if (result.success)
                    {
                        //loginCommunity huanxin
                        CommunityUser user = (CommunityUser) result.mResult;
                        //write_huanxin(user.getHxAccount(),user.getHxPwd());

                        try
                        {
                            String ticket = ((CommunityUser) result.mResult).getTicket();
                            if (!CommunityUtil.isEmptyTrimStringOrNull(ticket))
                            {
                                // 綁定gpns id
                                bindChannelId(0, ticket, AllOnlineApp.channelId(AllOnlineApp.mApp));
                            }
                            String uid = ((CommunityUser) result.mResult).getUid();
                            //上传用户信息
                            if (!TextUtils.isEmpty(ticket) && !TextUtils.isEmpty(uid))
                            {
                                Result startCallback = AllOnlineApp.mApiClient.senUserInfo(ticket, uid, "record_login", "0");
                            }
                        }
                        catch (Exception e)
                        {
                            CrashReport.postCatchedException(e);
                        }
                    }
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 绑定gpns推送cid
     *
     * @param ticket
     * @param cid
     * @return
     */
    public int bindChannelId(final int hashcode, final String ticket, final String cid)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_bindChannelId);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.bindChannelId(ticket, cid);
                    result.apiCode = Constant.FM_APIID_bindChannelId;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 运营账号登陆
     *
     * @param name
     * @param pwd
     * @param regist
     * @return
     */
    public int loginInner(final int hashcode, final String name, final String pwd, final int regist)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_loginInner);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.loginInner(name, pwd, regist);
                    result.apiCode = Constant.FM_APIID_loginInner;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 退出登录
     *
     * @param ticket    登录签名
     * @param channelId 推送的channelID
     * @return id
     */
    public int logoutCommunity(final int hashcode, final String ticket, final String channelId)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_logout);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.logoutCommunity(ticket, channelId);
                    result.apiCode = Constant.FM_APIID_logout;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 获取验证码
     *
     * @return response
     */
    public int getSmsCode(final int hashcode, final String phone, final String smsCode)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_getSmsCode);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getSmsCode(phone, smsCode);
                    result.apiCode = Constant.FM_APIID_getSmsCode;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 获取随机码
     *
     * @return response
     */
    public int getRandomCode(final int hashcode)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_getRandomCode);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getRandomCode();
                    result.apiCode = Constant.FM_APIID_getRandomCode;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 绑定手机号
     *
     * @return response
     */
    public int bindPhone(final int hashcode, final String access_token, final String ticket, final String cid, final String phone, final String smsCode, final boolean force)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_bindPhone);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.bindPhone(access_token, ticket, cid, phone, smsCode, force);
                    result.apiCode = Constant.FM_APIID_bindPhone;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 更换手机号
     *
     * @return response
     */
    public int modifyPhone(final int hashcode, final String access_token, final String ticket, final String phone, final String smsCode)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_modifyPhone);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.modifyPhone(access_token, ticket, phone, smsCode);
                    result.apiCode = Constant.FM_APIID_modifyPhone;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer getRedPacketConfig(final int hashcode, final String ticket, final int display_type)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_REDPACKET_CONFIG);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getRedPacketConfig(hashcode, ticket, display_type);
                    result.apiCode = Constant.FM_APIID_GET_REDPACKET_CONFIG;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer getPocketBalance(final int hashcode, final String ticket)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_POCKET_BALANCE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getPocketBalance(hashcode, ticket);
                    result.apiCode = Constant.FM_APIID_GET_POCKET_BALANCE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer createRedPacket(final int hashcode, final String ticket, final CreateRedPacketInfo createRedPacketInfo)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_CREATE_PAY_REDPACKET);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.createRedPacket(hashcode, ticket, createRedPacketInfo);
                    result.apiCode = Constant.FM_APIID_CREATE_PAY_REDPACKET;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer getUnsendRedPacketsByType(final int hashcode, final String ticket, final int display_type)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_UNSEND_REDPACKET);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getUnsendRedPacketsByType(hashcode, ticket, display_type);
                    result.apiCode = Constant.FM_APIID_GET_UNSEND_REDPACKET;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 红包-----拆红包
     */
    public Integer openRedPacket(final int hashcode, final String ticket, final String redpacket_id, final String mapType, final String lat, final String lng)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_OPEN_REDPACKET);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.openRedPacket(hashcode, ticket, redpacket_id, mapType, lat, lng);
                    result.apiCode = Constant.FM_APIID_OPEN_REDPACKET;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer getRedPacketInfoById(final int hashcode, final String ticket, final String redpacket_id, final double last_pointer, final int num, final String mapType, final String lat, final String lng, final int alloc_num)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_REDPACKET_INFO_BY_ID);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getRedPacketInfoById(hashcode, ticket, redpacket_id, last_pointer, num, mapType, lat, lng, alloc_num);
                    result.apiCode = Constant.FM_APIID_GET_REDPACKET_INFO_BY_ID;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer rechargeBalance(final int hashcode, final String ticket, final long amount, final int pay_platform, final int pay_manner)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_RECHARGE_BALANCE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.rechargeBalance(hashcode, ticket, amount, pay_platform, pay_manner);
                    result.apiCode = Constant.FM_APIID_RECHARGE_BALANCE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer withdrawBalance(final int hashcode, final String ticket, final String account, final long amount)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_WITHDRAW_BALANCE);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.withdrawBalance(hashcode, ticket, account, amount);
                    result.apiCode = Constant.FM_APIID_WITHDRAW_BALANCE;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer getRefreshPayOrderStatus(final int hashcode, final int aid, final long order_id)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_ORDER_STATUS);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getRefreshPayOrderStatus(hashcode, aid, order_id);
                    result.apiCode = Constant.FM_APIID_GET_ORDER_STATUS;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer getRechargeStatus(final int hashcode, final String ticket, final long order_id)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_RECHARGE_STATUS);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getRechargeStatus(hashcode, ticket, order_id);
                    result.apiCode = Constant.FM_APIID_GET_RECHARGE_STATUS;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    // 获取我的活动列表
    public Integer getMyActivityList(final int hashcode, final String ticket, final double lastPointer, final String lastId, final long num)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_MY_ACTIVITY_LIST);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getMyActivityList(hashcode, ticket, lastPointer, lastId, num);
                    result.apiCode = Constant.FM_APIID_GET_MY_ACTIVITY_LIST;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    /**
     * 支付活动费用
     */
    public Integer prepay(final int hashcode, final int aid, final long order_id, final int pay_platform, final int pay_manner)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_REQUEST_PREPAY);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.prepay(hashcode, aid, order_id, pay_platform, pay_manner);
                    result.apiCode = Constant.FM_APIID_REQUEST_PREPAY;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer getOrderInfo(final int hashcode, final String ticket, final int actId, final long order_id, final String mapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_ORDER_INFO);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getOrderInfo(hashcode, ticket, actId, order_id, mapType);
                    result.apiCode = Constant.FM_APIID_GET_ORDER_INFO;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer getDailyRedpacket(final int hashcode, final String citycode, final String posmaptype, final String lat, final String lng)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_GET_DAILY_RP);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getDailyRedpacket(hashcode, citycode, posmaptype, lat, lng);
                    result.apiCode = Constant.FM_APIID_GET_DAILY_RP;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer painting(final int hashcode, final String paintType, final String mapType)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_PAINTING);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.painting(hashcode, paintType, mapType);
                    result.apiCode = Constant.FM_APIID_PAINTING;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer rectPaint(final int hashcode, final String paintType, final String mapType, final double lat, final double lng, final int limit)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_RECTPAINT);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.rectPaint(hashcode, paintType, mapType, lat, lng, limit);
                    result.apiCode = Constant.FM_APIID_RECTPAINT;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }

    public Integer getPushAdv(final int hashcode)
    {
        final int id = generateID();
        if (!NetworkUtil.isNetworkConnected(mContext))
        {
            processNetwork(hashcode, id, Constant.FM_APIID_PUSHADV);
        }
        else
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    Result result = mClient.getPushAdv(hashcode);
                    result.apiCode = Constant.FM_APIID_PUSHADV;
                    startCallback(result, id, hashcode);
                }
            };
            if (pool != null && !pool.isShutdown())
            {
                pool.execute(task);
            }
        }
        return id;
    }
}
