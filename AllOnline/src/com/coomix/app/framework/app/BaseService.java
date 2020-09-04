package com.coomix.app.framework.app;

import android.app.Service;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseService extends Service {

    protected static final int MAX_POOL_SIZE = 10;
    protected static final int CORE_POOL_SIZE = 5;

    protected ExecutorService pool;
    protected Map<Integer, ServiceCallback> observers;
    protected static int messageId = 1000;
    public static final int REQUEST_CALLBACK = 0;
    protected CallbackHandler mHandler;
    
    public Handler getMessageHandler() {
        return mHandler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new CallbackHandler(this);
        pool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),new ThreadPoolExecutor.DiscardPolicy());
        observers = Collections.synchronizedMap(new HashMap<Integer, ServiceCallback>());

    }

    @Override
    public void onDestroy() {
        try {
            if (pool != null && !pool.isShutdown()) {
                pool.shutdown();
            }
            observers.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void registerServiceCallBack(ServiceCallback callback) {
        observers.put(callback.hashCode(), callback);
    }

    public void unregisterServiceCallBack(ServiceCallback callback) {
        observers.remove(callback.hashCode());
    }

    public interface ServiceCallback {
        public void callback(int messageId, Result result);
    }

    //Integer.MAX_VALUE = 2147483647
    public synchronized int generateID() {
        if (messageId >= Integer.MAX_VALUE - 1) {
            messageId = 1000;
        }
        messageId++;
        return messageId;
    }

    public void sendResult(Result result,int messageId, int hashcode){
        Message msg = getMessageHandler().obtainMessage(REQUEST_CALLBACK);
        msg.obj = result;
        msg.arg1 = hashcode;
        msg.arg2 = messageId;
        getMessageHandler().sendMessage(msg);
    }

    static class CallbackHandler extends Handler {
        private final WeakReference<BaseService> mService;

        CallbackHandler(BaseService service) {
            mService = new WeakReference<BaseService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseService service = mService.get();
            if (service != null && msg.what == REQUEST_CALLBACK) {
                for (ServiceCallback callback : service.observers.values()) {
                    if (msg.arg1 == callback.hashCode()) {
                        callback.callback(msg.arg2, (Result) msg.obj);
                        break;
                    }
                }

            }
        }
    }
    
}