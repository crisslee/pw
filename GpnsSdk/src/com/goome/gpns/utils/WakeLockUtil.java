package com.goome.gpns.utils;

import android.content.Context;
import android.os.PowerManager;

public class WakeLockUtil {
	private static PowerManager powerMgr;
	private static PowerManager.WakeLock wakeLock;
	private static final String WALKLOCK_TAG = "GpnsWakeLock2015";
	public static void getWakeLock(Context context){
		powerMgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
		if(wakeLock != null && wakeLock.isHeld()){
			//the wake lock has been acquired
			return;
		}else{
			wakeLock = powerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WALKLOCK_TAG);  
			wakeLock.acquire();  
		}	
	}
	
	public static void releaseWakeLock(){
		if(wakeLock != null && wakeLock.isHeld()){
			wakeLock.release();
			wakeLock = null;
		}
	}
}
