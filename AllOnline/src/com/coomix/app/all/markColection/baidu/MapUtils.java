package com.coomix.app.all.markColection.baidu;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.View;
import android.view.View.MeasureSpec;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;

public class MapUtils {
    static double DEF_PI = 3.14159265359; // PI
    static double DEF_2PI = 6.28318530712; // 2*PI
    static double DEF_PI180 = 0.01745329252; // PI/180.0
    static double DEF_R = 6370693.5; // radius of earth

    public static MBound getExtendedBounds(MapView map, MBound bound, Integer gridSize) {
        BaiduMap baiduMap = map.getMap();

        MBound tbounds = cutBoundsInRange(bound);

        Projection projection = baiduMap.getProjection();
        Point pixelNE = projection.toScreenLocation(new LatLng(tbounds.getRightTop().getLatitudeE6() / 1E6, tbounds.getRightTop().getLongitudeE6() / 1E6));
        Point pixelSW = projection.toScreenLocation(new LatLng(tbounds.getLeftBottom().getLatitudeE6() / 1E6, tbounds.getLeftBottom().getLongitudeE6() / 1E6));
        pixelNE.x += gridSize;
        pixelNE.y -= gridSize;
        pixelSW.x -= gridSize;
        pixelSW.y += gridSize;

        GeoPoint rightTop = new GeoPoint(projection.fromScreenLocation(pixelNE).latitudeE6, projection.fromScreenLocation(pixelNE).longitudeE6);
        GeoPoint leftBottom = new GeoPoint(projection.fromScreenLocation(pixelSW).latitudeE6, projection.fromScreenLocation(pixelSW).longitudeE6);

        return new MBound((int) rightTop.getLatitudeE6(), (int) rightTop.getLongitudeE6(), (int) leftBottom.getLatitudeE6(), (int) leftBottom.getLongitudeE6());
    }

    public static MBound cutBoundsInRange(MBound bounds) {
        int maxX = getRange(bounds.getRightTopLat(), -74000000, 74000000);
        int minX = getRange(bounds.getRightTopLat(), -74000000, 74000000);
        int maxY = getRange(bounds.getRightTopLng(), -180000000, 180000000);
        int minY = getRange(bounds.getLeftBottomLng(), -180000000, 180000000);
        return new MBound(minX, minY, maxX, maxY);
    }

    public static int getRange(int i, int mix, int max) {
        i = Math.max(i, mix);
        i = Math.min(i, max);
        return i;
    }

    public static double GetShortDistance(double lon1, double lat1, double lon2, double lat2) {
        double ew1, ns1, ew2, ns2;
        double dx, dy, dew;
        double distance;

        ew1 = lon1 * DEF_PI180;
        ns1 = lat1 * DEF_PI180;
        ew2 = lon2 * DEF_PI180;
        ns2 = lat2 * DEF_PI180;

        dew = ew1 - ew2;

        if (dew > DEF_PI)
            dew = DEF_2PI - dew;
        else if (dew < -DEF_PI)
            dew = DEF_2PI + dew;
        dx = DEF_R * Math.cos(ns1) * dew;
        dy = DEF_R * (ns1 - ns2);

        distance = Math.sqrt(dx * dx + dy * dy);
        return distance;
    }

    public static double GetLongDistance(double lon1, double lat1, double lon2, double lat2) {
        double ew1, ns1, ew2, ns2;
        double distance;

        ew1 = lon1 * DEF_PI180;
        ns1 = lat1 * DEF_PI180;
        ew2 = lon2 * DEF_PI180;
        ns2 = lat2 * DEF_PI180;

        distance = Math.sin(ns1) * Math.sin(ns2) + Math.cos(ns1) * Math.cos(ns2) * Math.cos(ew1 - ew2);

        if (distance > 1.0)
            distance = 1.0;
        else if (distance < -1.0)
            distance = -1.0;

        distance = DEF_R * Math.acos(distance);
        return distance;
    }

    public static Bitmap convertViewToBitmap(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

}
