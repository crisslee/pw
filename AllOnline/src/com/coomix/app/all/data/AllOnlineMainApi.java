package com.coomix.app.all.data;

import com.coomix.app.all.model.request.ReqRenewOrder;
import com.coomix.app.all.model.response.RespAccountGroupInfo;
import com.coomix.app.all.model.response.RespAddress;
import com.coomix.app.all.model.response.RespAlarmCategoryList;
import com.coomix.app.all.model.response.RespAlarmCount;
import com.coomix.app.all.model.response.RespAlarmDetailList;
import com.coomix.app.all.model.response.RespAlarmOption;
import com.coomix.app.all.model.response.RespAllTypes;
import com.coomix.app.all.model.response.RespAreaFence;
import com.coomix.app.all.model.response.RespAuthPage;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.model.response.RespBatchAddress;
import com.coomix.app.all.model.response.RespBindedWx;
import com.coomix.app.all.model.response.RespCityList;
import com.coomix.app.all.model.response.RespDevPassInfo;
import com.coomix.app.all.model.response.RespDeviceDetailInfo;
import com.coomix.app.all.model.response.RespDeviceList;
import com.coomix.app.all.model.response.RespDeviceSetting;
import com.coomix.app.all.model.response.RespLockList;
import com.coomix.app.all.model.response.RespMode;
import com.coomix.app.all.model.response.RespNotice;
import com.coomix.app.all.model.response.RespOpenAudioRecord;
import com.coomix.app.all.model.response.RespPlatDevList;
import com.coomix.app.all.model.response.RespPlatOrder;
import com.coomix.app.all.model.response.RespProvinceList;
import com.coomix.app.all.model.response.RespQueryFence;
import com.coomix.app.all.model.response.RespRandom;
import com.coomix.app.all.model.response.RespRefrshRenewPayOrder;
import com.coomix.app.all.model.response.RespRenewWlCard;
import com.coomix.app.all.model.response.RespResponse;
import com.coomix.app.all.model.response.RespSendCmd;
import com.coomix.app.all.model.response.RespServiceProvider;
import com.coomix.app.all.model.response.RespShareLocation;
import com.coomix.app.all.model.response.RespStatistics;
import com.coomix.app.all.model.response.RespThemeAll;
import com.coomix.app.all.model.response.RespToken;
import com.coomix.app.all.model.response.RespTrackPoints;
import com.coomix.app.all.model.response.RespTypeCmds;
import com.coomix.app.all.model.response.RespUpdateInfo;
import com.coomix.app.all.model.response.RespWLCardExpire;
import com.coomix.app.all.model.response.RespWLCardInfo;
import com.coomix.app.all.model.response.RespWLCardRecharge;
import io.reactivex.Flowable;
import java.util.Map;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * 查车（汽车在线）相关的api
 * Created by ly on 2017/9/27 14:31.
 */
public interface AllOnlineMainApi {
    /**
     * 平台支付设备列表接口
     *
     * @param beginpos 本次加载偏移位置
     * @param pagesize 每页加载条数响应
     */
    @GET("/1/carol-pay?method=renewDevList")
    Flowable<RespPlatDevList> getRenewDevList(@QueryMap Map<String, String> commonParam,
        @Query("access_token") String access_token,
        @Query("beginpos") int beginpos,
        @Query("pagesize") int pagesize);

    /**
     * 预支付接口,获取订单
     */
    @POST("/1/carol-pay?method=renewOrder")
    Flowable<RespPlatOrder> getRenewOrder(@QueryMap Map<String, String> commonParam,
        @Query("access_token") String access_token,
        @Body ReqRenewOrder reqRenewOrder);

    /**
     * 校验订单
     */
    @GET("1/carol-pay?method=renewPay")
    Flowable<RespRefrshRenewPayOrder> refreshRenewPayOrder(@QueryMap Map<String, String> commonParam,
        @Query("access_token") String access_token,
        @Query("order_id") long order_id);

    @GET("1/tool/modify_pwd?method=get_bind_captcha")
    Flowable<RespBase> forgetPwValidate(@QueryMap Map<String, String> commonParam,
        @Query("access_token") String access_token,
        @Query("account") String account);

    @GET("1/tool/modify_pwd?method=set_pwd")
    Flowable<RespBase> forgetPwReset(@QueryMap Map<String, String> commonParam,
        @Query("access_token") String access_token,
        @Query("captcha") String validateNum,
        @Query("pwd") String newPw,
        @Query("account") String account);

    /**
     * 查询围栏
     */
    @GET("1/tool/efence?method=get")
    Flowable<RespQueryFence> queryFence(@QueryMap Map<String, String> commonParam,
        @Query("access_token") String access_token,
        @Query("account") String account,
        @Query("imei") String imei,
        @Query("alarm_id") String alarm_id);

    /**
     * 添加围栏
     */
    @GET("1/tool/efence?method=set")
    Flowable<RespBase> addFence(@QueryMap Map<String, String> commonParam,
        @Query("access_token") String access_token,
        @Query("imei") String imei,
        @Query("shape_type") int shape_type,
        @Query("shape_param") String shape_param,
        @Query("validate_flag") int validate_flag,
        @Query("account") String account,
        @Query("alarm_type") int alarm_type);

    /**
     *
     */
    @GET("1/tool/efence?method=switch")
    Flowable<RespBase> switchFence(@QueryMap Map<String, String> commonParam,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("imei") String imei,
        @Query("id") String id,
        @Query("validate_flag") int validate_flag,
        @Query("alarm_type") int alarm_type);

    /**
     * get auth
     */
    @GET("account?method=getAuthPages")
    Flowable<RespAuthPage> getAuthPages(@QueryMap Map<String, String> commonParam,
        @Query("access_token") String access_token);

    /**
     * 开启录音
     */
    @GET("1/voicemgmt?method=openRecord")
    Flowable<RespOpenAudioRecord> openRecord(@Query("access_token") String access_token, @Query("imei") String imei,
        @Query("record_len") int len, @QueryMap Map<String, String> commonParam);

    /**
     * 关闭录音
     */
    @GET("1/voicemgmt?method=closeRecord")
    Flowable<RespOpenAudioRecord> closeRecord(@QueryMap Map<String, String> commonParam,
        @Query("access_token") String access_token,
        @Query("imei") String imei);

    /**
     * 用户名登录
     *
     * @return token
     */
    @GET("/1/auth/access_token?method=loginByUsername")
    Flowable<RespToken> loginByUsername(@Query("account") String account,
        @Query("time") long time,
        @Query("signature") String password,
        @Query("cid") String channelId,
        @QueryMap Map<String, String> params);

    /**
     * imei登录
     *
     * @param imei imei
     * @param sig md5(md5(pwd)+time)
     * @return token
     */
    @GET("/1/auth/access_token?method=loginByImei")
    Flowable<RespToken> loginByImei(@Query("imei") String imei,
        @Query("time") long time,
        @Query("signature") String sig,
        @Query("cid") String channelId,
        @QueryMap Map<String, String> params);

    /**
     * 获取随机码
     *
     * @return random数据
     */
    @GET("/captcha?method=getCaptchaRandom")
    Flowable<RespRandom> getCaptchaRandom(@QueryMap Map<String, String> params);

    /**
     * 获取验证码
     */
    @GET("/captcha?method=applycaptcha")
    Flowable<RespBase> applyCaptcha(@Query("tel") String tel,
        @Query("telsec") String telsec,
        @QueryMap Map<String, String> params);

    /**
     * 验证码登录
     *
     * @param phone 手机号
     * @param captcha 验证码
     * @param channelId cid
     * @return token
     */
    @GET("/1/auth/access_token?method=loginByPhone")
    Flowable<RespToken> loginByPhone(@Query("phone") String phone,
        @Query("captcha") String captcha,
        @Query("cid") String channelId,
        @QueryMap Map<String, String> params);

    /**
     * 微信登录接口
     *
     * @param wxCode 微信授权code
     * @return 登录结果
     */
    @GET("/1/auth/access_token?method=loginByWechat")
    Flowable<RespToken> loginByWx(@Query("wxcode") String wxCode,
        @Query("cid") String channelId,
        @QueryMap Map<String, String> params);

    /**
     * 退出登录
     */
    @GET("/1/account/unregister")
    Flowable<RespBase> signOut(@Query("access_token") String token,
        @Query("account") String account,
        @Query("time") long time,
        @Query("channelid") String cid,
        @QueryMap Map<String, String> params);

    /**
     * 注册
     */
    @GET("/1/auth/access_token?method=app_register")
    Flowable<RespBase> appRegister(@Query("phone") String phone,
        @Query("captcha") String captcha,
        @Query("pwd") String pwd,
        @Query("name") String name,
        @QueryMap Map<String, String> params);

    /**
     * 修改密码
     */
    @GET("/1/auth/access_token?method=app_modifypwd")
    Flowable<RespBase> modifyPwd(@Query("phone") String phone,
        @Query("captcha") String captcha,
        @Query("pwd") String pwd,
        @QueryMap Map<String, String> params);

    /**
     * 查看token是否过期
     */
    @GET("/1/auth/access_token?method=refreshToken")
    Flowable<RespToken> refreshToken(@Query("access_token") String oldToken,
        @Query("cid") String cid,
        @QueryMap Map<String, String> params);

    /**
     * 绑定设备
     */
    @GET("1/account/binddevice")
    Flowable<RespBase> bindDevice(@Query("access_token") String token,
        @Query("account") String account,
        @Query("imei") String imei,
        @Query("time") long time,
        @Query("signature") String sig,
        @QueryMap Map<String, String> params);

    /**
     * 获取所有设备列表
     */
    @GET("/1/account/monitor")
    Flowable<RespDeviceList> getDeviceList(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("target") String target,
        @Query("access_token") String access_token);

    /**
     * 获取地址
     */
    @GET("/1/tool/address")
    Flowable<RespAddress> requestAddress(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("target") String target,
        @Query("access_token") String access_token,
        @Query("lat") double lat,
        @Query("lng") double lng);

    /**
     * 获取单个设备的详细信息
     */
    @GET("/1/account/devinfo")
    Flowable<RespDeviceDetailInfo> getDevDetailInfo(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("target") String imei);

    /**
     * 修改设备的详细信息
     */
    @GET("/1/account/devinfo?method=modifyUser")
    Flowable<RespBase> modifyDevDetailInfo(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("imei") String imei,
        @Query("user_name") String name,
        @Query("phone") String phone,
        @Query("sex") String number,
        @Query("owner") String owner,
        @Query("tel") String tel,
        @Query("remark") String remark);

    /**
     * 获取全部告警信息的数目
     */
    @GET("/1/tool/get_alarminfo?method=getAlarmOverviewCount")
    Flowable<RespAlarmCount> getAlarmOverviewCount(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("timestamp") long timestamp);

    /**
     * 获取账户列表
     */
    @GET("/1/account/userinfo")
    Flowable<RespAccountGroupInfo> getAccountList(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("target") String target,
        @Query("access_token") String access_token);

    /**
     * 设备搜索
     */
    @GET("/1/account/search?method=app_deviceSearch")
    Flowable<RespDeviceList> searchDeviceByKey(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("target") String target,
        @Query("access_token") String access_token,
        @Query("search_content") String search_content,
        @Query("search_type") int type);

    /**
     * 设备设置状态查询
     */
    @GET("/1/tool/efence?method=get_switch_status")
    Flowable<RespDeviceSetting> queryDeviceSetting(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("imei") String imei);

    /**
     * 设置超速报警
     */
    @GET("/1/account/devinfo?method=modifyUser")
    Flowable<RespBase> setOverspeed(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("imei") String imei,
        @Query("overspeed_flag") int overspeed_flag,
        @Query("speed") int speed);

    /**
     * 关闭出省市报警
     */
    @GET("/1/tool/efence?method=switch_area_fence")
    Flowable<RespBase> switchAreaFence(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("imei") String imei,
        @Query("id") String id,
        @Query("validate_flag") int validate_flag);

    /**
     * 设置出省市报警
     */
    @GET("/1/tool/efence?method=set_area_fence")
    Flowable<RespAreaFence> setAreaFence(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("imei") String imei,
        @Query("citycode") String citycode,
        @Query("validate_flag") int validate_flag);

    /**
     * 获取省份列表
     */
    @GET("/1/tool/efence?method=get_provinces")
    Flowable<RespProvinceList> getProvincesList(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token);

    /**
     * 获取城市列表
     */
    @GET("/1/tool/efence?method=get_cities")
    Flowable<RespCityList> getCityList(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("province") String province);

    /**
     * 设置短信提醒
     */
    @GET("/1/tool/efence?method=saveAlarmPhoneNum")
    Flowable<RespBase> setAlarmPhoneNum(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("imei") String imei,
        @Query("id") String fenceId,
        @Query("phone_num") String phone_num);

    /**
     * 批量获取地址
     *
     * @param imeiLatLng 由所有的设备的imei和经纬度组成。格式如下：imei\lng\lat,imei\lng\lat...imei\lng\lat
     */
    @GET("/1/tool/address?method=batchgetaddress")
    Flowable<RespBatchAddress> getBatchAddress(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("lnglat") String imeiLatLng);

    /**
     * 获取回放历史
     */
    @GET("/1/devices/history")
    Flowable<RespTrackPoints> history(@Query("access_token") String token,
        @Query("imei") String imei,
        @Query("account") String account,
        @Query("begin_time") long beginTime,
        @Query("end_time") long endTime,
        @Query("time") long currTime,
        @Query("limit") int limit,
        @QueryMap Map<String, String> params);

    /**
     * 逆地址解析
     */
    @GET("/1/tool/address")
    Flowable<RespAddress> reverseGeo(@Query("access_token") String token,
        @Query("account") String account,
        @Query("lat") double lat,
        @Query("lng") double lng,
        @QueryMap Map<String, String> params);

    /**
     * 获取所有类型的告警的总览
     *
     * @param imei 请求设备的告警列表时候才会携带，不携带则是请求该账号下的所有告警
     */
    @GET("/1/tool/get_alarminfo?method=getAlarmOverview")
    Flowable<RespAlarmCategoryList> getAlarmCategoryList(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("timestamp") long timestamp,
        @Query("imei") String imei);

    /**
     * 获取某个告警类型的告警数据
     *
     * @param imei 请求设备的告警数据，不携带则是请求该账号下的所有告警
     */
    @GET("/1/tool/get_alarminfo?method=getAlarmDetail")
    Flowable<RespAlarmDetailList> getAlarmDetailList(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("timestamp") long timestamp,
        @Query("imei") String imei,
        @Query("alarm_type") String alarm_type,
        @Query("page_dir") String page_dir,
        @Query("pagesize") int pagesize);

    /**
     * 某个告警类型的告警数据标记为已读（所谓的删除）
     *
     * @param imei 请求设备的告警数据，不携带则是请求该账号下的所有告警
     */
    @GET("/1/tool/get_alarminfo?method=setread")
    Flowable<RespBase> setAlarmRead(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("timestamp") long timestamp,
        @Query("imei") String imei,
        @Query("alarm_type") String alarm_type,
        @Query("ids") String ids,
        @Query("except") boolean except);

    /**
     * 告警设置
     */
    @GET("/1/tool/get_alarminfo?method=set_option")
    Flowable<RespBase> setAlarmOption(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("alias") String alias,
        @Query("push") int push,
        @Query("start_time") long start_time,
        @Query("end_time") long end_time,
        @Query("alarm_type") String alarmTypeIds,
        @Query("sound") int sound,
        @Query("shake") int shake,
        @Query("channelid") String channelid);

    /**
     * 告警配置获取
     */
    @GET("/1/tool/get_alarminfo?method=get_option")
    Flowable<RespAlarmOption> getAlarmOption(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("alias") String alias,
        @Query("channelid") String channelid);

    /**
     * 过去分享地址的url
     */
    @GET("/1/account/devinfo?method=app_getWeixinShared")
    Flowable<RespShareLocation> getShareLocationUrl(@QueryMap Map<String, String> params,
        @Query("account") String account,
        @Query("access_token") String access_token,
        @Query("duration") String duration,
        @Query("imei") String imei);

    /**
     * 获取服务商信息
     */
    @GET("1/account/info")
    Flowable<RespServiceProvider> getServiceProvider(@Query("access_token") String token,
        @Query("account") String account,
        @Query("target") String target,
        @Query("time") long time,
        @QueryMap Map<String, String> params);

    /**
     * 获取区域围栏
     */
    @GET("/1/tool/efence?method=get_area_fence")
    Flowable<RespAreaFence> getAreaFence(@QueryMap Map<String, String> commonParam,
        @Query("access_token") String access_token,
        @Query("account") String account,
        @Query("imei") String imei,
        @Query("alarm_id") String alarm_id);

    /**
     * 获取更新版本信息
     */
    @GET("/1/tool/appupdate")
    Flowable<RespUpdateInfo> getLatestVersion(@QueryMap Map<String, String> commonParam,
        @Query("android_version") String android_version,
        @Query("language") String language,
        @Query("vercode") int vercode,
        @Query("patchcode") String patchcode);

    /**
     * 获取主题信息
     */
    @GET("/1/tool/theme?method=getAllTheme")
    Flowable<RespThemeAll> getThemeInfo(@Query("account") String account,
        @Query("access_token") String token,
        @QueryMap Map<String, String> commonParam);

    /**
     * 里程统计
     */
    @GET("/1/tool/runstatus?method=milestat")
    Flowable<RespStatistics> getRunstatus(@Query("imei") String imei,
        @Query("access_token") String token,
        @Query("beginTime") long beginTime,
        @Query("endTime") long endTime,
        @Query("timezone") int timezone,
        @QueryMap Map<String, String> commonParam);

    /**
     * 物联卡充值信息查询
     *
     * @param msisdns 物联卡号,多个以逗号分隔
     */
    @GET("/1/carol-pay?method=getWlCardPayInfo")
    Flowable<RespWLCardInfo> getWlCardPayInfo(@QueryMap Map<String, String> commonParam,
        @Query("account") String account,
        @Query("target") String target,
        @Query("access_token") String access_token,
        @Query("msisdns") String msisdns);

    /**
     * 物联卡批量续费
     *
     * @param msisdns 物联卡号,多个以逗号分隔
     * @param pay_type app端为“app”
     */
    @GET("/1/carol-pay?method=renewWlcard")
    Flowable<RespRenewWlCard> renewWlcard(@QueryMap Map<String, String> commonParam,
        @Query("account") String account,
        @Query("target") String target,
        @Query("access_token") String access_token,
        @Query("pay_type") String pay_type,
        @Query("msisdns") String msisdns);

    /**
     * 返回即将过期（已过期）物联卡信息分布
     *
     * @param type 0：已过期/即将过期 1: 已过期 2: 即将过期。目前传3
     * @param range 0-0
     */
    @GET("/GetDataService?method=getExpireWlCardDistributeNum")
    Flowable<RespWLCardExpire> getExpireWlCardDistributeNum(@QueryMap Map<String, String> commonParam,
        @Query("account") String account,
        @Query("target") String target,
        @Query("access_token") String access_token,
        @Query("type") int type,
        @Query("range") String range);

    /**
     * 返回即将过期（已过期）物联卡信息详情
     *
     * @param type 0：已过期/即将过期 1: 已过期 2: 即将过期。目前传3
     */
    @GET("/GetDataService?method=getExpireWlCardInfo")
    Flowable<RespWLCardRecharge> getExpireWlCardInfo(@QueryMap Map<String, String> commonParam,
        @Query("account") String account,
        @Query("target") String target,
        @Query("access_token") String access_token,
        @Query("type") int type,
        @Query("range") String range,
        @Query("pageno") int pageno,
        @Query("pagesize") int pagesize,
        @Query("distribute") String distribute);

    /**
     * 查询设备模式及上线时间
     */
    @GET("/GetDataService?method=getDevOnlineInfo")
    Flowable<RespMode> getDevMode(@Query("access_token") String token,
        @Query("imei") String imei,
        @QueryMap Map<String, String> commonParam);

    /**
     * 设置设备模式
     */
    @GET("/GetDataService?method=setDevOnlineSchedule")
    Flowable<RespBase> setDevMode(@Query("access_token") String token,
        @Query("imei") String imei,
        @Query(value = "content", encoded = true) String content,
        @Query("mode") int mode,
        @QueryMap Map<String, String> commonParam);

    /**
     * 开启/关闭 省电模式
     */
    @GET("/1/voicemgmt?method=switchTricklePower")
    Flowable<RespBase> setTricklePower(@Query("imei") String imei,
        @Query("tricklepower") int mode,
        @Query("access_token") String token,
        @QueryMap Map<String, String> commonParam);

    /**
     * 设置定位精度
     */
    @GET("/1/voicemgmt?method=setPositionAccuracy")
    Flowable<RespBase> setAccuracy(@Query("imei") String imei,
        @Query("pos_accuracy") int mode,
        @Query("access_token") String token,
        @QueryMap Map<String, String> commonParam);

    /**
     * 设置录音灵敏度
     */
    @GET("/1/voicemgmt?method=setSmartRecVolume")
    Flowable<RespBase> setRecordVolume(@Query("imei") String imei,
        @Query("volume") String volume,
        @Query("access_token") String token,
        @QueryMap Map<String, String> commonParam);

    /**
     * 获取公告
     */
    @GET("/GetDataService?method=getAppNotice")
    Flowable<RespNotice> getNotice(@Query("access_token") String token,
        @QueryMap Map<String, String> commonParam);

    /**
     * 获取设备透传信息
     */
    @GET("search?method=dev_pass_info")
    Flowable<RespDevPassInfo> getPassInfo(@Query("imei") String imei,
        @Query("access_token") String token,
        @QueryMap Map<String, String> commonParam);

    /**
     * 获取所有型号
     */
    @GET("1/tool/deviceAccess?method=typeList")
    Flowable<RespAllTypes> getAllTypes(@Query("access_token") String token,
        @Query("getall") int getall,
        @Query("pageno") int pageno,
        @Query("pagesize") int pagesize,
        @QueryMap Map<String, String> commonParam);

    /**
     * 刷新单个设备
     */
    @GET("1/devices/tracking")
    Flowable<RespDeviceList> refreshByImei(@Query("access_token") String token,
        @Query("account") String account,
        @Query("imeis") String imei,
        @QueryMap Map<String, String> commonParam);

    /**
     * 获取型号的指令列表
     */
    @GET("1/tool/cmd?method=get_cmd")
    Flowable<RespTypeCmds> getCmds(@Query("access_token") String token,
        @Query("devtype") String devType,
        @Query("show_all") int showAll,
        @QueryMap Map<String, String> commonParam);

    /**
     * 设置指令
     */
    @POST("1/tool/order?method=send_cmd")
    Flowable<RespSendCmd> sendCmd(@Query("imeis") String imei,
        @Query("access_token") String token,
        @QueryMap Map<String, String> commonParam,
        @Body RequestBody sendCmd);

    /**
     * 获取指令结果
     */
    @GET("1/tool/order?method=get_response")
    Flowable<RespResponse> getResp(@Query("imei") String imei,
        @Query("id") String id,
        @Query("access_token") String token,
        @QueryMap Map<String, String> commonParam);

    /**
     * 返回当前绑定的账号接口
     */
    @GET("/GetDataService?method=getBindedWxAccountByApp")
    Flowable<RespBindedWx> getBindWx(@Query("access_token") String token,
        @Query("union_id") String unionId,
        @QueryMap Map<String, String> commonParam);

    /**
     * 绑定公众号开通报警
     */
    @GET("/GetDataService?method=bindWxAccountByApp")
    Flowable<RespBase> bindWx(@Query("wxcode") String wxcode,
        @Query("access_token") String token,
        @QueryMap Map<String, String> commonParam);



    /**
     * 拉取申请锁的列表
     *
     */
    @GET("/GetDataService?method=queryLockCtrl")
    Flowable<RespLockList> getQueryLockList(
            @Query("access_token") String access_token);


    /**
     * 批量拒绝申请锁
     *
     * @param infos imei1,eid1;imei2,eid2;imei3,eid3
     */
    @GET("/GetDataService?method=rejectLockCtrl")
    Flowable<RespWLCardExpire> actRejectLock(
           @Query("infos") String infos,
           @Query("access_token") String access_token);



    /**
     * 批量同意申请锁
     *
     * @param infos imei1,eid1;imei2,eid2;imei3,eid3
     */
    @GET("/GetDataService?method=approveLockCtrl")
    Flowable<RespWLCardExpire> actApproveLock(
           @Query("infos") String infos,
           @Query("access_token") String access_token);



    /**
     * 申请开锁
     *
     * @param imei 设备
     */
    @GET("/GetDataService?method=applyLockCtrl")
    Flowable<RespWLCardExpire> actApplyLock(
            @Query("imei") String imei,
            @Query("access_token") String access_token);

}
