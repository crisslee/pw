package com.coomix.app.all.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.amap.api.maps.AMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Projection;
import com.coomix.app.all.R;
import com.coomix.app.framework.util.LatLonUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;

public class ScaleView extends View {
    private Paint mPaint;
    private WeakReference<MapView> bdMap;
    private WeakReference<GoogleMap> gMap;

    private WeakReference<AMap> aMap; // 高德地图

    private Rect mRect;
    private int minWidth;
    private int minHeight;
    private Bitmap scaleBmp;
    private int padding = 5;

    private int currentScaleIndex = -1;
    private int pixelsScaleView = -1;

    public static final int[] SCALE = { 5000000, 2000000, 2000000, 2000000, 1000000, 500000, 200000, 100000, 50000, 20000, 10000, 5000, 2000, 1000, 500, 200, 100, 50, 20 };

    private static final String[] SCALE_DESCS = { "5000km", "2000km", "2000km", "2000km", "1000km", "500km", "200km", "100km", "50km", "20km", "10km", "5km", "2km", "1km", "500m", "200m", "100m", "50m", "20m" };

    public ScaleView(Context context) {
        this(context, null);
    }

    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setFilterBitmap(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(19);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);

        scaleBmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_scale);

        mRect = new Rect();
        mPaint.getTextBounds(SCALE_DESCS[0], 0, SCALE_DESCS[0].length(), mRect);
        mPaint.setTextAlign(Align.LEFT);
        minWidth = mRect.width() + padding * 2 + 100;
        minHeight = mRect.height() + scaleBmp.getHeight() + padding * 3;
    }

    public interface OnMapLoadFinishListener {
        void onMapLoadFinish();
    }

    public void setMap(Object map) {
        if (map instanceof MapView) {
            bdMap = new WeakReference<MapView>((MapView) map);
        } else if (map instanceof GoogleMap) {
            gMap = new WeakReference<GoogleMap>((GoogleMap) map);
        } else if (map instanceof AMap) {
            aMap = new WeakReference<AMap>((AMap) map);
        } else {
            throw new IllegalArgumentException("map must be an instance of com.baidu.mapapi.map.MapView or com.google.android.gms.maps.GoogleMap");
        }
    }

    /**
     * call this after map load finish
     */
    public void refresh() {
        if (bdMap != null) {
            MapView mapview = bdMap.get();
            if (mapview != null) {
                int currentLevel = (int) mapview.getMap().getMapStatus().zoom;
                if (lastRefreshLevel == currentLevel) {
                    return;
                }
                lastRefreshLevel = currentLevel;
                currentScaleIndex = currentLevel;
                if (currentScaleIndex <= 0) {
                    currentScaleIndex = 0;
                } else if (currentScaleIndex >= SCALE.length) {
                    currentScaleIndex = SCALE.length - 1;
                }
                pixelsScaleView = metersToPixelsBaidu(mapview, SCALE[(int) currentScaleIndex]);
            }
        } else if (gMap != null) {
            GoogleMap map = gMap.get();
            if (map != null) {
                int currentLevel = (int) map.getCameraPosition().zoom;
                if (lastRefreshLevel == currentLevel) {
                    return;
                }
                lastRefreshLevel = currentLevel;
                currentScaleIndex = currentLevel;
                if (currentScaleIndex <= 0) {
                    currentScaleIndex = 0;
                } else if (currentScaleIndex >= SCALE.length) {
                    currentScaleIndex = SCALE.length - 1;
                }
                pixelsScaleView = metersToPixelsGoogle(map, SCALE[(int) currentScaleIndex]);
            }
        } else if (aMap != null) {
            AMap tempMap = aMap.get();
            if (tempMap != null) {
                int currentLevel = (int) tempMap.getCameraPosition().zoom;
                if (lastRefreshLevel == currentLevel) {
                    return;
                }
                lastRefreshLevel = currentLevel;
                currentScaleIndex = currentLevel;
                if (currentScaleIndex <= 0) {
                    currentScaleIndex = 0;
                } else if (currentScaleIndex >= SCALE.length) {
                    currentScaleIndex = SCALE.length - 1;
                }
                pixelsScaleView = metersToPixelsAMap(tempMap, SCALE[(int) currentScaleIndex]);
            }
        }
        requestLayout();
        // invalidate();//////////////////////////后期修改的 修改日期为：2014-7-9 被注释掉
    }

    private float lastRefreshLevel;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        if (currentScaleIndex == -1) {
            return;
        }
        draw9Patch(canvas, (int) pixelsScaleView);
        drawText(canvas, SCALE_DESCS[(int) currentScaleIndex]);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            width = widthSize;
        } else {
            width = pixelsScaleView + padding * 2;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            height = heightSize;
        } else {
            height = minHeight;
        }

        setMeasuredDimension(width, height);
    }

    private int metersToPixelsGoogle(GoogleMap map, int meters) {
        com.google.android.gms.maps.Projection projection = map.getProjection();
        LatLng centerLatLng = projection.getVisibleRegion().latLngBounds.getCenter();
        Point center = projection.toScreenLocation(centerLatLng);
        Point center200 = new Point(center.x + 200, center.y);
        LatLng centerLatLng200 = projection.fromScreenLocation(center200);
        return (int) (meters * 200 / LatLonUtil.getDistance(centerLatLng.longitude, centerLatLng.latitude, centerLatLng200.longitude, centerLatLng200.latitude));
    }

    private int metersToPixelsAMap(AMap map, int meters) {
        com.amap.api.maps.Projection projection = map.getProjection();
        com.amap.api.maps.model.LatLng centerLatLng = projection.getVisibleRegion().nearLeft;
        Point center = projection.toScreenLocation(centerLatLng);

        Point center200 = new Point(center.x + 200, center.y);
        com.amap.api.maps.model.LatLng centerLatLng200 = projection.fromScreenLocation(center200);

        return (int) (meters * 200 / LatLonUtil.getDistance(centerLatLng.longitude, centerLatLng.latitude, centerLatLng200.longitude, centerLatLng200.latitude));
    }

    private int metersToPixelsBaidu(MapView mapview, int meters) {
        Projection projection = mapview.getMap().getProjection();
        return (int) projection.metersToEquatorPixels(meters);
    }

    private void draw9Patch(Canvas canvas, int pixels) {
        NinePatch patch = new NinePatch(scaleBmp, scaleBmp.getNinePatchChunk(), null);
        int width = getWidth();
        int height = getHeight();
        mRect.top = height - padding - patch.getHeight();
        mRect.left = width - pixels - padding;
        mRect.right = width - padding;
        mRect.bottom = mRect.top + patch.getHeight();
        patch.draw(canvas, mRect);
    }

    private void drawText(Canvas canvas, String text) {
        float textWidth = mPaint.measureText(text);
        canvas.drawText(text, getWidth() - textWidth - padding, getHeight() - padding - scaleBmp.getHeight(), mPaint);
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
    }
}
