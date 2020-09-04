package com.coomix.app.all.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.view.View;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;

/**
 * Created by think on 2017/2/16.
 */
public class PermissionUtil {

    public static boolean checkLocationPermission(Context ctx) {
        if(Build.VERSION.SDK_INT >= 23) {
            return PermissionChecker.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || PermissionChecker.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static boolean hasPhonePermission(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private static boolean requestLocationPermission(Activity activity) {/*Object activityOrFragment) {*/
        if(Build.VERSION.SDK_INT >= 23) {
            String[] mPermissionList = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions((Activity) activity, mPermissionList, 123);
            /*if(activityOrFragment instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) activityOrFragment, mPermissionList, 123);
            } else if(activityOrFragment instanceof Fragment) {
                ((Fragment) activityOrFragment).requestPermissions(mPermissionList, 123);
            }*/
            return true;
        }
        return false;
    }

    public static void showLocationHint(Activity activity) {
        if(Build.VERSION.SDK_INT >= 23) {
            boolean hasLocationPermission = PermissionUtil.checkLocationPermission(activity);
            if (!hasLocationPermission) {
                // 针对android M（6.0  23）及以上用户
                PermissionUtil.requestLocationPermission(activity);
            }
        } else if(AllOnlineApp.getCurrentLocation() == null ||
                AllOnlineApp.getCurrentLocation().getLongitude() == 0 ||
                AllOnlineApp.getCurrentLocation().getLatitude() == 0) {
            // 定位失败
            toLocationSettings(activity);
        }
    }

    public static void showLocationHint(Fragment fragment) {
        if(!fragment.isAdded() || fragment.isDetached()) {
            return;
        }
        Activity activity = fragment.getActivity();
        if(Build.VERSION.SDK_INT >= 23) {
            boolean hasLocationPermission = PermissionUtil.checkLocationPermission(activity);
            if (!hasLocationPermission) {
                // 针对android M（6.0  23）及以上用户，定位权限被禁止
                toLocationSettings(activity);
                // PermissionUtil.requestLocationPermission(fragment); 此方法在frament中可能会弹不出提示框（s7 edge 6.0.1）
            }
        } else if(AllOnlineApp.getCurrentLocation() == null ||
                AllOnlineApp.getCurrentLocation().getLongitude() == 0 ||
                AllOnlineApp.getCurrentLocation().getLatitude() == 0) {
            toLocationSettings(activity);
        }
    }

    public static void toLocationSettings(final Activity activity) {
        final DialogUtil dialogUtil = new DialogUtil(activity, activity.getString(R.string.location_permission_hint));
        dialogUtil.setClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialogUtil != null) {
                            dialogUtil.dismiss();
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                        activity.startActivity(intent);
                        if (dialogUtil != null) {
                            dialogUtil.dismiss();
                        }
                    }
                });
        dialogUtil.show();
    }

    public static void goIntentSetting(Context ctx) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", ctx.getPackageName(), null);
        intent.setData(uri);
        try {
            ctx.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
