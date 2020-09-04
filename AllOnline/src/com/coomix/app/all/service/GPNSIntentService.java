package com.coomix.app.all.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.all.util.Utils;
import com.goome.gpns.GPNSInterface;
import com.goome.gpns.noti.NotifyManager;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by herry on 2016/12/26.
 */
public class GPNSIntentService extends IntentService {
    private static final String PARAM_SOURCE = "source";
    private static final String PARAM_EXTRAS = "extras";

    public GPNSIntentService() {
        super(GPNSIntentService.class.getName());
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(55555, createNotification());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private Notification createNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return null;
        }
        Utils.createChannel(nm, Constant.channelIdService, Constant.channelNameService);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constant.channelIdService);
        builder.setContentTitle(CommunityUtil.getAppName(this));
        builder.setContentText("有新的报警通知");
        builder.setWhen(System.currentTimeMillis()).setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_notify);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        return builder.build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String rawMsg = intent.getStringExtra(GPNSInterface.RAW_PUSH_MSG);
        if (StringUtil.isTrimEmpty(rawMsg)) {
            return;
        }
        try {
            JSONObject object = new JSONObject(rawMsg);
            object = object.optJSONObject(PARAM_EXTRAS);
            if (object == null) {
                return;
            }
            boolean exist = object.has(PARAM_SOURCE);

            NotifyManager.showNotify(getApplicationContext(), rawMsg, NotifyManager.MSG_TYPE_REMOTE, exist);

            if (!exist) {
                sendBroadcast(new Intent(GPNSInterface.ACTION_MESSAGE_RECEIVED));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void run(Context context, Intent intent) {
        intent.setClass(context, GPNSIntentService.class);
        // OPPO Colors手机息屏一段时间后会禁止启动service，需要try-catch
        // 一般在后台，不会影响用户体验
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
