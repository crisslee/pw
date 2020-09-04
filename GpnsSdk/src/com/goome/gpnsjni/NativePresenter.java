package com.goome.gpnsjni;

import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.goome.gpns.GPNSInterface;
import com.goome.gpns.contentprovider.ProviderConst;
import com.goome.gpns.utils.LogUtil;

public class NativePresenter {
    public static final int CONNECT_OK = 1, CONNECT_FAILURE = -1, CONNECT_BREAK = 0, RECONNECT = 2,
        RECONNECT_FAILURE = -2, HEART_BEAT_STATUS = 3, HELLO_STATUS = 4, SERVICE_STOP = 5, C_KEEP_ALIVE = 10,
        DEBUG_INFO = 99;
    private static final int OFFSET = 5 * 1000;
    private static final String WALKLOCK_TAG = "coomix:gpns";
    private static final int OPEN = 1, CLOSE = 0;

    static {
        System.loadLibrary("GpnsSDK_1.4.7");
    }

    private Context context;
    private long lastNotifyTime = -1;
    private MsgCallback mCallback;
    private PowerManager powerMgr;
    private PowerManager.WakeLock wakeLock;

    public NativePresenter(Context context) {
        this.context = context;
    }

    public native String init(String host, String devInfo, int port, long cid, int mode);

    public native String start();

    public native String stop();

    /**
     * c库调此方法反馈Tcp连接状态
     *
     * @param connectionStatus
     * @param msg
     */
    public void onConnectionStatusChanged(int connectionStatus, String msg) {
        String statusMsg = null;
        if (connectionStatus == CONNECT_BREAK) {
            statusMsg = "断开连接" + msg;
        } else if (connectionStatus == CONNECT_OK && "Connecting".equals(msg)) {
            statusMsg = "连接成功" + msg;
        } else if (connectionStatus == CONNECT_FAILURE && "Connect failed".equals(msg)) {
            statusMsg = "连接失败" + msg;
        } else if (connectionStatus == RECONNECT && "reconnect OK".equals(msg)) {
            statusMsg = "重连成功" + msg;
        } else if (connectionStatus == RECONNECT && "reconnect failed".equals(msg)) {
            statusMsg = "重连失败" + msg;
        } else if (connectionStatus == HELLO_STATUS) {
            statusMsg = "hello包状态" + msg;
        } else if (connectionStatus == CONNECT_OK && "Login ok".equals(msg)) {
            statusMsg = "hello包发送成功" + msg;
        } else if (connectionStatus == HEART_BEAT_STATUS) {
            statusMsg = "心跳包状态：" + msg;
        } else if (connectionStatus == SERVICE_STOP) {
            statusMsg = "主动关闭服务：" + msg;
        } else if (connectionStatus == DEBUG_INFO) {
            statusMsg = "调试信息：" + msg;
        } else if (connectionStatus == C_KEEP_ALIVE) {
            statusMsg = msg;
            updateTimeStamp();
        } else {
            statusMsg = "其他情况：" + msg;
        }
        if (connectionStatus != C_KEEP_ALIVE) {
            Log.d("gpns",statusMsg);
        }
    }

    /**
     * 更新时间戳方法，每隔一定的时间C库通过调用onConnectionStatusChanged（）更新
     * 时间戳机制：如果时间戳过时，会认为服务进程运行异常，重启新的服务进程
     */
    private void updateTimeStamp() {
        long currentTime = System.currentTimeMillis();
        if (lastNotifyTime == -1 || (currentTime - lastNotifyTime) > (GPNSInterface.RECORD_PERIOD - OFFSET)) {
            boolean result = GPNSInterface.writeKV(context, ProviderConst.getContentUri(context),
                    ProviderConst.LAST_NOTIFY, currentTime + "");
            if (result) {
                lastNotifyTime = currentTime;
            }
        } else {
            // LogUtil.e("本次不更新");
        }
    }

    public void register(MsgCallback callback) {
        this.mCallback = callback;
    }

    public void unRegister() {
        this.mCallback = null;
    }

    /**
     * 当收到服务器的推送消息时，C库会调用此方法把消息传过来
     *
     * @param rawPushMsg raw message
     */
    public void getMessage(String rawPushMsg) {
        if (mCallback != null && !TextUtils.isEmpty(rawPushMsg)) {
            mCallback.broadcastMessage(rawPushMsg);
        }
    }

    /**
     * 获取或释放WakeLock锁，1是获取，0是释放； 必须成对使用，否则获取而不释放时，CPU不能进入休眠状态，耗电量剧增。
     *
     * @param status
     */
    public void setWakeLockCpuStatus(int status) {
        if (OPEN == status) {
            powerMgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (wakeLock != null && wakeLock.isHeld()) {
                // the wake lock has been acquired
                return;
            } else {
                wakeLock = powerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WALKLOCK_TAG);
                wakeLock.acquire();
            }
        } else {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
            }
        }
    }

    public void wakeCpuAwhile() {
        try {
            LogUtil.d("wake awhile");
            setWakeLockCpuStatus(OPEN);
            Thread.sleep(2000);
            setWakeLockCpuStatus(CLOSE);
        } catch (InterruptedException e) {
            // 忽略
            e.printStackTrace();
        }
    }

    public interface MsgCallback {
        void broadcastMessage(String rawPushMsg);
    }

}
