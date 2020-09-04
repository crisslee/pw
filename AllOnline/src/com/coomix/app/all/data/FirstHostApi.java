package com.coomix.app.all.data;

import com.coomix.app.all.model.bean.RespDomainAdd;

import io.reactivex.Flowable;
import retrofit2.http.GET;

/**
 * File Description：请求域名列表--以文件形式返回数据
 *
 * @author ssl
 * @since 2018/8/10.
 */
public interface FirstHostApi {

    /**
     * 获取域名
     *
     * @return stream
     */
    @GET("/appserverlist")
    Flowable<RespDomainAdd> getFirstHost();

}
