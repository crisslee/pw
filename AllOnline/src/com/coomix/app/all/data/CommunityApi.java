package com.coomix.app.all.data;

import com.coomix.app.all.model.response.RespActCategory;
import com.coomix.app.all.model.response.RespActDetail;
import com.coomix.app.all.model.response.RespActList;
import com.coomix.app.all.model.response.RespActOrderInfo;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.model.response.RespBindWechat;
import com.coomix.app.all.model.response.RespCommunityUser;
import com.coomix.app.all.model.response.RespLoginCommunity;
import com.coomix.app.all.model.response.RespMyActList;
import com.coomix.app.all.model.response.RespPushAd;
import com.coomix.app.all.model.response.RespRefreshOrder;
import com.coomix.app.all.model.response.RespRegisterAct;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Map;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * 社区的网络api
 * Created by ly on 2017/5/17.
 */

public interface CommunityApi {

    /**
     * 1.12.3	确认订单是否已经支付
     *
     * @param commonParam
     * @param order_id
     * @return
     */
    @GET("1/wallet?method=refreshOrder")
    Flowable<RespRefreshOrder> refreshOrder(@QueryMap Map<String, String> commonParam,
                                            @Query("order_id") long order_id);


    /**
     * 1.12.6	查看交易信息
     *
     * @param commonParam
     * @param type        交易类型(1=红包，2=活动)
     * @param id          交易ID
     * @return
     */
    @GET("1/wallet?method=getTradeInfo")
    Flowable<JsonObject> getTradeInfo(@QueryMap Map<String, String> commonParam,
                                      @Query("type") int type,
                                      @Query("id") long id);

    /**
     * 获取微信的token
     *
     * @param url  获取的url
     * @param code 微信返回的code
     * @return
     */
    @GET
    Flowable<JsonObject> getWeixinAccessToken(@Url String url,
                                              @Query("code") String code);

    @GET("1/login?method=bindwxchat")
    Flowable<RespBindWechat> bindWeChat(@QueryMap Map<String, String> commonParam,
                                        @Query("wxcode") String wxcode,
                                        @Query("cover") boolean cover,
                                        @Query("access_token") String access_token,
                                        @Query("cid") String cid,
                                        @Query("openid") String openid);

    /**
     * 退出社区
     */
    @GET("1/login?method=logout")
    Flowable<JsonObject> logoutCommunity(@Query("ticket") String ticket, @Query("cid") String cid,
                                         @QueryMap Map<String, String> commonParam);

    /**
     * 查看我报名的信息
     *
     * @param commonParam
     * @param aid         订单id，取消报名需要传order_id
     * @param maptype
     * @return
     */
    @GET("1/activity?method=getApplyInfo")
    Flowable<JsonObject> getMyRegisteredAct(@QueryMap Map<String, String> commonParam,
                                            @Query("aid") long aid,
                                            @Query("maptype") String maptype);

    /**
     * 活动报名或信息修改
     *
     * @param commonParam
     * @param type        0:取消报名；1:活动报名；2：信息修改
     * @param aid
     * @param order_id    订单id，取消报名需要传order_id
     * @param regInfo
     * @return
     */
    @GET("1/activity?method=apply")
    Flowable<RespRegisterAct> registerAct(@QueryMap Map<String, String> commonParam,
                                          @Query("type") int type,
                                          @Query("aid") long aid,
                                          @Query("order_id") long order_id,
                                          @QueryMap Map<String, String> regInfo);

    /**
     * 获取推送广告
     *
     * @param commonParam
     * @return
     */
    @GET("1/login?method=checkpopup")
    Flowable<RespPushAd> getPushedAd(@QueryMap Map<String, String> commonParam);


    /**
     * 上传位置数据
     *
     * @param url
     * @param commonPara
     * @return
     */
    @GET
    Flowable<JSONObject> uploadLocation(@Url String url,
                                        @QueryMap Map<String, String> commonPara);

    /**
     * 获取活动分类
     *
     * @param citycode
     * @return
     */
    @GET("1/activity?method=getCategoryList")
    Flowable<RespActCategory> getCategorys(@QueryMap Map<String, String> commonPara, @Query("citycode") String citycode);

    /**
     * 获取特定分类的活动列表
     *
     * @param commonParam
     * @param type
     * @param citycode
     * @param category_id
     * @param last_pointer
     * @param last_id
     * @param num
     * @return
     */
    @GET("1/activity?method=getList")
    Flowable<RespActList> getActivityList(@QueryMap Map<String, String> commonParam,
                                          @Query("type") int type,
                                          @Query("citycode") String citycode,
                                          @Query("category_id") int category_id,
                                          @Query("last_pointer") double last_pointer,
                                          @Query("last_id") String last_id,
                                          @Query("num") long num);

    /**
     * 获取用户信息
     *
     * @param commonParam
     * @param uid
     * @return
     */
    @GET("/1/user?method=detailInfo")
    Flowable<RespCommunityUser> getUserDetailInfo(@QueryMap Map<String, String> commonParam,
                                                  @Query("uid") String uid);

    /**
     * 修改用户信息
     *
     * @param commonParam
     * @param mapInfos
     * @return
     */
    @GET("1/user?method=modify")
    Flowable<RespBase> userModify(@QueryMap Map<String, String> commonParam,
                                  @QueryMap Map<String, Object> mapInfos);

    /**
     * 绑定gpns推送cid
     *
     * @param commonParam
     * @param ticket
     * @param cid
     * @return
     */
    @GET("1/user?method=bindPush")
    Flowable<RespBase> bindChannelId(@QueryMap Map<String, String> commonParam,
                                     @Query("ticket") String ticket,
                                     @Query("cid") String cid);

    @GET("1/user?method=senduserinfo")
    Flowable<RespBase> senUserInfo(@QueryMap Map<String, String> commonParam,
                                   @Query("ticket") String ticket,
                                   @Query("uid") String uid,
                                   @Query("acttype") String acttype,
                                   @Query("recordhis") String recordhis);

    /**
     * 登录社区
     *
     * @param commonParam
     * @param access_token
     * @param cid
     * @param loginname
     * @return
     */
    @GET("1/login?method=bycaronline")
    Flowable<RespLoginCommunity> loginCommunity(@QueryMap Map<String, String> commonParam,
                                                @Query("access_token") String access_token,
                                                @Query("cid") String cid,
                                                @Query("loginname") String loginname);

    /**
     * 获取活动详情
     */
    @GET("/1/activity?method=getDetail")
    Flowable<RespActDetail> getActivityDetail(@QueryMap Map<String, String> commonParam,
                                              @Query("ticket") String ticket,
                                              @Query("id") int id);

    /**
     * 获取我的活动列表
     */
    @GET("/1/activity?method=getJoinList")
    Flowable<RespMyActList> getMyActivityList(@QueryMap Map<String, String> commonParam,
                                              @Query("ticket") String ticket,
                                              @Query("last_pointer") double lastPointer,
                                              @Query("last_id") String lastId,
                                              @Query("num") int num);

    /**
     * 提交报名信息
     */
    @GET("/1/activity?method=apply")
    Flowable<RespActOrderInfo> sendSignInfo(@QueryMap Map<String, String> commonParam,
                                            @Query("ticket") String ticket,
                                            @Query("type") int type,
                                            @Query("aid") int aid,
                                            @Query("order_id") long order_id,
                                            @Query("name") String name,
                                            @Query("tel") String tel,
                                            @Query("qqorwx") String qqorwx,
                                            @Query("addr") String addr,
                                            @Query("extend_items") String extend_items);

    /**
     * 获取报名信息---网络返回信息CommunitySignedInfo还不确定格式
     */
    @GET("/1/activity?method=getApplyInfo")
    Flowable<RespBase> getMySignedInfo(@QueryMap Map<String, String> commonParam,
                                       @Query("ticket") String ticket,
                                       @Query("aid") int aid);

}
