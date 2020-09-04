package com.coomix.app.all.data;

import com.coomix.app.all.model.response.RespGoogleAddress;
import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 获取广告的相关的api
 * Created by ssl on 2018/8/10.
 */
public interface GoogleAddressApi {
    /**
     * 请求地址，需要将坐标转换为google坐标系
     */
    @GET("/maps/api/geocode/json?sensor=true")
    Flowable<RespGoogleAddress> requestGoogleAddress(@Query("language") String language,
        @Query("latlng") String latLng);
}
