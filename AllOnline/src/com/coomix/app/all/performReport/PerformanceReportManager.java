package com.coomix.app.all.performReport;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.log.AppConfigs;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.log.GoomeLogUtil;
import com.coomix.app.all.service.ServiceAdapter;
import com.coomix.app.all.service.ServiceAdapter.ServiceAdapterCallback;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.framework.app.Result;
import com.coomix.app.framework.util.OSUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

//import com.tencent.tinker.lib.tinker.Tinker;

/**
 * 性能数据上报 Created by Administrator on 2016/11/8.
 */

public class PerformanceReportManager implements ServiceAdapterCallback {
	private final String TAG = PerformanceReportManager.class.getSimpleName();
	private static PerformanceReportManager instance = null;
	private Context mContext;
	/**
	 * App启动开始的时间
	 */
	private long iAppStartTimeCost = 0;
	/**
	 * 判断App启动时间是否已经设置了
	 */
	private boolean bAppStartOk = false;
	private DBHelper dbHelper = null;
	private final int MAX_NUM_DEFAULT = 20;
	/**
	 * 用此变量减少读取数据库的次数。
	 * 初始值设置一个比较大的值，已超过可上传的最大值，第一次网络请求后,会触发上传，上传的时候会从数据库读取所有的数据，然后检查一次是否真正需要上传。
	 * 另外从数据库读取数据后会做一个修正，插入数据会累加，删除数据会减去。
	 */
	private int iInsertDataCount = 1000;

	// 访问后台服务器接口
	private ServiceAdapter mServiceAdapter = null;
	private int iUploadPerformanceId = -1;

	// 子线程更新数据库以及上传数据到服务器
	private HandlerThread handlerThread = null;
	private Handler workHandler = null;
	private int iUploadFailCount = 0;

	private PerformanceReportManager(Context context) {
		if (context != null) {
			this.mContext = context.getApplicationContext();
		} else {
			this.mContext = AllOnlineApp.mApp;
		}
		dbHelper = DBHelper.getHelper(mContext);
		mServiceAdapter = ServiceAdapter.getInstance(mContext);
		mServiceAdapter.registerServiceCallBack(this);
		// initSonThread();
	}

	private void initSonThread() {
		if (handlerThread == null || workHandler == null) {
			handlerThread = new HandlerThread("SavePostThread");
			handlerThread.start();
			workHandler = new Handler(handlerThread.getLooper());
		}
	}

	public static synchronized PerformanceReportManager getInstance(
			Context context) {
		if (instance == null) {
			instance = new PerformanceReportManager(context);
		}
		return instance;
	}



	/**
	 * App启动进入main界面的时候设置，表示App已经完全起来的时间。 同时用此时间减去开始启动的时间得到整个App启动耗时
	 */
	public void setApptoMainTime(long time) {
		// 只要调用到这个函数就置为true，只有第一次调用设置的时间才有意义
		if (!isOpenAndUpload() || isSetAppTimeOk()) {
			bAppStartOk = true;
			return;
		}
		bAppStartOk = true;
		iAppStartTimeCost = time - AllOnlineApp.getAppStartTime();
	}

	/**
	 * 标识是否已经设置了App的启动耗时时间
	 */
	public boolean isSetAppTimeOk() {
		return bAppStartOk;
	}

	/**
	 * 在子线程中调用。 插入数据到数据库，并判断是否需要上传，如果需要上传则上传到服务器
	 */
	private String oldData = null;
	public void saveDataAndUploadByCondition(
			final NetPerformanceDB netPerformanceDB) {
		if (!isOpenAndUpload() || netPerformanceDB == null) {
			return;
		}
		if (workHandler == null) {
			initSonThread();
		}
		workHandler.post(new Runnable() {
			@Override
			public void run() {
				// 数据库插入数据
				insertNetPerformance(netPerformanceDB);
				int iMaxNum = getCanUploadMaxNum();
				if (bAppStartOk && isOpenAndUpload() && iInsertDataCount >= iMaxNum) {
					//数据插入后,根据计数器判断，如果达到MAX_NUM，则从数据库取出来再判断是否达到上传条数
					String data = getNetPerformanceJsonStr(iMaxNum);										
					if(StringUtil.isTrimEmpty(data)){
						return;
					}							
					if(data.equals(oldData)){	
						if (Constant.IS_DEBUG_MODE) {
							System.out.println("saveData----data==olddata,");
							//return;
						}
					}	
					if (!StringUtil.isTrimEmpty(data)) {
						// 上传到服务器		
						oldData = data;
						iUploadPerformanceId = mServiceAdapter.performUpload(this.hashCode(), data);
					}
				}
			}
		});
	}

	private void insertNetPerformance(NetPerformanceDB netPerformanceDB) {
		// 如果没有打开性能上报，以及打开上报的网络条件与当前不符都不存储性能数据。
		if (!isOpenAndUpload()) {
			return;
		}
		try {
			if(dbHelper!=null){
				Dao dao = dbHelper.getDao(NetPerformanceDB.class);
				dao.create(netPerformanceDB);
				iInsertDataCount++;
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {

		}
	}

	private int getUploadMaxNum() {
		int maxNum = AllOnlineApp.getAppConfig()
				.getNet_performance_report_num();
		if (maxNum > 0) {
			return maxNum;
		}
		return MAX_NUM_DEFAULT;
	}

	/**
	 * 从数据库读取保存的访问网络接口的数据。如果少于MAX_NUM条则返回null。
	 * 如果达到了MAX_NUM条，那么返回拼接的json字符串，上传到服务器
	 */
	public String getNetPerformanceJsonStr(int iMaxNum) {		
		List<NetPerformanceDB> listNetPers = null;
		JsonObject jsonObject = null;
		Dao dao = null;
		try {
			if (dbHelper!=null) {
				dao = dbHelper.getDao(NetPerformanceDB.class);
			}
			if (dao != null) {				
				listNetPers = dao.queryForAll();
				iInsertDataCount = listNetPers.size();
				if (iInsertDataCount < iMaxNum) {
					// 没有达到上传条数
					 dao = null;
					return null;
				}
			}
		} catch (Exception e) {
            e.printStackTrace();
			String fileMethodLine = "";
			fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
			fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
			fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
            GoomeLog.getInstance().logE(fileMethodLine, "Read SQL data error.Performance will upload and can't upload: " + e.getMessage(), -1);
        }
		if (dao != null) {
			dao = null;
		}
		if (listNetPers != null) {
			jsonObject = new JsonObject();
			jsonObject.addProperty("appid", Constant.COOMIX_APP_ID);// App的编号，1001为爱车安，84为酷米客公交
			jsonObject.addProperty("appver",
					OSUtil.getAppVersionNameExtend(mContext));// APP版本号
			// String patchVersion =
			// Tinker.with(mContext).getTinkerLoadResultIfPresent().getPackageConfigByName("patchVersion");
			jsonObject.addProperty("patchver", "");// 补丁版本号patchVersion == null
													// ? "" : patchVersion
			jsonObject.addProperty("sn", OSUtil.getUdid(mContext));// 手机唯一指纹
			jsonObject.addProperty("appstartuptime",
					String.valueOf(iAppStartTimeCost));// APP启动耗时
			jsonObject.addProperty("os", "android"); // 系统类型
			jsonObject.addProperty("osver",
					String.valueOf(Build.VERSION.SDK_INT));// 系统版本
			jsonObject.addProperty("osextra", Build.VERSION.RELEASE);// 系统其他信息
			jsonObject.addProperty("production", Build.MODEL); // 硬件类型，小米note2，
																// 华为p8
			JsonArray jsonArray = new JsonArray();
			for (int i = 0; i < iMaxNum; i++) {
				NetPerformanceDB netPerformanceDB = listNetPers.get(i);
				if (netPerformanceDB != null) {
					JsonObject tempObj = new JsonObject();
					tempObj.addProperty("id", netPerformanceDB.getNameid());
					tempObj.addProperty("errorcode",
							netPerformanceDB.getErrorcode());
					tempObj.addProperty("timecost",
							netPerformanceDB.getTimecost());
					tempObj.addProperty("starttime",
							netPerformanceDB.getStarttime());
					tempObj.addProperty("upsize", netPerformanceDB.getUpsize());
					tempObj.addProperty("downsize",
							netPerformanceDB.getDownsize());
					tempObj.addProperty("detail", netPerformanceDB.getDetail());
					tempObj.addProperty("nettype",
							netPerformanceDB.getNetworktype());
					tempObj.addProperty("httpheader",
							netPerformanceDB.getHttpheader());
					jsonArray.add(tempObj);
				}
			}
			jsonObject.add("report", jsonArray); // 多条信息的array节点
			// 先减去避免重复上传
			iInsertDataCount = listNetPers.size() - iMaxNum;
			if (iInsertDataCount > 2 * iMaxNum) {
				// 如果上传时候服务器连续一段时间出错，没有返回状态时，则不会删除数据。此处做数据删除操作，避免数据库保存的数据过大
				iInsertDataCount = 0;
				deleteUploadedData(listNetPers.size());				
			}
			if (iInsertDataCount >= iMaxNum) {
				iInsertDataCount = iMaxNum / 2;
			}
			Gson gson = new Gson();// new GsonBuilder().disableHtmlEscaping().create();
			// String json = gson.toJson(jsonObject);
			// Log.i("Test", "====net json: " + json);
			return gson.toJson(jsonObject);
		}
		return null;
	}

	public void release() {
		if (Constant.IS_DEBUG_MODE) {
			System.out.println("PerformanceManager------release");
		}
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper = null;
		}
		if (mServiceAdapter != null) {
			mServiceAdapter.unregisterServiceCallBack(this);
			mServiceAdapter = null;
		}
		if (workHandler != null) {
			workHandler.removeCallbacksAndMessages(null);
			workHandler = null;
		}
		if (handlerThread != null) {
			handlerThread.quit();
			handlerThread = null;
		}
	}

	public boolean isOpenAndUpload() {

		 AppConfigs config = AllOnlineApp.getAppConfig();

		 return config.getPerformance_onoff() == 1 &&
		 GoomeLogUtil.compareNetWorkType(config.getPerformance_condition());
	}

	@Override
	public void callback(int messageId, Result result) {
		if (result == null) {
			return;
		}
		if (result.statusCode == Result.ERROR_NETWORK && iUploadPerformanceId == messageId) {
			//&& iUploadPerformanceId == messageId
			if (Constant.IS_DEBUG_MODE) {
				Log.e(TAG, "net performance upload net error.");
			}
		}else 
			if (messageId == iUploadPerformanceId
					&& result.apiCode == Constant.FM_APIID_UPLOAD_PERFORMANCE) {				
				Result resp = (Result) result.mResult;
				if (resp != null && result.statusCode == Result.OK) {
					if (Constant.IS_DEBUG_MODE) {
						System.out.println("uploadperform callback-----net performance upload success.");
					}
					// 删除已上传的数据
					iUploadFailCount = 0;
					deleteUploadedData(getUploadMaxNum());
				} else {					
					if (Constant.IS_DEBUG_MODE) {
						System.out.println("uploadperform callback-----net performance upload fail. error code: "
								+ (resp != null ? resp.statusCode : 0));
					}
					iUploadFailCount++;
					if (iUploadFailCount >= 3) {
						deleteUploadedData(getUploadMaxNum());
					}
				}
			}
	}

	private void deleteUploadedData(final int iMaxNum) {
		workHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					Dao dao = dbHelper.getDao(NetPerformanceDB.class);
					if (dao != null) {
						List<NetPerformanceDB> listNetPers = dao.queryForAll();
						if (listNetPers != null
								&& listNetPers.size() >= iMaxNum) {
							for (int i = 0; i < iMaxNum; i++) {
								dao.deleteById(listNetPers.get(i).getId());
							}
						}
					}
					dao = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private int getCanUploadMaxNum()
    {
        int maxNum = AllOnlineApp.getAppConfig().getNet_performance_report_num();
        if (maxNum > 0)
        {
            return maxNum;
        }
        return MAX_NUM_DEFAULT;
    }
}
