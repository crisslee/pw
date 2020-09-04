package com.coomix.app.framework.util;

import android.location.Location;
import android.location.LocationManager;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;

public class LatLonUtil {

    private static final double PI = 3.14159265;

    // @see
    // http://snipperize.todayclose.com/snippet/php/SQL-Query-to-Find-All-Retailers-Within-a-Given-Radius-of-a-Latitude-and-Longitude--65095/
    // The circumference of the earth is 24,901 miles.
    // 24,901/360 = 69.17 miles / degree
    /**
     * @param raidus
     *            ��λ�� return minLat,minLng,maxLat,maxLng
     */
    public static double[] getAround(double lat, double lon, int raidus) {

        Double latitude = lat;
        Double longitude = lon;

        Double degree = (24901 * 1609) / 360.0;
        double raidusMile = raidus;

        Double dpmLat = 1 / degree;
        Double radiusLat = dpmLat * raidusMile;
        Double minLat = latitude - radiusLat;
        Double maxLat = latitude + radiusLat;

        Double mpdLng = degree * Math.cos(latitude * (PI / 180));
        Double dpmLng = 1 / mpdLng;
        Double radiusLng = dpmLng * raidusMile;
        Double minLng = longitude - radiusLng;
        Double maxLng = longitude + radiusLng;
        // System.out.println("["+minLat+","+minLng+","+maxLat+","+maxLng+"]");
        return new double[] { minLat, minLng, maxLat, maxLng };
    }

    /**
     * 
     * @param adLat
     * @param lon
     * @param raidus
     * @return ���ط�Χ�ڵľ�γ�Ȳ�(latspan,lonspan)
     */
    public static double[] getAroundSpan(int raidus) {
        double[] result = getAround(0, 0, raidus);
        // latSpan, lonSpan
        return new double[] { Math.abs(result[2] - result[0]), Math.abs(result[3] - result[1]) };
    }

    /**
     * �������侭γ����꣨doubleֵ���������������룬��λΪ��
     * 
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     * @return
     */
    public static float getDistance(double lng1, double lat1, double lng2, double lat2) {
        float[] result = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, result);
        return result[0];
    }

    public static float getDistance(GeoPoint gp1, GeoPoint gp2) {
        float[] result = new float[1];
        Location.distanceBetween(gp1.getLatitudeE6() / 1E6, gp1.getLongitudeE6() / 1E6, gp2.getLatitudeE6() / 1E6, gp2.getLongitudeE6() / 1E6, result);
        return result[0];
    }

    /** Checks whether two providers are the same */
    public static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public static LatLng fromWgs84ToBaidu(LatLng gp) {
        CoordinateConverter converter = new CoordinateConverter();
        return converter.coord(gp).from(CoordType.GPS).convert();
    }

    public static GeoPoint fromLatLngToGeoPoint(double lat, double lng) {
        GeoPoint mGeoPoint = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
        return mGeoPoint;
    }

    public static double fromGeoToLatLng(int value) {
        return ((double) value) / 1E6;
    }

    public static boolean isSamePoint(Location point1, Location point2) {
        boolean case1 = (point1.getLatitude() == point2.getLatitude());
        boolean case2 = (point1.getLongitude() == point2.getLongitude());
        return case1 && case2;
    }

    public static Location fake(Location location) {

        double lat = location.getLatitude() + Math.random() / 100;
        double lng = location.getLongitude() + Math.random() / 100;

        Location fakeLocation = new Location(location);
        fakeLocation.setProvider(LocationManager.GPS_PROVIDER);
        fakeLocation.setLatitude(lat);
        fakeLocation.setLongitude(lng);
        return fakeLocation;
    }
}