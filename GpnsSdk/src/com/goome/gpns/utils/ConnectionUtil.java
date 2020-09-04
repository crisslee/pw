package com.goome.gpns.utils;

import com.android.goome.volley.Request;
import com.android.goome.volley.RequestQueue;
import com.android.goome.volley.Response.ErrorListener;
import com.android.goome.volley.Response.Listener;
import com.android.goome.volley.VolleyError;
import com.android.goome.volley.toolbox.StringRequest;
import com.android.goome.volley.toolbox.Volley;
import com.goome.gpns.GPNSInterface;
import com.goome.gpns.service.ResultListener;

public class ConnectionUtil {
	private static RequestQueue requestQueue;

	static {
		requestQueue = Volley.newRequestQueue(GPNSInterface.appContext);
	}
	
	public static void requestHttpData(Request<?> request) {
		requestQueue.add(request);
	}

	public static void requestHttpData(String url,
			final ResultListener<String> resultListener) {
		StringRequest request = new StringRequest(url, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				resultListener.onSuccessed(response);
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				resultListener.onFailed(error.toString());
			}
		});
		requestHttpData(request);
	}
	
	public static void requestHttpData(String url,
			Listener<String> listener,ErrorListener errorListener) {
		StringRequest request = new StringRequest(url, listener, errorListener);
		requestHttpData(request);
	}
	
}
