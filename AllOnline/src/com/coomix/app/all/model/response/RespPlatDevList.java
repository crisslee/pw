package com.coomix.app.all.model.response;

import android.content.Context;
import android.text.TextUtils;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.model.request.ReqRenewOrder;
import com.coomix.app.all.util.CommunityUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ly on 2017/12/14 20:23.
 */
public class RespPlatDevList extends RespBase{
    /**
     * success : true
     * errcode : 0
     * msg : OK
     * data : [{"uid":1854167,"imei":"868120115743848","user_name":"道奇酷威 崔晓梦B10","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1854171,"imei":"868120115744002","user_name":"骊威 卢国明A4.2*","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943543,"imei":"868120116769354","user_name":"奥A4 郑梅森B14","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943547,"imei":"868120116770030","user_name":"雷克萨斯ES240 李庆福B13","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943551,"imei":"868120116770782","user_name":"科鲁兹 姜闯B3","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943559,"imei":"868120116764652","user_name":"迟鹏亮(结清)","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943563,"imei":"868120116764942","user_name":"雅阁 潘义勇A4*","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943575,"imei":"868120116765709","user_name":"凯美瑞 邹祥C9","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943579,"imei":"868120116765766","user_name":"昊锐 李可闻A9*","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943587,"imei":"868120116765907","user_name":"昊锐 李可闻B9","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943595,"imei":"868120116765980","user_name":"张登路(已拆)","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943599,"imei":"868120116765998","user_name":"昊锐 李可闻C9","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943603,"imei":"868120116766095","user_name":"迟鹏亮(结清)","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943607,"imei":"868120116766103","user_name":"迟鹏亮(结清)","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943611,"imei":"868120116766962","user_name":"凯美瑞 邹祥A9*","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943623,"imei":"868120116768471","user_name":"凯美瑞 邹祥B9","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943631,"imei":"868120116769800","user_name":"英菲尼迪QX50 王新B15","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]},{"uid":1943635,"imei":"868120116759439","user_name":"周宇(结清)","expire_time":"1970-01-01 00:00:00","expire_type":0,"fee":[{"fee_type":1,"amount":50}]}]
     */

    private List<PlatRechargeBean> data;

    public List<PlatRechargeBean> getData() {
        return data;
    }

    public void setData(List<PlatRechargeBean> data) {
        this.data = data;
    }

    public static class PlatRechargeBean {
        /**
         * uid : 1854167
         * imei : 868120115743848
         * user_name : 道奇酷威 崔晓梦B10
         * expire_time : 1970-01-01 00:00:00
         * user_expire_time : 2100-01-01 00:00:00
         * expire_type : 0
         * fee : [{"fee_type":1,"amount":50}]
         *
         uid: 设备id
         imei:设备IMEI号
         user_name:设备名称
         expire_time:到期时间
         user_expire_time:用户到期时间
         expire_type:到期状态 0:已过期 1:7天内到期 2：终身设备 3：正常 4：未启用
         fee:
         fee_type:续费类型 0：一年平台费
         amount：续费金额
         */

        public static final int TYPE_EXPIRED = 0;
        public static final int TYPE_ABOUT_TO_EXPIRED = 1;
        public static final int TYPE_NEVER_EXPIRED = 2;
        public static final int TYPE_NORMAL = 3;
        public static final int TYPE_UNUSED = 4;

        private long uid;
        private String imei;
        private String user_name;
        private String expire_time;
        private String user_expire_time;
        private int expire_type;
        private List<FeeBean> fee;

        //用于显示选择
        private boolean isSelected = false;
        private boolean isSelectMode = false;
        public long getUid() {
            return uid;
        }

        public void setUid(long uid) {
            this.uid = uid;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getUser_name() {
            return user_name;
        }

        public void setUser_name(String user_name) {
            this.user_name = user_name;
        }

        public String getExpire_time() {
            if(expire_time.trim().length() > 10) {
               return expire_time.trim().substring(0, 10);
            } else {
                return expire_time;
            }
        }

        public void setExpire_time(String expire_time) {
            this.expire_time = expire_time;
        }

        public String getUser_expire_time() {
            return user_expire_time;
        }

        public void setUser_expire_time(String user_expire_time) {
            this.user_expire_time = user_expire_time;
        }

        public int getExpire_type() {
            return expire_type;
        }

        public boolean couldRecharge() {
            if(expire_type == TYPE_EXPIRED || expire_type == TYPE_ABOUT_TO_EXPIRED || expire_type == TYPE_NORMAL) {
                return true;
            } else {
                return false;
            }
        }

        public ReqRenewOrder.DevRenewBean toDevRenewBean() {
            ReqRenewOrder.DevRenewBean bean = new ReqRenewOrder.DevRenewBean();
            bean.setUid(uid);
            bean.setFee_amount(getFee().get(0).getAmount());
            bean.setFee_type(getFee().get(0).getFee_type());
            bean.setImei(imei);
            return bean;
        }

        public String getExpireTypeMessage(Context context) {
            String msg;
            switch (expire_type) {
                case TYPE_EXPIRED:
                    msg = context.getString(R.string.renewal_expired);
                    break;
                case TYPE_ABOUT_TO_EXPIRED:
                    msg = context.getString(R.string.renewal_about_to_expire);
                    break;
                case TYPE_NEVER_EXPIRED:
                    msg = context.getString(R.string.renewal_never_expire);
                    try {
                        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date d = f.parse(user_expire_time);
                        if (d.getTime() < System.currentTimeMillis()) {
                            RespServiceProvider.ServiceProvider sp = AllOnlineApp.spInfo;
                            if (sp != null && !TextUtils.isEmpty(sp.sp_phone)) {
                                msg = "设备已过期，服务商联系方式：" + sp.sp_phone;
                            } else {
                                msg = "设备已过期，请前往 我的->服务商信息 页面联系您的服务商解决。";
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case TYPE_NORMAL:
                    msg = "";
                    break;
                default:
                    msg = "";
            }
            return msg;
        }

        public void setExpire_type(int expire_type) {
            this.expire_type = expire_type;
        }

        public List<FeeBean> getFee() {
            return fee;
        }

        public void setFee(List<FeeBean> fee) {
            this.fee = fee;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            if(couldRecharge()) {
                isSelected = selected;
            }
        }

        public boolean isSelectMode() {
            return isSelectMode;
        }

        public void setSelectMode(boolean selectMode) {
            isSelectMode = selectMode;
        }

        public String getUiShownName() {
            if(!TextUtils.isEmpty(user_name)) {
                return user_name;
            } else if(!TextUtils.isEmpty(imei)) {
                return imei;
            } else  {
                return "Empty";
            }

        }

        public static class FeeBean {
            /**
             * fee_type : 1
             * amount : 50
             */

            private int fee_type;
            private int amount;

            public int getFee_type() {
                return fee_type;
            }

            public void setFee_type(int fee_type) {
                this.fee_type = fee_type;
            }

            public int getAmount() {
                return amount;
            }

            public void setAmount(int amount) {
                this.amount = amount;
            }

            public String getFeeInfoStr(Context context) {
                String rmb = CommunityUtil.getDecimalStrByLong(amount, 2);
                String format = context.getString(R.string.recharge_format);
                String info = "";
                switch (fee_type) {
                    case 1:
                        info = String.format(format, rmb);
                        break;
                    default:
                        break;
                }
                return info;
            }
        }
    }
}
