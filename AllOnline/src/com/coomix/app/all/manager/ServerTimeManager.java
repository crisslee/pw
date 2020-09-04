package com.coomix.app.all.manager;

import android.os.SystemClock;

/**
 * Created by xxx on 2017/1/1.
 */
public class ServerTimeManager {
    private static ServerTimeManager instance = null;
    private static long iServerTime = 0;
    private static long iSystemUptime = 0;
    private boolean bRequesting = false;

    private ServerTimeManager() {

    }

    public synchronized static ServerTimeManager getInatnce() {
        if (instance == null) {
            instance = new ServerTimeManager();
        }

        return instance;
    }

    public void resetRequestStatus(boolean bRequestServerTime) {
        this.bRequesting = bRequestServerTime;
    }

    public long getCurrentServerTime() {
        if (iServerTime <= 0) {
            //requestServerTime(null);
            return System.currentTimeMillis();
        }
        return iServerTime + SystemClock.uptimeMillis() - iSystemUptime;
    }

    public void setServerTime(long time) {
        iServerTime = time;
        iSystemUptime = SystemClock.uptimeMillis();
    }

    public boolean isLaterThanSomeTime(long desTimeMils, long timesMils) {
        if (desTimeMils + timesMils >= getCurrentServerTime()) {
            return true;
        }
        return false;
    }
}
