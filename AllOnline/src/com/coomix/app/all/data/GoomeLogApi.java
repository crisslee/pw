package com.coomix.app.all.data;

import com.coomix.app.all.model.response.RespBase;

import java.util.Map;

import io.reactivex.Flowable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Goome日志api
 * Created by ssl on 2018/8/29.
 */

public interface GoomeLogApi {
    /**
     * 获取配置
     *
     * @return
     */
    @POST("/1/log?method=upload")
    Flowable<RespBase> uploadLogFile(@QueryMap Map<String, String> commonParam,
                                     @Query("production") String production,
                                     @Query("devname") String devname,
                                     @Query("osver") int osver,
                                     @Query("osextra") String osextra,
                                     @Query("extra") String extra,
                                     @Query("access_by") String netType,
                                     @Body RequestBody body);
}
