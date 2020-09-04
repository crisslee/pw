package com.coomix.app.framework.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.amap.api.location.AMapLocation;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.log.GoomeLog;
import com.coomix.app.all.log.GoomeLogErrorCode;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.performReport.NetPerformanceDB;
import com.coomix.app.all.performReport.PerformanceReportManager;
import com.coomix.app.all.service.AllOnlineApiClient;
import com.coomix.app.all.service.SSLSocketFactoryImp;
import com.coomix.app.all.util.CommunityUtil;
import com.coomix.app.all.util.ExperienceUserUtil;
import com.coomix.app.all.util.StringUtil;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.NetworkUtil;
import com.coomix.app.framework.util.OSUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.security.Security;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.tencent.bugly.crashreport.CrashReport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.RedirectException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

public class BaseApiClient {
    protected static final String TAG = BaseApiClient.class.getSimpleName();
    // private AndroidHttpClient mHttpClient;
    protected Context mContext;
	private Security security;
    protected String mServer;
    protected String mIP;

    private String value;
    private static int eventId = 5000;
	public static final String APP_TYPE = "all";//"bus";
	/** 酷米客公交社区so统一，此处跟随酷米客公交app版本号 */
	public static final String COMMUNITY_COOMIX_VERSION = "3.7.0";

    public static DefaultHttpClient sLongHttpClient;
    public static String mConnType;
    public final static String SHORT_CONNECTION = "0";
    public final static String LONG_CONNECTION = "1";

    private String getServiceOrIp;

	public static final String ENCRYPT_TEXT = "l1pPukuVJikaU5ge";
	public static final String ENCRYPT_PACKAGE_NAME = "bus.coomix.com";

	protected static ArrayList<String> encryptMethods = new ArrayList<String>();

	static {
		/***以模块的名字+#+方法名字，保证唯一性***/
		encryptMethods.add("redpacket#create"); //包红包
		encryptMethods.add("redpacket#allocate"); //拆红包
		encryptMethods.add("lottery#grab"); //抽奖
	}

	protected static ArrayList<String> resultEncryptMethods = new ArrayList<String>();

	static {
		resultEncryptMethods.add("/1/area/rectsearch");
		resultEncryptMethods.add("/1/devices/tracking");
	}

	protected static ArrayList<String> longSotimeMethods = new ArrayList<String>();

	static {
		/***以模块的名字+#+方法名字，保证唯一性***/
		longSotimeMethods.add("redpacket#create"); //包红包
		longSotimeMethods.add("redpacket#allocate"); //拆红包
		longSotimeMethods.add("lottery#grab"); //抽奖
		longSotimeMethods.add("activity#prepay"); //预支付
		longSotimeMethods.add("devinfo#app_getDevInfoAndLocation");//获取车辆列表
	}

    public String getGetServiceOrIp() {
        return getServiceOrIp;
    }

    public void setGetServiceOrIp(String getServiceOrIp) {
        this.getServiceOrIp = getServiceOrIp;
    }

    public static HttpClient getShortHttpClient(Context context) {
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, Constant.HTTP_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, Constant.SOCKET_TIMEOUT);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

        int waptype = NetworkUtil.isWap(context);
        if (waptype == NetworkUtil.TYPE_CM_CU_WAP) {

            HttpHost httpHost = new HttpHost("10.0.0.172", 80);
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);

        } else if (waptype == NetworkUtil.TYPE_CT_WAP) {
            HttpHost httpHost = new HttpHost("10.0.0.200", 80);
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
        }
        return httpClient;
    }

    private static HttpClient getLongHttpClient(Context context) {
        if (sLongHttpClient == null) {
            BasicHttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, Constant.HTTP_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParams, Constant.SOCKET_TIMEOUT);
            sLongHttpClient = new DefaultHttpClient(httpParams);
            int waptype = NetworkUtil.isWap(context);
            if (waptype == NetworkUtil.TYPE_CM_CU_WAP) {
                HttpHost httpHost = new HttpHost("10.0.0.172", 80); // 移动联通wap10.0.0.172
                sLongHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
            } else if (waptype == NetworkUtil.TYPE_CT_WAP) {
                HttpHost httpHost = new HttpHost("10.0.0.200", 80); // 电信wap
                                                                    // 10.0.0.200
                sLongHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
            }
        }
        return sLongHttpClient;
    }

    /**
     * 如果是开发环境，根据域名计算IP,
     * @param context
     * @param url
     * @return
     * @throws Exception
     */
	public HttpResponse excute(Context context, String url) throws Exception {
        HttpGet request = new HttpGet(url);
        request.addHeader("Connection", "Close");

        String cookieString = AllOnlineApiClient.getCookie();
        if(!StringUtil.isTrimEmpty(cookieString)){
        	request.addHeader("Cookie", cookieString);
        }
        modifyRequestToAcceptGzipResponse(request);
        // HttpClient client = getHttpClient(context);
        HttpClient client = null;
        //request.setHeader("Host", Constant.SERVER_DN);
        if (LONG_CONNECTION.equals(mConnType)) {
            request.addHeader("Connection", "Keep-Alive");
            client = getLongHttpClient(context);
        } else {
            request.addHeader("Connection", "Close");
            client = getShortHttpClient(context);
        }

//        if (NetworkUtil.isWap(context)) {
//            //return client.execute(request);
//        } else {
//            String sssString = getServer();
		//           // return client.execute(new HttpHost(getServer(), 80), request);
//        }
        return client.execute(request);
    }

	public HttpResponse excute(Context context, String url, boolean bHttps) throws Exception {
    	HttpGet request = new HttpGet(url);
        request.addHeader("Connection", "Close");

        String cookieString = AllOnlineApiClient.getCookie();
        if(!StringUtil.isTrimEmpty(cookieString)){
        	request.addHeader("Cookie", cookieString);
        }
        modifyRequestToAcceptGzipResponse(request);
        // HttpClient client = getHttpClient(context);
        HttpClient client = null;
        //request.setHeader("Host", Constant.SERVER_DN);
        if (LONG_CONNECTION.equals(mConnType)) {
            request.addHeader("Connection", "Keep-Alive");
            client = getLongHttpClient(context, bHttps);
        } else {
            request.addHeader("Connection", "Close");
            client = getShortHttpClient(context, bHttps);
        }

//        if (NetworkUtil.isWap(context)) {
//            //return client.execute(request);
//        } else {
//            String sssString = getServer();
		//           // return client.execute(new HttpHost(getServer(), 80), request);
//        }
        return client.execute(request);
    }

	public HttpResponse excute(Context context, String url, boolean bHttps, Header... headers) throws Exception {
    	HttpGet request = new HttpGet(url);
        request.addHeader("Connection", "Close");

        String cookieString = AllOnlineApiClient.getCookie();
        if(!StringUtil.isTrimEmpty(cookieString)){
        	request.addHeader("Cookie", cookieString);
        }
        modifyRequestToAcceptGzipResponse(request);
        // HttpClient client = getHttpClient(context);
        HttpClient client = null;
        //request.setHeader("Host", Constant.SERVER_DN);
        if (LONG_CONNECTION.equals(mConnType)) {
            request.addHeader("Connection", "Keep-Alive");
            client = getLongHttpClient(context, bHttps);
        } else {
            request.addHeader("Connection", "Close");
            client = getShortHttpClient(context, bHttps);
        }

//        if (NetworkUtil.isWap(context)) {
//            //return client.execute(request);
//        } else {
//            String sssString = getServer();
		//           // return client.execute(new HttpHost(getServer(), 80), request);
//        }
        return client.execute(request);
    }

    private static DefaultHttpClient getHttpClient(HttpParams params)
	{
		try
		{
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new SSLSocketFactoryImp(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			HttpProtocolParams.setUseExpectContinue(params, true);

			// 设置http https支持
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));// SSL/TSL的认证过程，端口为443
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
			return new DefaultHttpClient(ccm, params);
		}catch (Exception e){
			CrashReport.postCatchedException(e);
			return new DefaultHttpClient(params);
		}
	}

    private static HttpClient getLongHttpClient(Context context, boolean bHttps){
		if (sLongHttpClient == null){
			BasicHttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, Constant.HTTP_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, Constant.SOCKET_TIMEOUT);
			// sLongHttpClient = new DefaultHttpClient(httpParams);//引入https后注释掉了此处
			sLongHttpClient = getHttpClient(httpParams);
			int port = 80;
			if (bHttps){
				port = 443;
			}
			int waptype = NetworkUtil.isWap(context);
			if (waptype == NetworkUtil.TYPE_CM_CU_WAP){
				//System.out.println("is cmwap or unwap");
				HttpHost httpHost = new HttpHost("10.0.0.172", port);
				sLongHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
			}else if (waptype == NetworkUtil.TYPE_CT_WAP){
				//System.out.println("is ctwap");
				HttpHost httpHost = new HttpHost("10.0.0.200", port);
				sLongHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
			}
		}
		return sLongHttpClient;
	}

    public static HttpClient getShortHttpClient(Context context, boolean bHttps)
	{
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, Constant.HTTP_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, Constant.SOCKET_TIMEOUT);
		// httpClient = new DefaultHttpClient(httpParams); //引入https后注释掉了此处
		DefaultHttpClient httpClient = getHttpClient(httpParams);

		int waptype = NetworkUtil.isWap(context);
		int port = 80;
		if (bHttps){
			port = 443;
		}
		if (waptype == NetworkUtil.TYPE_CM_CU_WAP){
			//System.out.println("is cmwap or unwap");
			HttpHost httpHost = new HttpHost("10.0.0.172", port);
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
		}else if (waptype == NetworkUtil.TYPE_CT_WAP){
			//System.out.println("is ctwap");
			HttpHost httpHost = new HttpHost("10.0.0.200", port);
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
		}
		return httpClient;
	}

    protected static HttpResponse excutePost(Context context, String url, HttpEntity params,boolean bhttps) throws ClientProtocolException, IOException
   	{
   		HttpPost request = new HttpPost(url);
   		request.setEntity(params);

   		String cookieString = AllOnlineApiClient.getCookie();
   		if(!StringUtil.isTrimEmpty(cookieString)){
   			request.addHeader("Cookie", cookieString);
   		}
   		modifyRequestToAcceptGzipResponse(request);
   		// HttpClient client = getHttpClient(context);
   		HttpClient client = null;

   		if (LONG_CONNECTION.equals(mConnType)){
   			request.addHeader("Connection", "Keep-Alive");
   			client = getLongHttpClient(context,bhttps);
   		}else{
   			request.addHeader("Connection", "Close");
   			client = getShortHttpClient(context,bhttps);
		}

		return client.execute(request);
	}

    protected static HttpResponse excutePost(Context context, String url, HttpEntity params) throws ClientProtocolException, IOException
	{
		HttpPost request = new HttpPost(url);
		request.setEntity(params);

		String cookieString = AllOnlineApiClient.getCookie();
   		if(!StringUtil.isTrimEmpty(cookieString)){
   			request.addHeader("Cookie", cookieString);
   		}

		modifyRequestToAcceptGzipResponse(request);
		// HttpClient client = getHttpClient(context);
		HttpClient client = null;

		if (LONG_CONNECTION.equals(mConnType)){
			request.addHeader("Connection", "Keep-Alive");
			client = getLongHttpClient(context);
		}else{
			request.addHeader("Connection", "Close");
			client = getShortHttpClient(context);
		}

		return client.execute(request);
	}

    //----------------------------------------------    
	protected static HttpResponse excutePost(Context context, String url,
			String server, HttpEntity params, boolean bHttps,int conTimeout, int soTimeout, StringBuffer sbHeader, Header... headers)
			throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(params);

		String cookieString = AllOnlineApiClient.getCookie();
   		if(!StringUtil.isTrimEmpty(cookieString)){
   			httpPost.addHeader("Cookie", cookieString);
   		}

		AndroidHttpClient.modifyRequestToAcceptGzipResponse(httpPost);
		HttpClient client = null;

		if (AllOnlineApp.getAppConfig() != null
				&& AllOnlineApp.getAppConfig().getHttp_keepalive_onoff() == 1) {
			httpPost.addHeader("Connection", "Keep-Alive");
			// client = getLongHttpClient(context, bHttps, conTimeout,
			// soTimeout);
		} else {
			httpPost.addHeader("Connection", "Close");
			// client = getShortHttpClient(context, bHttps, conTimeout,
			// soTimeout);
		}
		client = getMyHttpClient(context, bHttps, conTimeout, soTimeout);
		if (headers != null && headers.length > 0) {
			for (Header header : headers) {
				httpPost.addHeader(header);
			}
		}
		httpPost.addHeader("Host", server);
		if (sbHeader != null) {
			// 用于进行网络数据性能统计
			sbHeader = setHeaderToString(httpPost.getAllHeaders(), sbHeader);
		}
		return client.execute(httpPost);
	}

	protected static HttpResponse excute(Context context, String url,
			String server, boolean bHttps, int conTimeout, int soTimeout,StringBuffer sbHeader, Header... headers)
			throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(url);

		String cookieString = AllOnlineApiClient.getCookie();
   		if(!StringUtil.isTrimEmpty(cookieString)){
   			httpGet.addHeader("Cookie", cookieString);
   		}

		AndroidHttpClient.modifyRequestToAcceptGzipResponse(httpGet);
		HttpClient client = null;

		if (AllOnlineApp.getAppConfig() != null
				&& AllOnlineApp.getAppConfig().getHttp_keepalive_onoff() == 1) {
			httpGet.addHeader("Connection", "Keep-Alive");
			// client = getLongHttpClient(context, bHttps, conTimeout,
			// soTimeout);
		} else {
			httpGet.addHeader("Connection", "Close");
			// client = getShortHttpClient(context, bHttps, conTimeout,
			// soTimeout);
		}
		client = getMyHttpClient(context, bHttps, conTimeout, soTimeout);

		if (headers != null && headers.length > 0) {
			for (Header header : headers) {
				httpGet.addHeader(header);
			}
		}
		httpGet.addHeader("Host", server);
		if (sbHeader != null) {
			// 用于进行网络数据性能统计
			sbHeader = setHeaderToString(httpGet.getAllHeaders(), sbHeader);
		}
		return client.execute(httpGet);
	}

	public static HttpClient getMyHttpClient(Context context, boolean bHttps, int conTimeout, int soTimeout) {
		DefaultHttpClient httpClient = getHttpClient();

		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
				conTimeout);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(),
				soTimeout);

		int waptype = NetworkUtil.isWap(context);
		int port = 80;
		if (bHttps) {
			port = 443;
		}
		if (waptype == NetworkUtil.TYPE_CM_CU_WAP) {
			//System.out.println("is cmwap or unwap");
			HttpHost httpHost = new HttpHost("10.0.0.172", port);
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					httpHost);
		} else if (waptype == NetworkUtil.TYPE_CT_WAP) {
			//System.out.println("is ctwap");
			HttpHost httpHost = new HttpHost("10.0.0.200", port);
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					httpHost);
		}
		return httpClient;
	}

	public static DefaultHttpClient httpClient = null;
	private static synchronized DefaultHttpClient getHttpClient() {
		if (httpClient == null) {
			BasicHttpParams httpParams = new BasicHttpParams();
			try {
				HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
				HttpProtocolParams.setUseExpectContinue(httpParams, true);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					int iMaxConnsPerHost = ConnPerRouteBean.DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
					int iMaxTotalConns = ConnManagerParams.DEFAULT_MAX_TOTAL_CONNECTIONS;
					if (AllOnlineApp.getAppConfig() != null) {
						iMaxConnsPerHost = AllOnlineApp.getAppConfig()
								.getMax_connections_per_host();
						iMaxTotalConns = AllOnlineApp.getAppConfig()
								.getMax_total_connections();
					}
					// set max connections per host
					ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
							new ConnPerRouteBean(iMaxConnsPerHost));
					// set max total connections
					ConnManagerParams.setMaxTotalConnections(httpParams,
							iMaxTotalConns);
				}

				// 设置http https支持
				KeyStore trustStore = KeyStore.getInstance(KeyStore
						.getDefaultType());
				trustStore.load(null, null);
				SSLSocketFactory sf = new SSLSocketFactoryImp(trustStore);
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				SchemeRegistry registry = new SchemeRegistry();
				registry.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				registry.register(new Scheme("https", sf, 443));// SSL/TSL的认证过程，端口为443
				ClientConnectionManager ccm = new ThreadSafeClientConnManager(
						httpParams, registry);
				httpClient = new DefaultHttpClient(ccm, httpParams);
			} catch (Exception e) {
				CrashReport.postCatchedException(e);
				httpClient = new DefaultHttpClient(httpParams);
			}
		}
		return httpClient;
	}

	private static StringBuffer setHeaderToString(Header[] headers,
			StringBuffer stringBuffer) {
		if (headers != null) {
			if (stringBuffer == null) {
				stringBuffer = new StringBuffer();
			}
			for (int i = 0; i < headers.length; i++) {
				Header header = headers[i];
				if (header != null) {
					stringBuffer.append(header.getName());
					stringBuffer.append(":");
					stringBuffer.append(header.getValue());
					stringBuffer.append("\n");
				}
			}
		}
		return stringBuffer;
	}

	public BaseApiClient(Context context) {
        mContext = context;
		security = new Security();
        mConnType = SHORT_CONNECTION;
    }

    protected synchronized int generateEventId() {
        if (eventId >= Integer.MAX_VALUE - 1) {
            eventId = 5000;
        }
        eventId++;
        return eventId;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        is.close();

        return sb.toString().trim();
    }

    protected static String convertStreamToStringNew(InputStream is) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			sb.append(line);
			sb.append("\n");
		}
		is.close();
		return sb.toString().trim();
	}
    
    /*protected String getDN() {
        return "http://" + AllOnlineApp.sRespDomainAdd.domainMain;//"http://apinew.gpsoo.net:80";
    }

    public static String getDN(boolean bhttps) {
    	if(bhttps){
    		return "https://" + AllOnlineApp.sRespDomainAdd.domainMain;
    	}else {
    		return "http://" + AllOnlineApp.sRespDomainAdd.domainMain;
    	}       
    }*/

	public static String getDN(boolean bhttps,String server) {
    	if(bhttps){
    		return "https://" + server;
    	}else {
    		return "http://" + server;
		}
    }

	protected boolean isContainHttp(boolean bhttps,String url){
    	boolean flag = false;
		if (bhttps) {
    	}else {
		}
    	if(!StringUtil.isTrimEmpty(url) && (url.contains("https://")||url.contains("http://"))){
			flag = true;
		}
    	return flag;
    }

	protected String getOldDN() {
        //getServiceOrIp = (TextUtils.isEmpty(mIP) || NetworkUtil.isWap(mContext) ? mServer : mIP);
        return "http://" + getServiceOrIp;//"http://apinew.gpsoo.net:80";
    }

	//    public void processException(Result resp, Exception e, String content) {
//    	String debugMsg = "Httpcontent: " + content;
//    	GoomeLog.getInstance().logE(AllOnlineApiClient.class.getSimpleName(), debugMsg,0);
//        processException(resp, e);
//        if ("true".equals(value) && !TextUtils.isEmpty(content)) {
//            //MobclickAgent.reportError(mContext, content);
//        }
//    }

    public void processException(Result resp, Exception e) {
//    	String debugMsg = "Errcode: " + resp.statusCode + ",Msg: " + resp.errorMessage
//    			+",Exception: " + CommonUtil.getStackTrace(e)
//    			+",NetworkType:" + NetworkUtil.getNetworkTypeExtend(mContext);
//    	GoomeLog.getInstance().logE(AllOnlineApiClient.class.getSimpleName(), debugMsg,0);

		if (e instanceof JSONException) {
            resp.statusCode = Result.ERROR_JSON_PARSE; // -20;           
        } else if (e instanceof SocketTimeoutException) {
            resp.statusCode = Result.ERROR_NETWORK_SOCKET_TIMEOUT; // -15;  // getGetServiceOrIp
        } else if (e instanceof ConnectTimeoutException) {
            resp.statusCode = Result.ERROR_NETWORK_CONNECT_TIMEOUT; // -11;
        } else if (e instanceof HttpHostConnectException || e instanceof UnknownHostException || e instanceof ConnectException) {
            resp.statusCode = Result.ERROR_NETWORK_CONNECT_HOST; // -12
        } else if (e instanceof HttpResponseException || e instanceof NoHttpResponseException) {
            resp.statusCode = Result.ERROR_NETWORK_RESPONSE; // -13;
        } else if (e instanceof RedirectException) {
            resp.statusCode = Result.ERROR_NETWORK_REDIRECT; // -14;
        } else if (e instanceof SocketException || e instanceof ProtocolException || e instanceof java.net.ProtocolException) {
            resp.statusCode = Result.ERROR_NETWORK; // -10;
        } else {
            resp.statusCode = Result.ERROR_UNKNOW; // -100;
        }

		// 捕捉到异常 此时错误信息自定义
        //resp.errorMessage = getGetServiceOrIp();

		if ("true".equals(value)) {
            //MobclickAgent.reportError(mContext, e);
        }
        e.printStackTrace();
    }

    public static InputStream getUngzippedContent(HttpEntity entity) throws IOException {
        InputStream responseStream = entity.getContent();
        if (responseStream == null)
            return responseStream;
        Header header = entity.getContentEncoding();
        if (header == null)
            return responseStream;
        String contentEncoding = header.getValue();
        if (contentEncoding == null)
            return responseStream;
        if (contentEncoding.contains("gzip"))
            responseStream = new GZIPInputStream(responseStream);
        return responseStream;
    }

    public static void modifyRequestToAcceptGzipResponse(HttpRequest request) {
        request.addHeader("Accept-Encoding", "gzip");
    }

    public String getServer() {
        return mServer;
    }

    public void setServer(String mServer) {
        this.mServer = mServer;
    }

	public class httpStateException extends Exception {
		public httpStateException(String msg) {
			super(msg);
		}
	}

	public class apiReturnFalseException extends Exception {
		public apiReturnFalseException(String msg) {
			super(msg);
		}
	}

	public class apiReturnIllegalFormatException extends Exception {
		public apiReturnIllegalFormatException(String msg) {
			super(msg);
		}
	}

	public class apiParamException extends Exception {
		public apiParamException(String msg) {
			super(msg);
		}
	}

	public class apiHtmlException extends Exception {
		public apiHtmlException(String msg) {
			super(msg);
		}
	}

	public class apiReturnIllegalDomainException extends Exception {
		public apiReturnIllegalDomainException(String msg) {
			super(msg);
		}
	}

	private final int  MAX_TRY_TIMES    = 3;
	private static final String logUploadMethod = "/1/log?method=upload";
    /**
     * 异常处理
     * @param resp Response
     * @param e Exception
     * @param content String 返回内容
     * @param stringBufferHeader StringBuffer http请求头,http请求相关异常需要上传
     */
	@SuppressLint("DefaultLocale")
	protected void processException(Result resp, Exception e, String content, StringBuffer... stringBufferHeader){
		resp.success = false;
		if (StringUtil.isTrimEmpty(resp.server)
				|| "null".equalsIgnoreCase(resp.server)) {
			resp.server = "";
		}
		if (StringUtil.isTrimEmpty(resp.ip)
				|| "null".equalsIgnoreCase(resp.ip)) {
			resp.ip = NetworkUtil.getIP(resp.server);
		}
		int wapType = NetworkUtil.isWap(mContext);
		resp.wapType = wapType;
		if (e instanceof JSONException) {
			if (resp.statusCode == 200) {
				if (content.contains("unknow") && content.contains("method")) {
					resp.errcode = Result.ERROR_API_RETURN_UNKNOW_METHOD;
					resp.msg = String.format(
							"接口返回未知方法异常(%1$s%2$d %3$s)", wapType > 0 ? "wap "
									+ wapType + " " : "", resp.errcode,
							resp.statusCode == -1 ? "" : resp.statusCode);
				} else {
					resp.errcode = Result.ERROR_API_RETURN;
					resp.msg = String.format("网络返回异常(%1$s%2$d %3$s)",
							wapType > 0 ? "wap " + wapType + " " : "",
							resp.errcode, resp.statusCode == -1 ? ""
									: resp.statusCode);
				}
			} else {
				// 返回码！= 200已抓取httpStateException,理论上不会运行到此
				resp.errcode = Result.ERROR_JSON_PARSE;
				resp.msg = String.format("数据解析失败(%1$s%2$d %3$s)",
						wapType > 0 ? "wap " + wapType + " " : "",
						resp.errcode, resp.statusCode == -1 ? ""
								: resp.statusCode);
			}
		} else if (e instanceof apiParamException) {
			resp.errcode = Result.ERROR_API_ILLEGAL_UPDOWN_PARAM;
			resp.msg = "上传参数错误";
		} else if (e instanceof apiReturnIllegalDomainException) {
			resp.errcode = Result.ERROR_API_RETURN_ILLEGAL_DOMAIN;
			resp.msg = String.format("接口返回域名不合法(%1$s%2$d %3$s)",
					wapType > 0 ? "wap " + wapType + " " : "", resp.errcode,
					resp.statusCode == -1 ? "" : resp.statusCode);
		} else if (e instanceof apiHtmlException) {
			if (NetworkUtil.isWifiExtend(mContext)) {
				resp.errcode = Result.ERROR_API_RETURN_HTML;
			} else {
				resp.errcode = Result.ERROR_API_RETURN;
			}
			resp.msg = String.format("接口返回html代码(%1$s%2$d %3$s)",
					wapType > 0 ? "wap " + wapType + " " : "", resp.errcode,
					resp.statusCode == -1 ? "" : resp.statusCode);
		}
		if (e instanceof apiReturnIllegalFormatException) {
			if (content == null || content.length() <= 0) {
				resp.errcode = Result.ERROR_API_RETURN_NULL;
				resp.msg = String.format("接口返回为空(%1$s%2$d %3$s)",
						wapType > 0 ? "wap " + wapType + " " : "",
						resp.errcode, resp.statusCode == -1 ? ""
								: resp.statusCode);
			} else if (content.startsWith("{") && content.endsWith("}")) {
				// 未按规定返回，success,errcode,msg,data
				resp.errcode = Result.ERROR_API_RETURN_FORMAT;
				resp.msg = String.format("接口返回格式错误(%1$s%2$d %3$s)",
						wapType > 0 ? "wap " + wapType + " " : "",
						resp.errcode, resp.statusCode == -1 ? ""
								: resp.statusCode);
			} else {
				// 返回非json
				// !content.startsWith("{") || !content.endsWith("}")
				int end = content.length() > 15 ? 15 : content.length();
				String contentStr = content.substring(0, end).toLowerCase();
				if (contentStr.startsWith("<!doctype html")
						|| contentStr.startsWith("<html")
						|| contentStr.startsWith("?<!doctype html")
						|| contentStr.startsWith("<script")
						|| contentStr.startsWith("<meta")) {
					if (NetworkUtil.isWifiExtend(mContext)) {
						resp.errcode = Result.ERROR_API_RETURN_HTML;
						/*
						 * resp.htmlCode = content; resp.htmlCodeBaseUrl =
						 * NetworkUtil.getLocIp(mContext);
						 */
						// .getIP(BusOnlineApp.server);
						// return;
					} else {
						resp.errcode = Result.ERROR_API_RETURN;
					}
					resp.msg = String.format(
							"接口返回html代码(%1$s%2$d %3$s)", wapType > 0 ? "wap "
									+ wapType + " " : "", resp.errcode,
							resp.statusCode == -1 ? "" : resp.statusCode);
				} else if (content.contains("unknow")
						&& content.contains("method")) {
					resp.errcode = Result.ERROR_API_RETURN_UNKNOW_METHOD;
					resp.msg = String.format(
							"接口返回未知方法异常(%1$s%2$d %3$s)", wapType > 0 ? "wap "
									+ wapType + " " : "", resp.errcode,
							resp.statusCode == -1 ? "" : resp.statusCode);
				} else {
					resp.errcode = Result.ERROR_API_RETURN;
					resp.msg = String.format("网络返回异常(%1$s%2$d %3$s)",
							wapType > 0 ? "wap " + wapType + " " : "",
							resp.errcode, resp.statusCode == -1 ? ""
									: resp.statusCode);
				}
			}
		} else if (e instanceof apiReturnFalseException) {
			resp.errcode = Result.ERROR_NETWORK_SOCKET_TIMEOUT;
			resp.msg = String.format("接口返回false(%1$s%2$d %3$s %4$s)",
					wapType > 0 ? "wap " + wapType + " " : "", resp.errcode,
					resp.statusCode == -1 ? "" : resp.statusCode,
					resp.errorMessage);
		} else if (e instanceof SocketTimeoutException) {
			resp.errcode = Result.ERROR_NETWORK_SOCKET_TIMEOUT;
			resp.msg = String.format("连接SO超时(%1$s%2$d %3$s)",
					wapType > 0 ? "wap " + wapType + " " : "", resp.errcode,
					resp.statusCode == -1 ? "" : resp.statusCode);
		} else if (e instanceof ConnectTimeoutException) {
			resp.errcode = Result.ERROR_NETWORK_CONNECT_TIMEOUT;
			resp.msg = String.format("连接Http超时(%1$s%2$d %3$s)",
					wapType > 0 ? "wap " + wapType + " " : "", resp.errcode,
					resp.statusCode == -1 ? "" : resp.statusCode);
		} else if (e instanceof HttpHostConnectException
				|| e instanceof UnknownHostException
				|| e instanceof ConnectException) {
			resp.errcode = Result.ERROR_NETWORK_CONNECT_HOST;
			resp.msg = String.format("无法连接到主机(%1$s%2$d %3$s)",
					wapType > 0 ? "wap " + wapType + " " : "", resp.errcode,
					resp.statusCode == -1 ? "" : resp.statusCode);
		} else if (e instanceof HttpResponseException
				|| e instanceof NoHttpResponseException) {
			resp.errcode = Result.ERROR_NETWORK_RESPONSE;
			resp.msg = String.format("服务器无响应(%1$s%2$d %3$s)",
					wapType > 0 ? "wap " + wapType + " " : "", resp.errcode,
					resp.statusCode == -1 ? "" : resp.statusCode);
		} else if (e instanceof CircularRedirectException
				|| e instanceof RedirectException) {
			resp.errcode = Result.ERROR_NETWORK_REDIRECT;
			resp.msg = String.format("连接重定向(%1$s%2$d %3$s)",
					wapType > 0 ? "wap " + wapType + " " : "", resp.errcode,
					resp.statusCode == -1 ? "" : resp.statusCode);
		} else if (e instanceof SocketException
				|| e instanceof ClientProtocolException
				|| e instanceof ProtocolException
				|| e instanceof java.net.ProtocolException) {
			resp.errcode = Result.ERROR_NETWORK;
			resp.msg = String.format("网络异常(%1$s%2$d %3$s)",
					wapType > 0 ? "wap " + wapType + " " : "", resp.errcode,
					resp.statusCode == -1 ? "" : resp.statusCode);
		} else if (e instanceof httpStateException) {
			resp.errcode = Result.ERROR_HTTP_STATE;
			resp.msg = String.format("服务器异常(%1$s%2$d %3$s)",
					wapType > 0 ? "wap " + wapType + " " : "", resp.errcode,
					resp.statusCode == -1 ? "" : resp.statusCode);
		} else {
			resp.errcode = Result.ERROR_UNKNOW;
			resp.msg = String.format("未知异常(%1$s%2$d %3$s)",
					wapType > 0 ? "wap " + wapType + " " : "", resp.errcode,
					resp.statusCode == -1 ? "" : resp.statusCode);
		}
		// 网络控制是否接收umeng bug && 测试环境下不上传umeng bug）
		StringBuilder logSB = new StringBuilder();
		logSB.append("Ip: ");
		logSB.append(resp.ip);
		logSB.append(",Server: ");
		logSB.append(resp.server);
		logSB.append(",Errcode: ");
		logSB.append(resp.errcode);
		logSB.append(",StatusCode: ");
		logSB.append(resp.statusCode);
		logSB.append(",Msg: ");
		logSB.append(resp.msg);
		logSB.append(",ErrorMeg: ");
		logSB.append(resp.errorMessage);
		logSB.append(" \nUrl: ");
		logSB.append(resp.debugUrl);
		int errorCode = 0;
		if (e instanceof NullPointerException) {
			// // 不上传NullPointException
			// return;
			logSB.append(" \nException: ");
			logSB.append(CommonUtil.getStackTrace(e));
			errorCode = GoomeLogErrorCode.ERROR_CODE_NETWORK_NULL;
		} else if (e instanceof JSONException) {
			if (resp.statusCode == 200) {
				logSB.append(" \nHttpcontent: ");
				logSB.append(content);
				errorCode = GoomeLogErrorCode.ERROR_CODE_NETWORK_JSON;
			} else {
				logSB.append(" \nException: ");
				logSB.append(CommonUtil.getStackTrace(e));
				errorCode = GoomeLogErrorCode.ERROR_CODE_NETWORK_JSON;
			}
		} else if (e instanceof apiReturnFalseException) {
			logSB.append(" \nHttpcontent: ");
			logSB.append(content);
			errorCode = resp.errcode;
		} else if (e instanceof httpStateException) {
			logSB.append(" \nHttpcontent: ");
			logSB.append(content);
			errorCode = resp.errcode;
		} else if (e instanceof apiReturnIllegalDomainException) {
			logSB.append(" \nHttpcontent: ");
			logSB.append(content);
			errorCode = resp.errcode;
		} else if (e instanceof apiReturnIllegalFormatException) {
			logSB.append(" \nHttpcontent: ");
			logSB.append(content);
			errorCode = resp.errcode;
		} else if (e instanceof apiParamException) {
			errorCode = resp.errcode;
		} else {
			logSB.append(" \nException: ");
			logSB.append(CommonUtil.getStackTrace(e));
			errorCode = GoomeLogErrorCode.ERROR_CODE_NETWORK;
		}
		if (stringBufferHeader != null && stringBufferHeader.length > 0
				&& stringBufferHeader[0] != null) {
			// http请求头
			logSB.append(" \nHeader: ");
			logSB.append(stringBufferHeader[0]);
		}
		if (resp.isLastTimeIpQuery
				&& (resp.debugUrl != null && !resp.debugUrl
						.contains(logUploadMethod))) {
			// 仅接口访问的最后一次ip访问， 并且 不是日志上传接口 才自动上传日志
			String fileMethodLine = "";
			fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
			fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
			fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
			GoomeLog.getInstance().logE(fileMethodLine, logSB.toString(), errorCode);
		} else {
			String fileMethodLine = "";
			fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
			fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
			fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
			GoomeLog.getInstance().logE(fileMethodLine, logSB.toString(), 0);
		}
	}

	//获取数据接收超时时间,上传时候需要根据数据大小来计算超时时间.返回毫秒
	protected int getHttpSOTimeout(int iTryTimes, long size, boolean bWap) {
		int time = 0;
		if (size > 0) {
			size = size / 1024; // 字节换成KB
			time = (int) Math.ceil(size
					/ AllOnlineApp.getAppConfig().getNetwork_upload_speed()
					* 1000);
			if (time <= 0) {
				time = 20000; // 按照普通图片200kb除以10kb/s计算得到
			}
			switch (iTryTimes) {
			case 0:
				// 第一次尝试，默认是10kb/s计算得到的时间
				break;
			case 1:
				time = (int) (time * 1.5f); // 第二次尝试最初的1.5倍
				break;
			case 2:
			default:
				time = time * 2;// 第三次尝试及默认值最初的2倍
				break;
			}
		} else {
			switch (iTryTimes) {
			case 0:
				time = AllOnlineApp.getAppConfig().getNetwork_timeout_fir() * 1000; // 第一次尝试
				break;
			case 1:
				time = AllOnlineApp.getAppConfig().getNetwork_timeout_sec() * 1000;// 第二次尝试
				break;
			case 2:
			default:
				time = AllOnlineApp.getAppConfig().getNetwork_timeout_thir() * 1000;// 第三次尝试及其他默认值
				break;
			}
		}

		if (bWap) {
			// WAP环境超时时间是普通的3倍
			time *= 3;
		}
		return time;
	}

	// 获取连接超时的时间.返回毫秒
	protected int getHttpConnectionTimeout(int iTryTimes, boolean bWap) {
		int time = 0;
		switch (iTryTimes) {
		case 0:
			time = AllOnlineApp.getAppConfig().getNetwork_timeout_fir() * 1000; // 第一次尝试
			break;
		case 1:
			time = AllOnlineApp.getAppConfig().getNetwork_timeout_sec() * 1000;// 第二次尝试
			break;
		case 2:
		default:
			time = AllOnlineApp.getAppConfig().getNetwork_timeout_thir() * 1000;// 第三次尝试以及其他默认
			break;
		}

		if (bWap) {
			// WAP环境超时时间是普通的3倍
			time *= 3;
		}

		return time;
	}

	public String httpToServerRequestOrUpData(String requestUrl, String server,
			Result response, boolean bHttps, long... time) {
		long t;
		if(time == null || time.length <= 0 || time[0] <= 0) {
			t = System.currentTimeMillis() / 1000;
		} else {
			t = time[0];
		}
		String content = httpToServerRequestOrUpData(requestUrl,server, response, false, t,false,null,bHttps);
		//检测 errcode是否为 ERR_INVALID_SESSION
		try{
			JSONObject json = new JSONObject(content);
			int code = json.optInt("errcode");
//			if(code == HttpError.ERR_INVALID_SESSION) {
//				//AppUtil.restartApp(AllOnlineApp.getInstantce());
//				AppUtil.showWeinxinUndindDialog(AllOnlineApp.getInstantce().getApplicationContext());
//			}
//			else if(code == HttpError.ERR_EXIRPED_SESSION){
//				AppUtil.restartCommunityOnly(AllOnlineApp.getInstantce().getApplicationContext());
//			}

		} catch (Exception e) {
			Log.e("api client", e.getMessage());
		}

		return content;
	}

	/**
	 * 网络数据上传或请求
	 * WAP环境直接使用域名请求数据,并且不做重试。
	 * 如果失败域名重试三次，相应超时时间也跟着变化
	 * */
	public String httpToServerRequestOrUpData(String requestUrl, String server,
			Result response, boolean bEncrypt, long t, boolean isPost,
			HttpEntity params, boolean bHttps, Header... headers) {
		String resultJson = null;
//		if (CommonUtil.isEmptyTrimStringOrNull(server)){
//			server = AllOnlineApp.sRespDomainAdd.domainMain;
//        }
		if (t <= 0) {
			t = System.currentTimeMillis() / 1000;
		}

		//重试次数
        int iMaxTryTimes = MAX_TRY_TIMES;
		if(isLongSoTimeInterface(requestUrl)){
			//一些特殊接口，只执行一次，超时设置长一些
			iMaxTryTimes = 1;
		}
        long httpSeq = getHttpSeq();

		// WAP网络
		if (NetworkUtil.isWap(mContext) > 0) {
			iMaxTryTimes = 1;
			response.isLastTimeIpQuery = true;
			resultJson = tryToRequestOrUpData(requestUrl, server, null,
					response, bEncrypt, t, isPost, params, bHttps, iMaxTryTimes, httpSeq, headers);
			// WAP环境不做重试，直接用域名请求一次，无论是否成功都直接退出
			if (!StringUtil.isTrimEmpty(resultJson)) {
				return resultJson;
			} else {
				return "{}";
			}
			//return resultJson == null ? "{}" : resultJson;
		}
		resultJson = tryToRequestOrUpData(requestUrl, server, null,
				response, bEncrypt, t, isPost, params, bHttps, iMaxTryTimes, httpSeq, headers);
		// 域名重试三次
		if (!StringUtil.isTrimEmpty(resultJson)) {
			response.isLastTimeIpQuery = true;
			return resultJson;
		} else {
			String defaultIp = getDefaultIp(server);
			if(defaultIp != null) {
				// 含有默认域名，使用默认域名重试
				resultJson = tryToRequestOrUpData(requestUrl, server, defaultIp,
						response, bEncrypt, t, isPost, params, bHttps, iMaxTryTimes, httpSeq, headers);
				if (!StringUtil.isTrimEmpty(resultJson)) {
					response.isLastTimeIpQuery = true;
					return resultJson;
				}
			}
			return "{}";
		}
	}

	/**
	 *
	 * @param requestUrl
	 * @param server
	 * @param ip  ip不为null时使用ip重试
	 * @param myResponse
	 * @param bEncrypt
	 * @param t
	 * @param isPost
	 * @param params
	 * @param bHttps
	 * @param maxTry
     * @param httpSeq
     * @param headers
     * @return
     */
	private String tryToRequestOrUpData(String requestUrl, String server,String ip, Result myResponse, boolean bEncrypt, long t,
			boolean isPost, HttpEntity params, boolean bHttps, int maxTry, long httpSeq, Header... headers) {
		boolean bWap = false;
		String wholeUrl = null;
		String resultJson = null;
		String methodName = null;
		if (myResponse == null) {
			myResponse = new Result();
		}
		if (PerformanceReportManager.getInstance(mContext).isOpenAndUpload()
				&& !StringUtil.isTrimEmpty(requestUrl) && requestUrl.contains("&")) {
			methodName = requestUrl.substring(1, requestUrl.indexOf("&"));
		}
		myResponse.server = server;
		myResponse.ip = ip;
		/**
		 * 是否最后一个ip
		 */
		boolean isLastTime = myResponse.isLastTimeIpQuery;
		requestUrl = addOtherParameterToUrl(server, requestUrl, ip, t, httpSeq);
		for (int i = 0; i < maxTry; i++) {
			if (isLastTime && i == maxTry - 1) {
				// 最后一个ip，且是最后一次请求， 置为true, 在抛出异常时会立即上报信息
				myResponse.isLastTimeIpQuery = true;
			} else {
				myResponse.isLastTimeIpQuery = false;
			}
			if (NetworkUtil.isWap(mContext) > 0) {
				bWap = true;
			} else {
				bWap = false;
			}
			//根据网络环境获取完整的请求URL。请求的过程中可能网络环境已经改变,所以每次尝试时都获取一次网络类型和URL
			wholeUrl = getWholeRequestUrl(server, requestUrl, ip, bHttps, bWap);
			if (Constant.IS_DEBUG_MODE){
				System.out.println("----------times: " + i + " request URL: " + wholeUrl);
			}
			myResponse.debugUrl = wholeUrl;

			if(wholeUrl.contains("method=bycaronline") || wholeUrl.contains("auth/access_token")){
				//每次登录爱车安或者社区都写进日志
				String fileMethodLine = "";
				fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
				fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
				fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
				GoomeLog.getInstance().logE(fileMethodLine, " SERVER: " + server + "URL: " + wholeUrl,0);
			}
			else{
				String fileMethodLine = "";
				fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
				fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
				fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
				GoomeLog.getInstance().logD(fileMethodLine, " SERVER: " + server + "URL: " + wholeUrl);
			}

			resultJson = excuteAndParseData(wholeUrl, isPost, server, ip,
					methodName, params, bHttps, i, bEncrypt, myResponse, t,
					bWap, headers);
			if (!CommonUtil.isEmptyTrimStringOrNull(resultJson)) {
				// 获取到数据跳出重试循环
				String str = " HTTP REQUEST SUCCESS!  " + ",server:" + server
						+ ",ip:" + ip + ",request:" + wholeUrl;
				GoomeLog.getInstance().logI(TAG, str);
				break;
			}
		}
		if(Constant.IS_DEBUG_MODE) {
			System.out.println("requestUrl " + requestUrl + "-- --" + resultJson);
		}
		return resultJson;
	}

	private int iBusIpIndex = 0;
	protected String getDefaultIp(String server)
	{
		if (Constant.DOMAIN_ADDRESS_FIRST.equals(server))
		{
			int idNum = Constant.DOMAIN_ADDRESS_FIRST_IPS.length;
			if (iBusIpIndex == 0)
			{ // 随机从要给ip开始，
				iBusIpIndex = new Random().nextInt(idNum);
			}
			return Constant.DOMAIN_ADDRESS_FIRST_IPS[iBusIpIndex % idNum];
		}
		return null;
	}

	private String addOtherParameterToUrl(String server, String requestUrl, String ip, long t, long httpSeq)
    {
		if (TextUtils.isEmpty(requestUrl)) {
			return "";
		}
		String ver = OSUtil.getAppVersionNameNoV(mContext);
		String appid = String.valueOf(Constant.COOMIX_APP_ID);
		String n = OSUtil.getUdid(mContext);
		String lang = SettingDataManager.language;
		String source = Constant.NEW_VERSION_TAG;
		if (!requestUrl.contains("&time=")) {
			requestUrl = requestUrl + "&time=" + t;
		}
		if (!requestUrl.contains("&sign=")) {
			String sign = OSUtil.toMD5(t + Constant.PRIVATE_KEY + n + ver);
			requestUrl = requestUrl + "&sign=" + sign;
		}
		if (!requestUrl.contains("&n=")) {
			requestUrl = requestUrl + "&n=" + n;
		}
		if (!requestUrl.contains("&appver=")) {
			requestUrl = requestUrl + "&appver=" + ver;
		}
		if (!requestUrl.contains("&appid=")) {
			requestUrl = requestUrl + "&appid=" + appid;
		}
		if (!requestUrl.contains("&os=android")) {
			requestUrl = requestUrl + "&os=android";
		}
		if (!requestUrl.contains("&access_type=inner")) {
			requestUrl = requestUrl + "&access_type=inner";
		}
		if (!requestUrl.contains("&lang=")) {
			requestUrl = requestUrl + "&lang="+lang;
		}
		if (!requestUrl.contains("&source=")) {
			requestUrl = requestUrl + "&source="+ source;
		}
        if(!requestUrl.contains("&http_seq="))
        {
            requestUrl = requestUrl + "&http_seq=" + httpSeq;
        }
		if(!requestUrl.contains("&apptype="))
		{
			requestUrl = requestUrl + "&apptype=" + APP_TYPE;
		}
		if(!requestUrl.contains("&lat=")) {
			requestUrl = requestUrl + "&lat=" + getLatLng()[0];
		}
		if(!requestUrl.contains("&lng=")) {
			requestUrl = requestUrl + "&lng=" + getLatLng()[1];
		}
		if(!requestUrl.contains("&vercode=")) {
			int versionCode = 1;
			try {
				versionCode = mContext.getPackageManager().getPackageInfo(
						mContext.getPackageName(), 0).versionCode;
			} catch (Exception e) {
				CrashReport.postCatchedException(e);
				e.printStackTrace();
			}
			requestUrl = requestUrl + "&vercode=" + versionCode;
		}
		if(!requestUrl.contains("&appid=")) {
			requestUrl = requestUrl + "&appid=" + Constant.COOMIX_APP_ID;
		}

		//判断是否是加密接口，如果是，则对参数进行加密
		if (!requestUrl.contains("&sec=") || requestUrl.contains("&telsec="))
		{
			//判断是否是加密接口，如果是，则对参数进行加密
			if(isEncryptInterface(requestUrl))
			{
				// 上传参数加密
				String encryptStr = OSUtil.toMD5(t + ENCRYPT_TEXT + ENCRYPT_PACKAGE_NAME);
				String[] str = split2(requestUrl, "?");
				if(str != null)
				{
					requestUrl = str[0] + "?" + getSecEncryptInfo(str[1], encryptStr, COMMUNITY_COOMIX_VERSION);
				}
				else
				{
					String fileMethodLine = "";
					fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
					fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
					fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
					GoomeLog.getInstance().logE(fileMethodLine, "split2 error on security the method", 0);
				}
			}
		}
		return requestUrl;
    }
	private boolean isGoodJson(String json) {
		if (StringUtils.isBlank(json)) {
			return false;
		}
		try {
			new JsonParser().parse(json);
			//Log.i("rectsearch","good json");
			return true;
		} catch (JsonParseException e) {
			//Log.i("rectsearch","bad json");
			return false;
		}
	}
	protected double[] getLatLng() {
		double lat = 0;
		double lng = 0;
		AMapLocation location = AllOnlineApp.getCurrentLocation();
		if(location != null) {
			lat = location.getLatitude();
			lng = location.getLongitude();
		}
		return new double[]{lat,lng};
	}

    /***URL前面部分的拼接。添加http头，以及决定用IP或域名---*/
    private String getWholeRequestUrl(String server, String requestUrl, String ip,  boolean bHttps, boolean bWap)
    {
        if (TextUtils.isEmpty(requestUrl))
        {
            return "";
        }
		if(CommunityUtil.isEmptyTrimStringOrNull(ip) || bWap) {// ip为空，则使用域名访问
			return getDN(bHttps, server) + requestUrl;
		} else {// 否则使用ip进行访问
			return getDN(bHttps, ip)+ requestUrl;
		}
    }

	private String excuteAndParseData(String url, boolean isPost,String server, String ip, String methodName, HttpEntity params,
			boolean bHttps, int num, boolean bEncrypt, Result myResponse,long t, boolean bWap, Header... headers) {
		HttpResponse httpResponse = null;
		String content = null;
		boolean bUpload = (params != null);
		long iStartTime = 0;
		StringBuffer stringBufferHeader = null;
		long iEndTime = 0;
		long upSize = 0;
		int iNetworkType = -1;

		if (bUpload) {
			upSize = params.getContentLength();
		}
		int conTimeout;
		int soTimeout;
		if(isLongSoTimeInterface(url)){
			//一些特殊接口，只执行一次，超时设置长一些
			conTimeout = 15000;
			soTimeout = 30000;
		}else {
			conTimeout = getHttpConnectionTimeout(num, bWap);
			soTimeout = getHttpSOTimeout(num, upSize, bWap);
		}
		if (PerformanceReportManager.getInstance(mContext).isOpenAndUpload()) {
			iStartTime = System.currentTimeMillis();
			iNetworkType = NetworkUtil.getInstance().getDetailNetworType(mContext);
			stringBufferHeader = new StringBuffer();
		}

		try {
			if (isPost) {
				// post
				httpResponse = excutePost(mContext, url, server, params,bHttps,conTimeout, soTimeout,  stringBufferHeader,headers);
			} else {
				// get
				httpResponse = excute(mContext, url, server, bHttps,conTimeout, soTimeout, stringBufferHeader, headers);
			}
			iEndTime = System.currentTimeMillis();
			if (httpResponse != null && httpResponse.getStatusLine() != null) {
				myResponse.statusCode = httpResponse.getStatusLine().getStatusCode();
				//System.out.println("response.stateCode = " + myResponse.statusCode);
				InputStream is = AndroidHttpClient.getUngzippedContent(httpResponse.getEntity());
				if (is != null) {
					content = convertStreamToString(is);
				}
				if(isResultEncryptInterface(url) && !isGoodJson(content)){
					//Log.i("rectsearch","content = " + content);
					// 加密密文16位
					String encryptStr = OSUtil.toMD5(t + ENCRYPT_TEXT + ENCRYPT_PACKAGE_NAME);
					String ver = BaseApiClient.COMMUNITY_COOMIX_VERSION;
					try {
						content = security.decodeProcess(content, encryptStr, ver);
					}catch (Exception e){
						e.printStackTrace();
					}
					//Log.i("rectsearch","content1 = " + content);
				}
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					myResponse.success = true;
				} else {
					myResponse.success = false;
					throw new httpStateException("httpStateCode异常");
				}
				content = parseResult(content, bEncrypt, t, url, myResponse);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (iEndTime <= 0) {
				iEndTime = System.currentTimeMillis();
			}
			processException(myResponse, e, content, stringBufferHeader);
			if (e instanceof apiReturnFalseException) {
				content = "{}";
			} else {
				content = null;
			}
		}
		// 内部会判断是否已经放开性能数据收集
		savePerformanceInfo(methodName, url, stringBufferHeader, iNetworkType,iStartTime, iEndTime, bUpload,
				getHttpDataLength(params, httpResponse), myResponse.errcode);

		return content;
	}

	private String parseResult(String content, boolean encrypt, long t,
			String realRequestUrl, Result response)
			throws apiReturnIllegalFormatException, apiHtmlException,
			JSONException, apiReturnFalseException {
		if (content != null) {
			content = content.trim();
		}
		if (content == null || content.length() == 0) {
			throw new apiReturnIllegalFormatException("接口返回格式异常");
		} else if (content.contains("unknow") && content.contains("method")) {
			throw new JSONException("unknow method");
		} else if (content.startsWith("<!doctype html")
				|| content.startsWith("<html")
				|| content.startsWith("?<!doctype html")
				|| content.startsWith("<script") || content.startsWith("<meta")) {
			throw new apiHtmlException("接口返回html代码");
		}
//		if (content != null && content.length() > 0 && content.startsWith("{")
//				&& content.endsWith("}") && content.contains("success")
//				&& content.contains("errcode") && content.contains("msg")
//				&& content.contains("data")) {
//			// 返回是明文，则不管是否该接口需要解密，都不解密
		//		}
//		if (!(content != null && content.length() > 0
//				&& content.startsWith("{") && content.endsWith("}")
//				&& content.contains("success") && content.contains("errcode")
//				&& content.contains("msg") && content.contains("data"))) {
//			throw new apiReturnIllegalFormatException("接口返回格式异常");
//		}
		JSONObject json = new JSONObject(content);
		response.success = json.optBoolean("success");
		response.statusCode = json.optInt("errcode");
		response.errorMessage = json.optString("msg");

		if (!response.success)
		{
			switch (response.statusCode)
			{
				case Result.ERRCODE_LONGIN_TWO_DEVICES:
				case Result.ERRCODE_LOGIN_TICKET_ERROR:
				case Result.ERROR_ACCOUNT_OR_PASSWORD_INVAID:
					String fileMethodLine = "";
					fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
					fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
					fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
					GoomeLog.getInstance().logE(fileMethodLine, "LOGOUT IP: " + response.ip + " SERVER: " + response.server + "response url " + response.debugUrl + " content: " + content, 0);
					if(response.statusCode == Result.ERROR_ACCOUNT_OR_PASSWORD_INVAID)
					{
						clearLocalToken();
					}
					break;

				default:
					throw new apiReturnFalseException("接口返回false异常");
			}
		}

		return content;
	}

	private void clearLocalToken()
	{
		//只要账户或密码错误，就先清除掉
		try
		{
			GoomeLog.getInstance().logE("BaseApiClient", "clear local token,account...STRAT! Account: " + AllOnlineApp.sAccount, 0);
			AllOnlineApp.sAccount = "";
			AllOnlineApp.sPassword = "";
			if(AllOnlineApp.sToken != null)
			{
				AllOnlineApp.sToken.access_token = "";
			}
			PreferenceUtil.commitString("TOKEN", "");
			PreferenceUtil.commitString("ACCOUNT", "");
			PreferenceUtil.commitString("PWD", "");
			ExperienceUserUtil.clearUserJsonFromSp(mContext);

			GoomeLog.getInstance().logE("BaseApiClient", "clear local token,account...END! Reset the Account: " + AllOnlineApp.sAccount, 0);
		}
		catch (Exception e)
		{}
	}

	private long getHttpDataLength(HttpEntity params, HttpResponse resp) {
		long length = 0;
		if (resp != null && resp.getEntity() != null) {
			length = resp.getEntity().getContentLength();
		} else if (params != null) {
			length = params.getContentLength();
		}
		return length < 0 ? 0 : length;
	}

	private void savePerformanceInfo(String methodName, String realUrl, StringBuffer sbHeaders, int iNetworkType, long iStartTime, long iEndTime, boolean bUpload, long size, int errorCode)
	{
		if(PerformanceReportManager.getInstance(mContext).isOpenAndUpload())
		{
			NetPerformanceDB netPerformanceDB = new NetPerformanceDB();
			netPerformanceDB.setStarttime(String.valueOf(iStartTime));
			netPerformanceDB.setTimecost((int)(iEndTime - iStartTime));
			netPerformanceDB.setNameid(methodName);
			netPerformanceDB.setErrorcode(errorCode);
			netPerformanceDB.setDetail(realUrl);
			netPerformanceDB.setNetworktype(iNetworkType);
			netPerformanceDB.setHttpheader(sbHeaders != null ? sbHeaders.toString() : "");
			if(bUpload)
			{
				netPerformanceDB.setUpsize(String.valueOf(size));
			}
			else
			{
				netPerformanceDB.setDownsize(String.valueOf(size));
			}
			PerformanceReportManager.getInstance(mContext).saveDataAndUploadByCondition(netPerformanceDB);
		}
	}

	/**
	 * 社区接口调用 ，增加sign
	 * @param t
	 * @return
     */
	protected String getUrlSign(long t) {
		String n = OSUtil.getUdid(mContext);
		String ver = COMMUNITY_COOMIX_VERSION;
		String sign;
		try {
			sign = security.hashProcess(t + Constant.PRIVATE_KEY + n + ver, ver, mContext);
		} catch (Throwable e) {
			CrashReport.postCatchedException(e);
			if(Constant.IS_DEBUG_MODE){
				e.printStackTrace();
			}
			return "";
		}
		return "&sign=" + sign + "&ver=" + ver + "&t=" + t;
	}

	private static long iHttpSeq = -1;

	public synchronized static long getHttpSeq() {
		if (iHttpSeq <= 0) {
			iHttpSeq = PreferenceUtil.getLong(Constant.PREFERENCE_HTTP_SEQ, 0);
		}

		++iHttpSeq;

		PreferenceUtil.commitLong(Constant.PREFERENCE_HTTP_SEQ, iHttpSeq);

		return iHttpSeq;
	}

	private boolean isEncryptInterface(String requestUrl)
	{
		final String SPLIT_METHOD = "method=";
		if(!TextUtils.isEmpty(requestUrl) && requestUrl.contains(SPLIT_METHOD) && requestUrl.contains("?"))
		{
			try
			{
				String method = getMethodsName(requestUrl);
				String module = requestUrl.substring(0, requestUrl.indexOf("?"));
				module = module.substring(module.lastIndexOf("/") + 1, module.length());
				String moduleMethod = module + "#" + method;
				if(encryptMethods.contains(moduleMethod))
				{
					return true;
				}
			}
			catch (Exception e)
			{
				String debugMsg = "Exception: " + CommonUtil.getStackTrace(e);
				String fileMethodLine = "";
				fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
				fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
				fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
				GoomeLog.getInstance().logE(fileMethodLine, debugMsg, 0);
			}
		}
		return false;
	}

	private boolean isLongSoTimeInterface(String requestUrl)
	{
		final String SPLIT_METHOD = "method=";
		if(!TextUtils.isEmpty(requestUrl) && requestUrl.contains(SPLIT_METHOD) && requestUrl.contains("?"))
		{
			try
			{
				String method = getMethodsName(requestUrl);
				String module = requestUrl.substring(0, requestUrl.indexOf("?"));
				module = module.substring(module.lastIndexOf("/") + 1, module.length());
				String moduleMethod = module + "#" + method;
				if(longSotimeMethods.contains(moduleMethod))
				{
					return true;
				}
			}
			catch (Exception e)
			{
				String debugMsg = "Exception: " + CommonUtil.getStackTrace(e);
				String fileMethodLine = "";
				fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
				fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
				fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
				GoomeLog.getInstance().logE(fileMethodLine, debugMsg, 0);
			}
		}
		else if(isResultEncryptInterface(requestUrl)){
			return true;
		}
		return false;
	}

	private boolean isResultEncryptInterface(String requestUrl)
	{
		if(!TextUtils.isEmpty(requestUrl) && requestUrl.contains("?"))
		{
			try
			{
				String module = requestUrl.substring(0, requestUrl.indexOf("?"));
				module = module.substring(module.indexOf("//") + 2,module.length());
				module = module.substring(module.indexOf("/"),module.length());
				if(resultEncryptMethods.contains(module))
				{
					return true;
				}
			}
			catch (Exception e)
			{
				String debugMsg = "Exception: " + CommonUtil.getStackTrace(e);
				String fileMethodLine = "";
				fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
				fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
				fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
				GoomeLog.getInstance().logE(fileMethodLine, debugMsg, 0);
			}
		}
		return false;
	}

	private String getMethodsName(String requestUrl)
	{
		final String SPLIT_METHOD = "method=";
		String method = "";
		try
		{
			if (requestUrl.contains(SPLIT_METHOD) && requestUrl.contains("?"))
			{
				// 使用String.split()会出现很多系统库错误（正则 导致的），10/25修改为字符串截断
				String[] methods = split2(requestUrl, SPLIT_METHOD);
				if (methods != null && methods.length > 1)
				{
					method = methods[1].substring(0, methods[1].indexOf("&"));
				}
			}
		}
		catch (Exception e)
		{
			String debugMsg = "Exception: " + CommonUtil.getStackTrace(e);
			String fileMethodLine = "";
			fileMethodLine += "File: " + Thread.currentThread().getStackTrace()[2].getFileName();//文件名
			fileMethodLine += ",Method: " + Thread.currentThread().getStackTrace()[2].getMethodName();//函数名
			fileMethodLine += ",Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber();//行号
			GoomeLog.getInstance().logE(fileMethodLine, debugMsg, 0);
		}
		return  method;
	}

	private String[] split2(String str, String split)
	{
		String[] methods = new String[2];
		int indexMethod = str.indexOf(split);
		methods[0] = str.substring(0, indexMethod);
		methods[1] = str.substring(indexMethod + split.length());
		return methods;
	}

	private String getSecEncryptInfo(String str, String key, String ver)
	{
		String[] strs = str.split("&");
		StringBuilder result = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		if (strs != null)
		{
			for (int i = 0; i < strs.length; i++)
			{
				if (strs[i] != null && strs[i].length() > 0)
				{
					if (strs[i].startsWith("ver=") || strs[i].startsWith("method="))
					{
						result.append(strs[i] + "&");
					}
					else
					{
						sb.append(strs[i] + "&");
					}
				}
			}
			if (sb.length() > 0)
			{
				sb.deleteCharAt(sb.length() - 1);
			}
			result.append("sec=");
			try
			{
				String s = sb.toString();
				String encode = security.getSecInfo(s, key, ver);
				result.append(URLEncoder.encode(encode, "UTF-8"));
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		return result.toString();
	}
}