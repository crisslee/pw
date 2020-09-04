package com.coomix.app.all.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.framework.util.PreferenceUtil;
import com.goome.gpns.GPNSInterface;
import com.goome.gpns.noti.GPNSNotificationBuilder;
import com.tencent.bugly.crashreport.CrashReport;
import java.io.File;

public class GpnsUtil {
    public static void initGpns(Context context, String baseurl) {
        // 开启Gpns推送
        int iconId = R.drawable.ic_launcher;
        //if (android.os.Build.VERSION.SDK_INT >= 21) {
        //    // 5.0以上版本要求仅支持alpha通道，白色
        //    iconId = R.drawable.notice_icon;
        //}
        Uri audioUri = getAudioUri(context);
        if (audioUri == null) {
            Log.d("gpns", "audioUri=null");
        } else {
            Log.d("gpns", "audioUri=" + audioUri.toString());
        }
        GPNSNotificationBuilder notiBuilder = new GPNSNotificationBuilder(context.getString(R.string.app_name), iconId,
            audioUri);
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                PackageManager.GET_META_DATA);
            int appId = appInfo.metaData.getInt("GPNS_APP_ID");
            if (Constant.IS_DEBUG_MODE) {
                Log.d("gpns", "initGPNS url------" + baseurl);
            }
            GPNSInterface.initGPNS(context.getApplicationContext(), String.valueOf(appId),
                context.getPackageName() + ":GpnsService", notiBuilder, baseurl);
        } catch (Exception e) {
            if (Constant.IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            CrashReport.postCatchedException(e);
        }
    }

    public static void setGpnsNotiSound(Context context, int soundType, int position) {
        Log.d("gpns", "soundType=" + soundType + "position=" + position);

        Uri audioUri = null;
        switch (soundType) {
            case 0:
                if (position != 0) {
                    /* 判断位置不为0则播放的条目为position-1 */
                    RingtoneManager rm = new RingtoneManager(context);
                    rm.setType(RingtoneManager.TYPE_NOTIFICATION);
                    rm.getCursor();
                    audioUri = rm.getRingtoneUri(position - 1);
                } else {
                    /* position为0是跟随系统，先得到系统所使用的铃声，然后播放 */
                    audioUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                }
                break;
            case 1:
                if (position != 0) {
                    /* 判断位置不为0则播放的条目为position-1 */
                    RingtoneManager rm = new RingtoneManager(context);
                    rm.setType(RingtoneManager.TYPE_ALARM);
                    rm.getCursor();
                    audioUri = rm.getRingtoneUri(position - 1);
                } else {
                    /* position为0是跟随系统，先得到系统所使用的铃声，然后播放 */
                    audioUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
                }
                break;
            case 2:
                String soundFile = PreferenceUtil.getString(Constant.PREFERENCE_CHOOSE_SOUND, "0");
                Uri mUri =
                    Uri.parse(Environment.getExternalStorageDirectory().getPath() + File.separator + "CarOnlineSound"
                        + File.separator + soundFile);
                audioUri = mUri;
                break;
            default:
                audioUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                break;
        }
        if (audioUri == null) {
            Log.d("gpns", "audioUri=null");
        } else {
            Log.d("gpns", "audioUri=" + audioUri.toString());
        }

        GPNSInterface.setNotificationSound(context, audioUri);
    }

    /**
     * 获取提示音uri
     */
    public static Uri getAudioUri(Context context) {
        int notificationSoundType = PreferenceUtil.getInt(Constant.PREFERENCE_NOTIFICATION_TYPE, 0);
        if (notificationSoundType == 0) {
            int position = PreferenceUtil.getInt(Constant.PREFERENCE_CHOOSE_AUDIO, 0);
            if (position != 0) {
                /* 判断位置不为0则播放的条目为position-1 */
                RingtoneManager rm = new RingtoneManager(context);
                rm.setType(RingtoneManager.TYPE_NOTIFICATION);
                rm.getCursor();
                return rm.getRingtoneUri(position - 1);
            } else {
                /* position为0是跟随系统，先得到系统所使用的铃声，然后播放 */
                return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
            }
        } else if (notificationSoundType == 1) {
            int position = PreferenceUtil.getInt(Constant.PREFERENCE_CHOOSE_RING, 0);
            if (position != 0) {
                /* 判断位置不为0则播放的条目为position-1 */
                RingtoneManager rm = new RingtoneManager(context);
                rm.setType(RingtoneManager.TYPE_ALARM);
                rm.getCursor();
                return rm.getRingtoneUri(position - 1);
            } else {
                /* position为0是跟随系统，先得到系统所使用的铃声，然后播放 */
                return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
            }
        } else if (notificationSoundType == 2) {
            String soundFile = PreferenceUtil.getString(Constant.PREFERENCE_CHOOSE_SOUND, "0");
            Uri mUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + File.separator + "CarOnlineSound"
                + File.separator + soundFile);
            return mUri;
        } else {
            int position = PreferenceUtil.getInt(Constant.PREFERENCE_CHOOSE_AUDIO, 0);
            if (position != 0) {
                /* 判断位置不为0则播放的条目为position-1 */
                RingtoneManager rm = new RingtoneManager(context);
                rm.setType(RingtoneManager.TYPE_NOTIFICATION);
                rm.getCursor();
                return rm.getRingtoneUri(position - 1);
            } else {
                /* position为0是跟随系统，先得到系统所使用的铃声，然后播放 */
                return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
            }
        }
    }

    public static boolean isRedpacketNotifyOn() {
        return false;
    }
}
