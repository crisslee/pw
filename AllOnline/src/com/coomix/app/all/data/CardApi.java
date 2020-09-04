package com.coomix.app.all.data;

import com.coomix.app.all.model.response.RespCardDataInfo;
import com.coomix.app.all.model.response.RespDataBundles;
import com.coomix.app.all.model.response.RespRechargeDataBundle;
import io.reactivex.Flowable;
import java.util.Map;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/3/18.
 */
public interface CardApi {

    /**
     * 查询卡和流量信息
     */
    @GET("1/cardpool/charge?method=wechatQueryCard")
    Flowable<RespCardDataInfo> queryCardData(@Query("card_code") String cardCode,
        @QueryMap Map<String, String> commonParam);

    /**
     * 查询卡能充值的类型/套餐
     */
    @GET("1/cardpool/charge?method=wechatQueryDataBundles")
    Flowable<RespDataBundles> queryBundles(@Query("msisdn") String msisdn, @QueryMap Map<String, String> commonParam);

    /**
     * 流量充值，获取order_id
     */
    @GET("1/cardpool/charge?method=AppPurchaseDataBundle")
    Flowable<RespRechargeDataBundle> buyDataBundle(@Query("msisdn") String msisdn, @Query("bundleid") long bundleId,
        @Query("platform") String platform, @QueryMap Map<String, String> commonParam);
}
