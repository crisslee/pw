package com.coomix.app.all.data;

import com.coomix.app.all.model.response.RespPlateInfo;
import com.coomix.app.all.model.response.RespUploadImage;

import java.util.Map;

import io.reactivex.Flowable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * 安装信息上传的网络api
 * Created by ssl on 2018/1/3.
 */

public interface UploadInfoApi {
    /**
     * 上传图片
     *
     * @param commonParam
     * @param body
     * @return
     */
    @POST("/s3service?bucket=plate&method=uploadFile")
    Flowable<RespUploadImage> uploadPicture(@QueryMap Map<String, String> commonParam,
                                            @Query("access_token") String access_token,
                                            @Body RequestBody body);

    /**
     * 根据图片，做车牌号识别
     */
    @GET("/plate_recg_service?method=hyperlpr")
    Flowable<RespPlateInfo> hyperlpr(@QueryMap Map<String, String> commonParam,
                                     @Query("access_token") String access_token,
                                     @Query("plate_url") String plate_url);

    /**
     * 根据图片，做车牌号识别 ali
     */
    @GET("/plate_recg_service?method=ali")
    Flowable<RespPlateInfo> aliHyperlpr(@QueryMap Map<String, String> commonParam,
                                              @Query("access_token") String access_token,
                                              @Query("plate_url") String plate_url);
    /**
     * 录入信息提交
     */
    @POST("/install_info?method=enterInstallInfo")
    Flowable<RespPlateInfo> enterInstallInfo(@QueryMap Map<String, String> commonParam,
                                              @Query("access_token") String access_token,
                                              @Query("imei") String imei,
                                              @Query("plate_no") String plate_no,
                                              @Query("access_domain") String access_domain,
                                              @Query("plate_url") String plate_url);

    //多张图片上传
//    @Multipart
//    @POST("")
//    Flowable<RespBase> uploadImages(@PartMap() Map<String, RequestBody> files);
//    Map<String, RequestBody> map = new HashMap<>();
//            for (int i = 0; i < files.size(); i++) {
//        RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), files.get(i));
//        map.put("file" + i + "\";filename=\"" + files.get(i).getName(), requestBody);
//    }
}
