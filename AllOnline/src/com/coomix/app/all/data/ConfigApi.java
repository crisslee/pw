package com.coomix.app.all.data;

import com.coomix.app.all.model.response.RespAppConfig;

import java.util.Map;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * 获取配置api
 * Created by ssl on 2018/8/29.
 */

public interface ConfigApi {
    /**
     * 获取配置
     *
     * @return
     */
    @GET("/1/config?method=get")
    Flowable<RespAppConfig> getAppConfig(@QueryMap Map<String, String> commonParam,
                                         @Query("production") String production,
                                         @Query("devname") String devname,
                                         @Query("osver") int osver,
                                         @Query("osextra") String osextra,
                                         @Query("extra") String extra,
                                         @Query("access_by") String netType);
}
