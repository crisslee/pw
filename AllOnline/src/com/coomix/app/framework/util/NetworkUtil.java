package com.coomix.app.framework.util;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.log.GoomeLog;

import java.net.UnknownHostException;
import java.util.Locale;

public class NetworkUtil {
    public static final String CTWAP = "ctwap";
    public static final String CMWAP = "cmwap";
    public static final String WAP_3G = "3gwap";
    public static final String UNIWAP = "uniwap";
    
    public static final int TYPE_NET_WORK_DISABLED = 0;
    
    public static final int TYPE_OTHER_NET = 0;
    public static final int TYPE_CM_CU_WAP = 1;
    public static final int TYPE_CT_WAP = 2;

    public static final NetworkUtil INSTANCE  = new NetworkUtil();
    public static final  int  NET_TYPE_UNKNOWN = 0;
    /**
     * 0=未知网络<br/>
     * 1=wifi<br/>
     * 2=2G<br/>
     * 3=3G<br/>
     * 4=4G<br/>
     */
    private int               currNetType;
    private String            currNetExtraInfo;
    private NetworkUtil()
    {
        currNetState = getNetConnState();
        currNetType = NET_TYPE_UNKNOWN;
        currNetExtraInfo = "";
    }

    public static NetworkUtil getInstance()
    {
        return INSTANCE;
    }
    
    public static String getIP(String host) {
        java.net.InetAddress x;
        try {
            x = java.net.InetAddress.getByName(host);
            String ip_devdiv = x.getHostAddress();
            return ip_devdiv;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cnmger = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cnmger.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isConnected();
        }
        return false;
    }

   
    
    public static boolean isNetworkTypeAvailable(Context context, int networkType) {
        ConnectivityManager cnmger = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cnmger.getNetworkInfo(networkType);
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

   
    public static int getNetworkType(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (networkInfo != null) {
            String type = networkInfo.getExtraInfo();
            if (!TextUtils.isEmpty(type)) {
                if (type.toLowerCase(Locale.getDefault()).contains(CMWAP)
                		|| type.toLowerCase(Locale.getDefault()).contains(WAP_3G) 
                		|| type.toLowerCase(Locale.getDefault()).contains(UNIWAP))
                    return TYPE_CM_CU_WAP;//1

                else if (type.toLowerCase(Locale.getDefault()).contains(CTWAP))
                    return TYPE_CT_WAP;//2
                else
                    return TYPE_OTHER_NET;//0
            }
        }
        return TYPE_OTHER_NET;
    }
    
//    public static boolean isWap(Context context) {
//        return getNetworkType(context) > 1;
//    }
    /**
	 * 是否wap类型
	 * {@link #CM_UN_WAP} 移动/联通2/3G<br>
	 * {@link #CTWAP} 电信2/3G<br>
	 * DEFAULT 0 其它
	 */
    public static int isWap(Context context){
		NetworkInfo networkInfo =
				((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (networkInfo != null)
		{
			String type = networkInfo.getExtraInfo();
			if (!TextUtils.isEmpty(type))
			{
				if (type.toLowerCase(Locale.getDefault()).contains("cmwap")
						|| type.toLowerCase(Locale.getDefault()).contains("3gwap")
						|| type.toLowerCase(Locale.getDefault()).contains("uniwap"))
					return TYPE_CM_CU_WAP;
				else if (type.toLowerCase(Locale.getDefault()).contains("ctwap"))
					return TYPE_CT_WAP;
			}
		}
		return 0;
	}

    public static boolean isWifi(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && !networkInfo.getTypeName().toLowerCase(Locale.getDefault()).equals("wifi")) {
            return true;
        }
        return false;
    }
    
    public static boolean isWifiExtend(Context context){
		NetworkInfo networkInfo =
				((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
			return true;
		}
		return false;
	}
    
    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * 
     * @param context
     * @return true 表示开启
     */
    public static final boolean isGpsOpen(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps) {
            return true;
        }
        return false;
    }
    
    public static final int wifi = 40;
	public static final int mobile = 50;
    public static int checkNetwork(Context mContext) {
	    try {
	        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
	                .getSystemService(Context.CONNECTIVITY_SERVICE);
	        final NetworkInfo mobNetInfoActivity = connectivityManager
	                .getActiveNetworkInfo();
	        if (mobNetInfoActivity != null || mobNetInfoActivity.isAvailable()) {
	            // NetworkInfo不为null开始判断是网络类型
	            int netType = mobNetInfoActivity.getType();
	            if (netType == ConnectivityManager.TYPE_WIFI) {
	                // wifi net处理
	                Log.i("", "=====================>wifi网络");
	                return wifi;
	            } else if (netType == ConnectivityManager.TYPE_MOBILE) {
	                return mobile;
	            }
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return 0;
	    }
	    return 0;
	}
    
  //大类 : 0没有网络 1为WIFI网络 2/6/7为2G网络  3/4/5/8/9/11/12为3G网络 10为4G网络 -1为不知名网络
    public static final int NETWORK_TYPE_NO      = -1;

    /**
     * Current network is WIFI.用于性能上报和iOS协商一致
     */
    public static final int NETWORK_TYPE_WIFI    = 100;
    
    /**
     * 获取详细的网络类型，用于型性能上报
     *
     * @return
     */
    public int getDetailNetworType(Context context)
    {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectMgr != null)
        {
            NetworkInfo info = connectMgr.getActiveNetworkInfo();
            if (info != null)
            {
                switch (info.getType())
                {
                    case ConnectivityManager.TYPE_WIFI:
                        return NETWORK_TYPE_WIFI;

                    case ConnectivityManager.TYPE_MOBILE:
                        return info.getSubtype();
                }
            }
        }
        return NETWORK_TYPE_NO;
    }
    

    /**
	 * @return int <br/>
	 *         0=未知网络<br/>
	 *         1=wifi<br/>
	 *         2=2G<br/>
	 *         3=3G<br/>
	 *         4=4G<br/>
	 */
	public static int getNetworkTypeExtend(Context context)
	{
		int strNetworkType = 0;

		NetworkInfo networkInfo =
				((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
		{
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
			{
				strNetworkType = 1;
			}
			else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
			{
				// TD-SCDMA networkType is 17
				int networkType = networkInfo.getSubtype();
				switch (networkType)
				{
					case TelephonyManager.NETWORK_TYPE_GPRS:
					case TelephonyManager.NETWORK_TYPE_EDGE:
					case TelephonyManager.NETWORK_TYPE_CDMA:
					case TelephonyManager.NETWORK_TYPE_1xRTT:
					case TelephonyManager.NETWORK_TYPE_IDEN: // api<8 : replace
																// by 11
						strNetworkType = 2;
						break;
					case TelephonyManager.NETWORK_TYPE_UMTS:
					case TelephonyManager.NETWORK_TYPE_EVDO_0:
					case TelephonyManager.NETWORK_TYPE_EVDO_A:
					case TelephonyManager.NETWORK_TYPE_HSDPA:
					case TelephonyManager.NETWORK_TYPE_HSUPA:
					case TelephonyManager.NETWORK_TYPE_HSPA:
					case TelephonyManager.NETWORK_TYPE_EVDO_B: // api<9 :
																// replace by 14
					case TelephonyManager.NETWORK_TYPE_EHRPD: // api<11 :
																// replace by 12
					case TelephonyManager.NETWORK_TYPE_HSPAP: // api<13 :
																// replace by 15
						strNetworkType = 3;
						break;
					case TelephonyManager.NETWORK_TYPE_LTE: // api<11 : replace
															// by 13
						strNetworkType = 4;
						break;
					default:
						String _strSubTypeName = networkInfo.getSubtypeName();
						// http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信
						// 三种3G制式
						if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA")
								|| _strSubTypeName.equalsIgnoreCase("CDMA2000"))
						{
							strNetworkType = 3;
						}
						else
						{
							strNetworkType = 0;
						}

						break;
				}

			}
		}

		if(Constant.IS_DEBUG_MODE){
			System.out.println("NetworkType-----------" + strNetworkType);
		}		
		return strNetworkType;
	}
	
	/**
	 * @return int <br/>
	 *         0=未知网络<br/>
	 *         1=wifi<br/>
	 *         2=2G<br/>
	 *         3=3G<br/>
	 *         4=4G<br/>
	 */
	public static int getNetworkTypeExtend() {
		int strNetworkType = 0;

		NetworkInfo networkInfo = ((ConnectivityManager) AllOnlineApp
				.mApp.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				strNetworkType = 1;
			} else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				// TD-SCDMA networkType is 17
				int networkType = networkInfo.getSubtype();
				switch (networkType) {
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_IDEN: // api<8 : replace
															// by 11
					strNetworkType = 2;
					break;
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_EVDO_B: // api<9 :
															// replace by 14
				case TelephonyManager.NETWORK_TYPE_EHRPD: // api<11 :
															// replace by 12
				case TelephonyManager.NETWORK_TYPE_HSPAP: // api<13 :
															// replace by 15
					strNetworkType = 3;
					break;
				case TelephonyManager.NETWORK_TYPE_LTE: // api<11 : replace
														// by 13
					strNetworkType = 4;
					break;
				default:
					String _strSubTypeName = networkInfo.getSubtypeName();
					// http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信
					// 三种3G制式
					if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA")
							|| _strSubTypeName.equalsIgnoreCase("WCDMA")
							|| _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
						strNetworkType = 3;
					} else {
						strNetworkType = 0;
					}

					break;
				}

			}
		}

		if (Constant.IS_DEBUG_MODE) {
			System.out.println("NetworkType-----------" + strNetworkType);
		}
		return strNetworkType;
	}
    
	/**
     * @return int <br/>
     * 0=未知网络<br/>
     * 1=wifi<br/>
     * 2=2G<br/>
     * 3=3G<br/>
     * 4=4G<br/>
     */
	public int getCurrNetType() {
		if (currNetType == NET_TYPE_UNKNOWN) {
			ConnectivityManager connectivityManager = (ConnectivityManager) AllOnlineApp.mApp
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
			currNetType = getNetType(ni);
		}
		return currNetType;
	}

	public void setCurrNetType(int currNetType) {
		this.currNetType = currNetType;
	}

	public static final int NET_TYPE_WIFI = 1;
	public static final int NET_TYPE_2G = 2;
	public static final int NET_TYPE_3G = 3;
	public static final int NET_TYPE_4G = 4;

	public int getNetType(NetworkInfo ni) {
		if (ni == null) {
			return NET_TYPE_UNKNOWN;
		}
		if (ni.getType() == ConnectivityManager.TYPE_WIFI) {
			return NET_TYPE_WIFI;
		} else if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
			// TD-SCDMA networkType is 17
			int networkType = ni.getSubtype();
			switch (networkType) {
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_IDEN: // api<8 : replace
				// by 11
				return NET_TYPE_2G;

			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_EVDO_B: // api<9 :
				// replace by 14
			case TelephonyManager.NETWORK_TYPE_EHRPD: // api<11 :
				// replace by 12
			case TelephonyManager.NETWORK_TYPE_HSPAP: // api<13 :
				// replace by 15
				return NET_TYPE_3G;

			case TelephonyManager.NETWORK_TYPE_LTE: // api<11 : replace
				// by 13
				return NET_TYPE_4G;

			default:
				String _strSubTypeName = ni.getSubtypeName();
				// http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信
				// 三种3G制式
				if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA")
						|| _strSubTypeName.equalsIgnoreCase("WCDMA")
						|| _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
					return NET_TYPE_3G;
				} else {
					return NET_TYPE_UNKNOWN;
				}
			}
		}
		return NET_TYPE_UNKNOWN;
	}

	public static final String TAG = NetworkUtil.class.getSimpleName();
	private NetworkInfo.State getNetConnState() {
		NetworkInfo.State currNetState = NetworkInfo.State.DISCONNECTED;
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) AllOnlineApp
					.mApp.getSystemService(
							Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
			if (ni != null) {
				currNetState = ni.getState();
			} else {
				currNetState = NetworkInfo.State.DISCONNECTED;
			}
		} catch (Exception e) {
			// TODO: handle exception
			String fileMethodLine = "";
			fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
			fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
			fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
			GoomeLog.getInstance()
					.logE(fileMethodLine,
							"isNetWorkConnected() currNetState: "
									+ currNetState + "-->exception: "
									+ CommonUtil.getStackTrace(e), 0);
		}
		Log.i("NetworkUtil", "==get net conn state: " + currNetState);
		return currNetState;
	}
	
	private NetworkInfo.State currNetState;
	public boolean isNetWorkConnected()
    {
        if(currNetState != NetworkInfo.State.CONNECTED)
        {
            currNetState = getNetConnState();
        }

        return currNetState == NetworkInfo.State.CONNECTED;
    }
	public void setCurrNetExtraInfo(String currNetExtraInfo)
	{
		this.currNetExtraInfo = currNetExtraInfo;
	}
	public void setCurrNetState(NetworkInfo.State currNetState)
	{
		this.currNetState = currNetState;
	}
		
}