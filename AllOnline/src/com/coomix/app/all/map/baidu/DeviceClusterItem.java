package com.coomix.app.all.map.baidu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.model.LatLng;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceInfo;

/**
 * 每个Marker点，包含Marker点坐标以及图标
 */
public class DeviceClusterItem implements ClusterItem {
    private final DeviceInfo mDevice;
    private Context mContext;

    public DeviceClusterItem(Context context, DeviceInfo device) {
        mDevice = device;
        mContext = context;
    }

    @Override
    public LatLng getPosition() {
        if (mDevice != null) {
            return new LatLng(mDevice.getLat(), mDevice.getLng());
        }
        return null;
    }

    @Override
    public BitmapDescriptor getBitmapDescriptor() {
        if (mDevice != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.nothing);
            if (mDevice.getState() == DeviceInfo.STATE_RUNNING) {
                bitmapDrawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.dev_green);
            } else if (mDevice.getState() == DeviceInfo.STATE_STOP) {
                bitmapDrawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.dev_blue);
            } else if (mDevice.getState() == DeviceInfo.STATE_OFFLINE) {
                bitmapDrawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.dev_gray);
            }
            
            Bitmap bitmap = getDrawable(bitmapDrawable, mDevice.getCourse());
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }
        return null;
    }
    
    public static Bitmap getDrawable(Drawable source, float degree) {
        if (!(source instanceof BitmapDrawable)) {
            throw new IllegalArgumentException("source drawable must be an instance of BitmapDrawable");
        }
        int width = source.getIntrinsicHeight();
        int height = source.getIntrinsicHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap copy = ((BitmapDrawable) source).getBitmap().copy(Config.ARGB_8888, false);
        Bitmap rotatedBitmap = Bitmap.createBitmap(copy, 0, 0, width, height, matrix, true);
        if (rotatedBitmap != copy) {
            copy.recycle();
        }
        return rotatedBitmap;
    }
}