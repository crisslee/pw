package com.coomix.app.all.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.OfflineCityItem;
import com.coomix.app.all.ui.main.OfflineBMapActivity;
import com.coomix.app.framework.app.BaseService.ServiceCallback;
import com.coomix.app.framework.util.CommonUtil;
import java.util.ArrayList;

//百度地图离线地图
public class OfflineMapService extends Service implements MKOfflineMapListener {
    private NotificationManager mNM;

    private MKOfflineMap mOffline;
    //    private MapView mMapView;

    private final IBinder mBinder = new LocalBinder();

    private ServiceCallback mServiceCallback;

    private int NOTIFICATION = 2130903085;

    private ArrayList<OfflineCityItem> mArrayListOfflineItem;
    private SparseArray<OfflineCityItem> mSparseArrayOfflineItem;
    private ArrayList<MKOLUpdateElement> mArrayListUpdates;

    private final static int UPDATE_ALL_ELEMENT = 100;
    private final static int DELETE_ELEMENT = 200;
    public final static int UPDATE_CITY_LIST = 300;

    @SuppressWarnings("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ALL_ELEMENT:
                    updateAllElement();
                    break;

                case DELETE_ELEMENT:
                    int cityId = (Integer) msg.obj;
                    OfflineCityItem updateItem = mSparseArrayOfflineItem.get(cityId);
                    updateItem.clearUpdateData();
                    updateAllElement();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //        if (AllOnlineApp.mApp != null) {
        //            mMapView = new MapView(AllOnlineApp.mApp);
        //        } else {
        //            stopService(new Intent(this, OfflineMapService.class));
        //            return;
        //        }

        mOffline = new MKOfflineMap();
        mOffline.init(this);

        mArrayListOfflineItem = new ArrayList<OfflineCityItem>();
        mSparseArrayOfflineItem = new SparseArray<OfflineCityItem>();
        getOfflineCityDatas();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void getOfflineCityDatas() {
        try {
            OfflineCityItem headerItem1 = new OfflineCityItem();
            headerItem1.label = getString(R.string.hot_cities);
            headerItem1.cityType = OfflineCityItem.HEADER;

            mArrayListOfflineItem.add(headerItem1);
            ArrayList<MKOLSearchRecord> records1 = mOffline.getHotCityList();
            for (MKOLSearchRecord r : records1) {
                OfflineCityItem item = new OfflineCityItem(r);
                mArrayListOfflineItem.add(item);
                mSparseArrayOfflineItem.put(item.cityId, item);
            }

            ArrayList<MKOLSearchRecord> records2 = mOffline.getOfflineCityList();
            OfflineCityItem headerItem2 = new OfflineCityItem();
            headerItem2.label = getString(R.string.all_cities);
            headerItem2.cityType = OfflineCityItem.HEADER;
            mArrayListOfflineItem.add(headerItem2);
            for (MKOLSearchRecord r : records2) {
                if (r.cityType == OfflineCityItem.CITY || r.cityType == OfflineCityItem.COUNTRY) {
                    OfflineCityItem cityItem = null;
                    if (mSparseArrayOfflineItem.indexOfKey(r.cityID) < 0) {
                        cityItem = new OfflineCityItem(r);
                        mSparseArrayOfflineItem.put(cityItem.cityId, cityItem);
                    } else {
                        cityItem = mSparseArrayOfflineItem.get(r.cityID);
                    }
                    mArrayListOfflineItem.add(cityItem);
                } else {
                    // for province to collect child cities
                    OfflineCityItem provinceItem = new OfflineCityItem(r, mSparseArrayOfflineItem);
                    mArrayListOfflineItem.add(provinceItem);
                }
            }

            mArrayListUpdates = mOffline.getAllUpdateInfo();
            if (mArrayListUpdates == null) {
                mArrayListUpdates = new ArrayList<MKOLUpdateElement>();
            }
            for (MKOLUpdateElement mkolUpdateElement : mArrayListUpdates) {
                OfflineCityItem updateItem = mSparseArrayOfflineItem.get(mkolUpdateElement.cityID);
                if (updateItem != null) {
                    updateItem.updateStatus(mkolUpdateElement);
                    // listAdapter.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerServiceCallback(ServiceCallback mServiceCallback) {
        this.mServiceCallback = mServiceCallback;
    }

    public void unregisterServiceCallback() {
        this.mServiceCallback = null;
    }

    private void requestQuickUpdateAllElement() {
        Message msg = mHandler.obtainMessage();
        msg.what = UPDATE_ALL_ELEMENT;
        msg.sendToTarget();
    }

    /**
     * Baidu Bug. When a city download ratio reach 100, the status is not switch
     * to finished immediately and following update is missing. Have to manually
     * get finish status by delay request
     */
    private void requestUpdateAllElement() {
        Message msg = mHandler.obtainMessage();
        msg.what = UPDATE_ALL_ELEMENT;
        mHandler.sendMessageDelayed(msg, 200);
    }

    private void requestRemoveElement(int cityId) {
        Message msg = mHandler.obtainMessage();
        msg.what = DELETE_ELEMENT;
        msg.obj = cityId;
        msg.sendToTarget();
    }

    public ArrayList<OfflineCityItem> getCityArrayList() {
        return mArrayListOfflineItem;
    }

    public SparseArray<OfflineCityItem> getCitySparseArray() {
        return mSparseArrayOfflineItem;
    }

    public ArrayList<MKOLUpdateElement> getUpdateCityData() {
        return mArrayListUpdates;
    }

    public void startDownload(OfflineCityItem item) {
        if (shouldBeDeleted(item)) {
            mOffline.remove(item.cityId);
        }
        mOffline.start(item.cityId);
        requestQuickUpdateAllElement();
    }

    public void startDownload(int cityId) {
        mOffline.start(cityId);
        requestQuickUpdateAllElement();
    }

    public void pauseDownload(int cityId) {
        mOffline.pause(cityId);
        requestQuickUpdateAllElement();
    }

    public void deleteCity(int cityId) {
        boolean isRemoved = mOffline.remove(cityId);
        if (isRemoved) {
            requestRemoveElement(cityId);
        }
    }

    public void updateCity(int cityId) {
        mOffline.remove(cityId);
        mOffline.start(cityId);
        requestQuickUpdateAllElement();
    }

    private void updateAllElement() {
        ArrayList<MKOLUpdateElement> listUpdate = mOffline.getAllUpdateInfo();
        if (listUpdate != null) {
            mArrayListUpdates.clear();
            mArrayListUpdates.addAll(listUpdate);
            int updateCount = 0;
            for (MKOLUpdateElement mkolUpdateElement : listUpdate) {
                if (mkolUpdateElement == null) {
                    continue;
                }
                OfflineCityItem updateItem = mSparseArrayOfflineItem.get(mkolUpdateElement.cityID);
                if (updateItem != null) {
                    updateItem.updateStatus(mkolUpdateElement);
                }
                if (mkolUpdateElement.update) {
                    updateCount++;
                }

                if (updateCount > 0) {
                    AllOnlineApp.sHasUpdate = true;
                } else {
                    AllOnlineApp.sHasUpdate = false;
                }
            }
        } else {
            mArrayListUpdates.clear();
            AllOnlineApp.sHasUpdate = false;
        }
        if (mServiceCallback != null) {
            mServiceCallback.callback(UPDATE_CITY_LIST, null);
        }
    }

    private boolean shouldBeDeleted(OfflineCityItem item) {
        MKOLUpdateElement updateMapItem = mOffline.getUpdateInfo(item.cityId);
        if (item.cityType == OfflineCityItem.COUNTRY
            || (updateMapItem != null && TextUtils.isEmpty(CommonUtil.getStatus(this, updateMapItem.status)))) {
            return true;
        }
        return false;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(int count, String currentCity, int percent) {
        Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.status_downloading),
            System.currentTimeMillis());

        Intent notificationIntent = new Intent(this, OfflineBMapActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.download_notification);
        contentView.setTextViewText(R.id.download_notification_text,
            String.format(getString(R.string.download_status), currentCity, count));
        contentView.setTextViewText(R.id.download_notification_ratio, String.format("%d%%", percent));

        contentView.setProgressBar(R.id.download_notification_pbar, 100, percent, false);

        notification.contentView = contentView;
        notification.contentIntent = contentIntent;

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    /**
     * Show a notification while this service is running.
     */
    private void showFinishNotification() {
        Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.download_finish),
            System.currentTimeMillis());

        Intent notificationIntent = new Intent(this, OfflineBMapActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_ONE_SHOT);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.download_notification);
        contentView.setTextViewText(R.id.download_notification_text, getString(R.string.offline_map_download_finished));
        contentView.setViewVisibility(R.id.download_notification_pbar_linear, View.GONE);

        notification.contentView = contentView;
        notification.contentIntent = contentIntent;
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    public int getUndownloadedSize() {
        int size = 0;
        ArrayList<MKOLUpdateElement> listUpdate = mOffline.getAllUpdateInfo();
        for (MKOLUpdateElement element : listUpdate) {
            if (element.ratio != 100) {
                size++;
            }
        }
        return size;
    }

    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {

                requestQuickUpdateAllElement();

                MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                // 处理下载进度更新提示
                if (update != null) {
                    // Log.e("FDEBUG", String.format("%s %s: %d%% %s",
                    // update.cityName, update.cityID,
                    // update.ratio, CommonUtil.getStatus(this,update.status)));
                    showNotification(getUndownloadedSize(), update.cityName, update.ratio);
                    if (update.ratio == 100) {
                        requestUpdateAllElement();
                        mNM.cancel(NOTIFICATION);
                        if (getUndownloadedSize() < 1) {
                            showFinishNotification();
                        }
                    }
                }
            }
            break;
            case MKOfflineMap.TYPE_NEW_OFFLINE:
                // 有新离线地图安装
                break;
            case MKOfflineMap.TYPE_VER_UPDATE:
                // 版本更新提示
                // MKOLUpdateElement e = mOffline.getUpdateInfo(state);

                break;
        }
    }

    @Override
    public void onDestroy() {
        mNM.cancel(NOTIFICATION);
        if (mHandler != null) {
            mHandler.removeMessages(UPDATE_ALL_ELEMENT);
            mHandler.removeMessages(DELETE_ELEMENT);
            mHandler = null;
        }
        if (mOffline != null) {
            mOffline.destroy();
            mOffline = null;
        }
        //        if (mMapView != null) {
        //            mMapView.onDestroy();
        //            mMapView = null;
        //        }
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public OfflineMapService getService() {
            return OfflineMapService.this;
        }
    }
}