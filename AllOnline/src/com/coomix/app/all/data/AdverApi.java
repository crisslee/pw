package com.coomix.app.all.data;

import com.coomix.app.all.model.response.RespAdver;
import com.coomix.app.all.model.response.RespBase;

import java.util.Map;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * 获取广告的相关的api
 * Created by ssl on 2018/8/10.
 */

public interface AdverApi {
    /**
     * 获取全部广告
     *
     * @return
     */
    @GET("/v1/bus/mbcommonservice?method=getallad")
    Flowable<RespAdver> getAllAd(@QueryMap Map<String, String> commonParam,
                                 @Query("login_name") String loginName,
                                 @Query("width") int width,
                                 @Query("height") int height,
                                 @Query("lat") double lat,
                                 @Query("lng") double lng,
                                 @Query("citycode") String citycode);


    /**
     * 点击广告上报
     *
     * @return
     */
    @GET("/v1/bus/mbcommonservice?method=upload_ad_click")
    Flowable<RespBase> clickAdverReport(@QueryMap Map<String, String> commonParam,
                                        @Query("login") String phoneNumber,
                                        @Query("citycode") String citycode,
                                        @Query("type") int type,
                                        @Query("ad_id") int ad_id);
}
