package com.coomix.app.all.model.response;

import java.util.List;

/**
 * Created by ly on 2017/12/25 11:15.
 */

public class RespRefrshRenewPayOrder extends RespBase {
    /**
     * data : {"renew_infos":[{"userid":60485,"user_name":"沪B81227","expire_time":"2018-12-25 11:14:22"}]}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<RenewInfosBean> renew_infos;

        public List<RenewInfosBean> getRenew_infos() {
            return renew_infos;
        }

        public void setRenew_infos(List<RenewInfosBean> renew_infos) {
            this.renew_infos = renew_infos;
        }

        public static class RenewInfosBean {
            /**
             * userid : 60485
             * user_name : 沪B81227
             * expire_time : 2018-12-25 11:14:22
             */

            private long userid;
            private String user_name = "";
            private String expire_time = "";

            public long getUserid() {
                return userid;
            }

            public void setUserid(int userid) {
                this.userid = userid;
            }

            public String getUser_name() {
                return user_name;
            }

            public void setUser_name(String user_name) {
                this.user_name = user_name;
            }

            public String getExpire_time() {
                return expire_time;
            }

            public void setExpire_time(String expire_time) {
                this.expire_time = expire_time;
            }
        }
    }
}
