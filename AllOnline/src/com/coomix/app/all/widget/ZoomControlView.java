package com.coomix.app.all.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.amap.api.maps.AMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.coomix.app.all.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.tencent.tencentmap.mapsdk.map.TencentMap;
import java.lang.ref.WeakReference;

public class ZoomControlView extends LinearLayout implements OnClickListener {

    private WeakReference<MapView> bdMap;//百度地图
    private WeakReference<GoogleMap> gMap;//谷歌地图
    private WeakReference<AMap> aMap; // 高德地图
    private WeakReference<TencentMap> tMap; //腾讯地图

    private ImageButton zoomInBtn, zoomOutBtn;

    public ZoomControlView(Context context) {
        this(context, null);
    }

    public ZoomControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
        zoomInBtn = new ImageButton(context);
        zoomInBtn.setBackgroundResource(R.drawable.nav_btn_zoom_in);
        zoomInBtn.setOnClickListener(this);
        zoomOutBtn = new ImageButton(context);
        zoomOutBtn.setBackgroundResource(R.drawable.nav_btn_zoom_out);
        zoomOutBtn.setOnClickListener(this);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(zoomInBtn, params);
        addView(zoomOutBtn, params);
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(LinearLayout.VERTICAL);
    }

    public void zoomTo(float zoom) {
        if (bdMap != null) {
            MapView tmpMap = bdMap.get();
            if (tmpMap != null) {
                float minLevel = tmpMap.getMap().getMinZoomLevel();
                float maxLevel = tmpMap.getMap().getMaxZoomLevel();
                if (zoom >= maxLevel) {
                    zoomInBtn.setEnabled(false);
                } else if (zoom <= minLevel) {
                    zoomOutBtn.setEnabled(false);
                } else {
                    if (!zoomInBtn.isEnabled()) {
                        zoomInBtn.setEnabled(true);
                    }
                    if (!zoomOutBtn.isEnabled()) {
                        zoomOutBtn.setEnabled(true);
                    }
                }
            }
        } else if (gMap != null) {
            GoogleMap tmpMap = gMap.get();
            if (tmpMap != null) {
                float minLevel = tmpMap.getMinZoomLevel();
                float maxLevel = tmpMap.getMaxZoomLevel();
                if (zoom >= maxLevel) {
                    zoomInBtn.setEnabled(false);
                } else if (zoom <= minLevel) {
                    zoomOutBtn.setEnabled(false);
                } else {
                    if (!zoomInBtn.isEnabled()) {
                        zoomInBtn.setEnabled(true);
                    }
                    if (!zoomOutBtn.isEnabled()) {
                        zoomOutBtn.setEnabled(true);
                    }
                }
            }
        } else if (aMap != null) {
            AMap tmpMap = aMap.get();
            if (tmpMap != null) {
                float minLevel = tmpMap.getMinZoomLevel();
                float maxLevel = tmpMap.getMaxZoomLevel();
                if (zoom >= maxLevel) {
                    zoomInBtn.setEnabled(false);
                } else if (zoom <= minLevel) {
                    zoomOutBtn.setEnabled(false);
                } else {
                    if (!zoomInBtn.isEnabled()) {
                        zoomInBtn.setEnabled(true);
                    }
                    if (!zoomOutBtn.isEnabled()) {
                        zoomOutBtn.setEnabled(true);
                    }
                }
            }
        } else if (tMap != null) {
            TencentMap tmpMap = tMap.get();
            if (tmpMap != null) {
                float minLevel = tmpMap.getMinZoomLevel();
                float maxLevel = tmpMap.getMaxZoomLevel();
                if (zoom >= maxLevel) {
                    zoomInBtn.setEnabled(false);
                } else if (zoom <= minLevel) {
                    zoomOutBtn.setEnabled(false);
                } else {
                    if (!zoomInBtn.isEnabled()) {
                        zoomInBtn.setEnabled(true);
                    }
                    if (!zoomOutBtn.isEnabled()) {
                        zoomOutBtn.setEnabled(true);
                    }
                }
            }
        }
    }

    public void zoomIn() {
        if (bdMap != null) {
            MapView tmpMap = bdMap.get();
            if (tmpMap != null) {
                float curLevel = tmpMap.getMap().getMapStatus().zoom;
                float maxLevel = tmpMap.getMap().getMaxZoomLevel();
                if (curLevel < maxLevel) {
                    curLevel++;
                    MapStatusUpdate update = MapStatusUpdateFactory.zoomIn();
                    tmpMap.getMap().animateMapStatus(update);
                    // 动画有延时
                    if (curLevel >= maxLevel) {
                        zoomInBtn.setEnabled(false);
                    } else {
                        if (!zoomOutBtn.isEnabled()) {
                            zoomOutBtn.setEnabled(true);
                        }
                    }
                }
            }
        } else if (gMap != null) {
            GoogleMap tmpMap = gMap.get();
            if (tmpMap != null) {
                tmpMap.moveCamera(CameraUpdateFactory.zoomIn());
                if (tmpMap.getCameraPosition().zoom >= tmpMap.getMaxZoomLevel()) {
                    zoomInBtn.setEnabled(false);
                } else {
                    if (!zoomOutBtn.isEnabled()) {
                        zoomOutBtn.setEnabled(true);
                    }
                }
            }
        } else if (aMap != null) {
            AMap tmpMap = aMap.get();
            if (tmpMap != null) {
                tmpMap.moveCamera(com.amap.api.maps.CameraUpdateFactory.zoomIn());
                if (tmpMap.getCameraPosition() == null || tmpMap.getCameraPosition().zoom >= tmpMap.getMaxZoomLevel()) {
                    zoomInBtn.setEnabled(false);
                } else {
                    if (!zoomOutBtn.isEnabled()) {
                        zoomOutBtn.setEnabled(true);
                    }
                }
            }
        } else if (tMap != null) {
            TencentMap tmpMap = tMap.get();
            if (tmpMap != null) {
                tmpMap.zoomIn();
                if (tmpMap.getZoomLevel() >= tmpMap.getMaxZoomLevel()) {
                    zoomInBtn.setEnabled(false);
                } else {
                    if (!zoomOutBtn.isEnabled()) {
                        zoomOutBtn.setEnabled(true);
                    }
                }
            }
        }
    }

    public void zoomOut() {
        if (bdMap != null) {
            MapView tmpMap = bdMap.get();
            if (tmpMap != null) {
                float curLevel = tmpMap.getMap().getMapStatus().zoom;
                float minLevel = tmpMap.getMap().getMinZoomLevel();
                if (curLevel > minLevel) {
                    curLevel--;
                    MapStatusUpdate update = MapStatusUpdateFactory.zoomOut();
                    tmpMap.getMap().animateMapStatus(update);

                    if (curLevel <= minLevel) {
                        zoomOutBtn.setEnabled(false);
                    } else {
                        if (!zoomInBtn.isEnabled()) {
                            zoomInBtn.setEnabled(true);
                        }
                    }
                }
            }
        } else if (gMap != null) {
            GoogleMap tmpMap = gMap.get();
            if (tmpMap != null) {
                tmpMap.moveCamera(CameraUpdateFactory.zoomOut());
                if (tmpMap.getCameraPosition().zoom <= tmpMap.getMinZoomLevel()) {
                    zoomOutBtn.setEnabled(false);
                } else {
                    if (!zoomInBtn.isEnabled()) {
                        zoomInBtn.setEnabled(true);
                    }
                }
            }
        } else if (aMap != null) {
            AMap tmpMap = aMap.get();
            if (tmpMap != null) {
                tmpMap.moveCamera(com.amap.api.maps.CameraUpdateFactory.zoomOut());
                if (tmpMap.getCameraPosition().zoom <= tmpMap.getMinZoomLevel()) {
                    zoomOutBtn.setEnabled(false);
                } else {
                    if (!zoomInBtn.isEnabled()) {
                        zoomInBtn.setEnabled(true);
                    }
                }
            }
        } else if (tMap != null) {
            TencentMap tmpMap = tMap.get();
            if (tmpMap != null) {
                tmpMap.zoomOut();
                if (tmpMap.getZoomLevel() <= tmpMap.getMinZoomLevel()) {
                    zoomOutBtn.setEnabled(false);
                } else {
                    if (!zoomInBtn.isEnabled()) {
                        zoomInBtn.setEnabled(true);
                    }
                }
            }
        }
    }

    public void setMap(Object map) {
        if (map instanceof MapView) {
            bdMap = new WeakReference<MapView>((MapView) map);
        } else if (map instanceof GoogleMap) {
            gMap = new WeakReference<GoogleMap>((GoogleMap) map);
        } else if (map instanceof AMap) {
            // 高德地图
            aMap = new WeakReference<AMap>((AMap) map);
        } else if (map instanceof TencentMap) {
            // 腾讯地图
            tMap = new WeakReference<TencentMap>((TencentMap) map);
        } else {
            throw new IllegalArgumentException(
                "map must be an instance of com.baidu.mapapi.map.MapView or com.google.android.gms.maps.GoogleMap");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == zoomOutBtn) {
            zoomOut();
        } else if (v == zoomInBtn) {
            zoomIn();
        }
    }

    public void releaseMap() {
        if (bdMap != null) {
            bdMap.clear();
            bdMap = null;
        }
        if (gMap != null) {
            gMap.clear();
            gMap = null;
        }

        if (aMap != null) {
            aMap.clear();
            aMap = null;
        }

        if (tMap != null) {
            tMap.clear();
            tMap = null;
        }
    }
}
