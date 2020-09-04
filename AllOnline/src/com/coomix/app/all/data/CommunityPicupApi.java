package com.coomix.app.all.data;

import com.coomix.app.all.model.response.RespCommunityImageInfo;

import java.util.Map;

import io.reactivex.Flowable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * 社区图片上传的网络api
 * Created by ssl on 2018/1/3.
 */

public interface CommunityPicupApi
{
    /**
     * 上传图片
     * @param commonParam
     * @param body
     * @return
     */
    @POST("/1/picture?method=upload")
    Flowable<RespCommunityImageInfo> uploadPicture(@QueryMap Map<String, String> commonParam, @Body RequestBody body);
}
