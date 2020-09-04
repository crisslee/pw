package com.coomix.app.all.manager;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.MainThread;
import android.text.TextUtils;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.bumptech.GlideApp;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.TrackPoint;

/**
 * Created by think on 2018/10/8.
 */
public class MapIconManager {

    private static MapIconManager instance;

    private com.baidu.mapapi.map.BitmapDescriptor bdRunningIcon, bdStopIcon, bdOfflineIcon, bdWarnningIcon, bdOverspeedIcon, bdOutDateIcon;
    private com.amap.api.maps.model.BitmapDescriptor amapRunningIcon, amapStopIcon, amapOfflineIcon, amapWarnningIcon, amapOverspeedIcon, amapOutDateIcon;
    private com.tencent.mapsdk.raster.model.BitmapDescriptor tmapRunningIcon, tmapStopIcon, tmapOfflineIcon, tmapWarnningIcon, tmapOverspeedIcon, tmapOutDateIcon;

    private MapIconManager() {
        initIcons();
    }

    public synchronized static MapIconManager getInstance() {
        if (instance == null) {
            instance = new MapIconManager();
        }

        return instance;
    }

    @MainThread
    public void initIcons() {
        switch (SettingDataManager.getInstance(AllOnlineApp.mApp).getMapTypeInt()) {
            case Constant.MAP_TYPE_AMAP:
                // 在线
                amapRunningIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_green);
                // 静止
                amapStopIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_blue);
                // 离线
                amapOfflineIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_gray);
                // 时速80km-120km
                amapWarnningIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_yellow);
                // 时速超过120km
                amapOverspeedIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_red);
                //过期设备
                amapOutDateIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_purple);
                break;

            case Constant.MAP_TYPE_TENCENT:
                // 在线
                tmapRunningIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_green);
                // 静止
                tmapStopIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_blue);
                // 离线
                tmapOfflineIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_gray);
                // 时速80km-120km
                tmapWarnningIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_yellow);
                // 时速超过120km
                tmapOverspeedIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_red);
                //过期
                tmapOutDateIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromResource(R.drawable.dev_purple);
                break;

            case Constant.MAP_TYPE_BAIDU:
            default:
                // 在线
                bdRunningIcon = BitmapDescriptorFactory.fromResource(R.drawable.dev_green);
                // 静止
                bdStopIcon = BitmapDescriptorFactory.fromResource(R.drawable.dev_blue);
                // 离线
                bdOfflineIcon = BitmapDescriptorFactory.fromResource(R.drawable.dev_gray);
                //时速80km-120km
                bdWarnningIcon = BitmapDescriptorFactory.fromResource(R.drawable.dev_yellow);
                //时速超过120km
                bdOverspeedIcon = BitmapDescriptorFactory.fromResource(R.drawable.dev_red);
                //过期设备
                bdOutDateIcon = BitmapDescriptorFactory.fromResource(R.drawable.dev_purple);
                break;
        }

        //运行
        String moving = ThemeManager.getInstance().getThemeAll().getLocIcon().moving;
        if (!TextUtils.isEmpty(moving)) {
            GlideApp.with(AllOnlineApp.mApp).load(moving).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (resource == null) {
                        return;
                    }
                    BitmapDrawable bd = (BitmapDrawable) resource;
                    if (bd == null || bd.getBitmap() == null) {
                        return;
                    }
                    switch (SettingDataManager.getInstance(AllOnlineApp.mApp).getMapTypeInt()) {
                        case Constant.MAP_TYPE_AMAP:
                            amapRunningIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_TENCENT:
                            tmapRunningIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_BAIDU:
                        default:
                            bdRunningIcon = BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;
                    }
                }
            });
        }

        //静止
        String stop = ThemeManager.getInstance().getThemeAll().getLocIcon().still;
        if (!TextUtils.isEmpty(stop)) {
            GlideApp.with(AllOnlineApp.mApp).load(stop).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (resource == null) {
                        return;
                    }
                    BitmapDrawable bd = (BitmapDrawable) resource;
                    if (bd == null || bd.getBitmap() == null) {
                        return;
                    }
                    switch (SettingDataManager.getInstance(AllOnlineApp.mApp).getMapTypeInt()) {
                        case Constant.MAP_TYPE_AMAP:
                            amapStopIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_TENCENT:
                            tmapStopIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_BAIDU:
                        default:
                            bdStopIcon = BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;
                    }
                }
            });
        }

        //时速80km-120km
        String fast = ThemeManager.getInstance().getThemeAll().getLocIcon().fast;
        if (!TextUtils.isEmpty(fast)) {
            GlideApp.with(AllOnlineApp.mApp).load(fast).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (resource == null) {
                        return;
                    }
                    BitmapDrawable bd = (BitmapDrawable) resource;
                    if (bd == null || bd.getBitmap() == null) {
                        return;
                    }
                    switch (SettingDataManager.getInstance(AllOnlineApp.mApp).getMapTypeInt()) {
                        case Constant.MAP_TYPE_AMAP:
                            amapWarnningIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_TENCENT:
                            tmapWarnningIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_BAIDU:
                        default:
                            bdWarnningIcon = BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;
                    }
                }
            });
        }

        //时速超过120km
        String overSpeed = ThemeManager.getInstance().getThemeAll().getLocIcon().overSpeed;
        if (!TextUtils.isEmpty(overSpeed)) {
            GlideApp.with(AllOnlineApp.mApp).load(overSpeed).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (resource == null) {
                        return;
                    }
                    BitmapDrawable bd = (BitmapDrawable) resource;
                    if (bd == null || bd.getBitmap() == null) {
                        return;
                    }
                    switch (SettingDataManager.getInstance(AllOnlineApp.mApp).getMapTypeInt()) {
                        case Constant.MAP_TYPE_AMAP:
                            amapOverspeedIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_TENCENT:
                            tmapOverspeedIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_BAIDU:
                        default:
                            bdOverspeedIcon = BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;
                    }
                }
            });
        }

        //离线
        String offline = ThemeManager.getInstance().getThemeAll().getLocIcon().offline;
        if (!TextUtils.isEmpty(offline)) {
            GlideApp.with(AllOnlineApp.mApp).load(offline).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (resource == null) {
                        return;
                    }
                    BitmapDrawable bd = (BitmapDrawable) resource;
                    if (bd == null || bd.getBitmap() == null) {
                        return;
                    }
                    switch (SettingDataManager.getInstance(AllOnlineApp.mApp).getMapTypeInt()) {
                        case Constant.MAP_TYPE_AMAP:
                            amapOfflineIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_TENCENT:
                            tmapOfflineIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_BAIDU:
                        default:
                            bdOfflineIcon = BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;
                    }
                }
            });
        }

        //过期
        String expired = ThemeManager.getInstance().getThemeAll().getLocIcon().expired;
        if (!TextUtils.isEmpty(expired)) {
            GlideApp.with(AllOnlineApp.mApp).load(expired).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (resource == null) {
                        return;
                    }
                    BitmapDrawable bd = (BitmapDrawable) resource;
                    if (bd == null || bd.getBitmap() == null) {
                        return;
                    }
                    switch (SettingDataManager.getInstance(AllOnlineApp.mApp).getMapTypeInt()) {
                        case Constant.MAP_TYPE_AMAP:
                            amapOutDateIcon = com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_TENCENT:
                            tmapOutDateIcon = com.tencent.mapsdk.raster.model.BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;

                        case Constant.MAP_TYPE_BAIDU:
                        default:
                            bdOutDateIcon = BitmapDescriptorFactory.fromBitmap(bd.getBitmap());
                            break;
                    }
                }
            });
        }
    }

    public BitmapDescriptor getBaiduIcon(DeviceInfo device) {
        if (device == null) {
            return null;
        }
        BitmapDescriptor icon = null;
        if (device.getState() == DeviceInfo.STATE_RUNNING) {
            if (device.getSpeedStatus() == DeviceInfo.SPEED_OVER80) {
                if(bdWarnningIcon == null){
                    initIcons();
                }
                icon = bdWarnningIcon;
            } else if (device.getSpeedStatus() == DeviceInfo.SPEED_OVER120) {
                if(bdOverspeedIcon == null){
                    initIcons();
                }
                icon = bdOverspeedIcon;
            } else {
                if(bdRunningIcon == null){
                    initIcons();
                }
                icon = bdRunningIcon;
            }
        } else if (device.getState() == DeviceInfo.STATE_STOP) {
            if(bdStopIcon == null){
                initIcons();
            }
            icon = bdStopIcon;
        } else if (device.getState() == DeviceInfo.STATE_OFFLINE) {
            if(bdOfflineIcon == null){
                initIcons();
            }
            icon = bdOfflineIcon;
        } else if (device.getState() == DeviceInfo.STATE_EXPIRE) {
            if(bdOutDateIcon == null){
                initIcons();
            }
            icon = bdOutDateIcon;
        }
        return icon;
    }

    public BitmapDescriptor getBaiduHistoryIcon(TrackPoint curPoint) {
        if (curPoint == null) {
            return null;
        }
        BitmapDescriptor icon = null;
        int speedStatus = DeviceInfo.getSpeedStatus(curPoint.speed);
        if (speedStatus == DeviceInfo.SPEED_OVER80) {
            if(bdWarnningIcon == null){
                initIcons();
            }
            icon = bdWarnningIcon;
        } else if (speedStatus == DeviceInfo.SPEED_OVER120) {
            if(bdOverspeedIcon == null){
                initIcons();
            }
            icon = bdOverspeedIcon;
        } else if (speedStatus == DeviceInfo.SPEED_NORMAL) {
            if(bdRunningIcon == null){
                initIcons();
            }
            icon = bdRunningIcon;
        } else {
            if(bdStopIcon == null){
                initIcons();
            }
            icon = bdStopIcon;
        }
        return icon;
    }

    public BitmapDescriptor getBaiduStopIcon() {
        if(bdStopIcon == null){
            initIcons();
        }
        return bdStopIcon;
    }

    /*********************************************************************/
    public com.amap.api.maps.model.BitmapDescriptor getAmapIcon(DeviceInfo device) {
        if (device == null) {
            return null;
        }
        com.amap.api.maps.model.BitmapDescriptor icon = null;
        if (device.getState() == DeviceInfo.STATE_RUNNING) {
            if (device.getSpeedStatus() == DeviceInfo.SPEED_OVER80) {
                if(amapWarnningIcon == null){
                    initIcons();
                }
                icon = amapWarnningIcon;
            } else if (device.getSpeedStatus() == DeviceInfo.SPEED_OVER120) {
                if(amapOverspeedIcon == null){
                    initIcons();
                }
                icon = amapOverspeedIcon;
            } else {
                if(amapRunningIcon == null){
                    initIcons();
                }
                icon = amapRunningIcon;
            }
        } else if (device.getState() == DeviceInfo.STATE_STOP) {
            if(amapStopIcon == null){
                initIcons();
            }
            icon = amapStopIcon;
        } else if (device.getState() == DeviceInfo.STATE_OFFLINE) {
            if(amapOfflineIcon == null){
                initIcons();
            }
            icon = amapOfflineIcon;
        } else if (device.getState() == DeviceInfo.STATE_EXPIRE) {
            if(amapOutDateIcon == null){
                initIcons();
            }
            icon = amapOutDateIcon;
        }
        return icon;
    }

    public com.amap.api.maps.model.BitmapDescriptor getAmapHistoryIcon(TrackPoint curPoint) {
        if (curPoint == null) {
            return null;
        }
        com.amap.api.maps.model.BitmapDescriptor icon = null;
        int speedStatus = DeviceInfo.getSpeedStatus(curPoint.speed);
        if (speedStatus == DeviceInfo.SPEED_OVER80) {
            if(amapWarnningIcon == null){
                initIcons();
            }
            icon = amapWarnningIcon;
        } else if (speedStatus == DeviceInfo.SPEED_OVER120) {
            if(amapOverspeedIcon == null){
                initIcons();
            }
            icon = amapOverspeedIcon;
        } else if (speedStatus == DeviceInfo.SPEED_NORMAL) {
            if(amapRunningIcon == null){
                initIcons();
            }
            icon = amapRunningIcon;
        } else {
            if(amapStopIcon == null){
                initIcons();
            }
            icon = amapStopIcon;
        }
        return icon;
    }

    public com.amap.api.maps.model.BitmapDescriptor getAmapStopIcon() {
        if(amapStopIcon == null){
            initIcons();
        }
        return amapStopIcon;
    }

    /*********************************************************************/
    public com.tencent.mapsdk.raster.model.BitmapDescriptor getTmapIcon(DeviceInfo device) {
        if (device == null) {
            return null;
        }
        com.tencent.mapsdk.raster.model.BitmapDescriptor icon = null;
        if (device.getState() == DeviceInfo.STATE_RUNNING) {
            if (device.getSpeedStatus() == DeviceInfo.SPEED_OVER80) {
                if(tmapWarnningIcon == null){
                    initIcons();
                }
                icon = tmapWarnningIcon;
            } else if (device.getSpeedStatus() == DeviceInfo.SPEED_OVER120) {
                if(tmapOverspeedIcon == null){
                    initIcons();
                }
                icon = tmapOverspeedIcon;
            } else {
                if(tmapRunningIcon == null){
                    initIcons();
                }
                icon = tmapRunningIcon;
            }
        } else if (device.getState() == DeviceInfo.STATE_STOP) {
            if(tmapStopIcon == null){
                initIcons();
            }
            icon = tmapStopIcon;
        } else if (device.getState() == DeviceInfo.STATE_OFFLINE) {
            if(tmapOfflineIcon == null){
                initIcons();
            }
            icon = tmapOfflineIcon;
        } else if (device.getState() == DeviceInfo.STATE_EXPIRE) {
            if(tmapOutDateIcon == null){
                initIcons();
            }
            icon = tmapOutDateIcon;
        }
        return icon;
    }

    public com.tencent.mapsdk.raster.model.BitmapDescriptor getTmapHistoryIcon(TrackPoint curPoint) {
        if (curPoint == null) {
            return null;
        }
        com.tencent.mapsdk.raster.model.BitmapDescriptor icon = null;
        int speedStatus = DeviceInfo.getSpeedStatus(curPoint.speed);
        if (speedStatus == DeviceInfo.SPEED_OVER80) {
            if(tmapWarnningIcon == null){
                initIcons();
            }
            icon = tmapWarnningIcon;
        } else if (speedStatus == DeviceInfo.SPEED_OVER120) {
            if(tmapOverspeedIcon == null){
                initIcons();
            }
            icon = tmapOverspeedIcon;
        } else if (speedStatus == DeviceInfo.SPEED_NORMAL) {
            if(tmapRunningIcon == null){
                initIcons();
            }
            icon = tmapRunningIcon;
        } else {
            if(tmapStopIcon == null){
                initIcons();
            }
            icon = tmapStopIcon;
        }
        return icon;
    }

    public com.tencent.mapsdk.raster.model.BitmapDescriptor getTmapStopIcon() {
        if(tmapStopIcon == null){
            initIcons();
        }
        return tmapStopIcon;
    }

    public void release() {
        if (bdRunningIcon != null) {
            bdRunningIcon.recycle();
            bdRunningIcon = null;
        }
        if (bdStopIcon != null) {
            bdStopIcon.recycle();
            bdStopIcon = null;
        }
        if (bdOfflineIcon != null) {
            bdOfflineIcon.recycle();
            bdOfflineIcon = null;
        }
        if (bdWarnningIcon != null) {
            bdWarnningIcon.recycle();
            bdWarnningIcon = null;
        }
        if (bdOverspeedIcon != null) {
            bdOverspeedIcon.recycle();
            bdOverspeedIcon = null;
        }
        if (bdOutDateIcon != null) {
            bdOutDateIcon.recycle();
            bdOutDateIcon = null;
        }
        /////////////////////
        if (amapRunningIcon != null) {
            amapRunningIcon.recycle();
            amapRunningIcon = null;
        }
        if (amapStopIcon != null) {
            amapStopIcon.recycle();
            amapStopIcon = null;
        }
        if (amapOfflineIcon != null) {
            amapOfflineIcon.recycle();
            amapOfflineIcon = null;
        }
        if (amapWarnningIcon != null) {
            amapWarnningIcon.recycle();
            amapWarnningIcon = null;
        }
        if (amapOverspeedIcon != null) {
            amapOverspeedIcon.recycle();
            amapOverspeedIcon = null;
        }
        if (amapOutDateIcon != null) {
            amapOutDateIcon.recycle();
            amapOutDateIcon = null;
        }
        /////////////////////
        if (tmapRunningIcon != null) {
            tmapRunningIcon = null;
        }
        if (tmapStopIcon != null) {
            tmapStopIcon = null;
        }
        if (tmapOfflineIcon != null) {
            tmapOfflineIcon = null;
        }
        if (tmapWarnningIcon != null) {
            tmapWarnningIcon = null;
        }
        if (tmapOverspeedIcon != null) {
            tmapOverspeedIcon = null;
        }
        if (tmapOutDateIcon != null) {
            tmapOutDateIcon = null;
        }
    }
}
