package com.coomix.app.all.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ly on 2018/1/4 15:00.
 */

public class RespLoginCommunity extends RespBase {
    /**
     * data : {"userinfo":{"uid":3176350,"name":"剑人","sid":"0","city_code":"","sex":0,"label":"挤来挤去渝中区","img":"http://buspic.gpsoo.net/goome01/M00/47/F2/wKgCoVnN5B-EHkRDAAAAAL2bxu0215.jpg","grade":0,"score":127,"create_time":1497232150,"modify_time":1508815308,"remark":"","tel":"","wxid":"omS6RwGdDp0OIiC_DYwV9ROppQpE","privilege":16,"background":[]},"ticket":"2|jZx9NsDkyElBGLp8C09D389MxBTz4SuBT3OMxB+wJR+ekBLfu1dpuhrJNWtPPiHEsN+sMMxLZ9ZzEB82TGQ5+g==","loginid":"1551751","logintype":"0","querycity":"869999","hxAccount":"3176350","hxPwd":"245105","customerId":"caronlinekf_000","customerPic":"http://buspic.gpsoo.net/goome01/M00/1F/61/wKgCoFkC7-qEMVLHAAAAAB4y5RM413.png"}
     */

    @SerializedName("data")
    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * userinfo : {"uid":3176350,"name":"剑人","sid":"0","city_code":"","sex":0,"label":"挤来挤去渝中区","img":"http://buspic.gpsoo.net/goome01/M00/47/F2/wKgCoVnN5B-EHkRDAAAAAL2bxu0215.jpg","grade":0,"score":127,"create_time":1497232150,"modify_time":1508815308,"remark":"","tel":"","wxid":"omS6RwGdDp0OIiC_DYwV9ROppQpE","privilege":16,"background":[]}
         * ticket : 2|jZx9NsDkyElBGLp8C09D389MxBTz4SuBT3OMxB+wJR+ekBLfu1dpuhrJNWtPPiHEsN+sMMxLZ9ZzEB82TGQ5+g==
         * loginid : 1551751
         * logintype : 0
         * querycity : 869999
         * hxAccount : 3176350
         * hxPwd : 245105
         * customerId : caronlinekf_000
         * customerPic : http://buspic.gpsoo.net/goome01/M00/1F/61/wKgCoFkC7-qEMVLHAAAAAB4y5RM413.png
         */

        @SerializedName("userinfo")
        private CommunityUser userinfo;
        @SerializedName("ticket")
        private String ticket;
        @SerializedName("loginid")
        private String loginid;
        @SerializedName("logintype")
        private String logintype;
        @SerializedName("querycity")
        private String querycity;
        @SerializedName("hxAccount")
        private String hxAccount;
        @SerializedName("hxPwd")
        private String hxPwd;
        @SerializedName("customerId")
        private String customerId;
        @SerializedName("customerPic")
        private String customerPic;

        public CommunityUser getUserinfo() {
            return userinfo;
        }

        public void setUserinfo(CommunityUser userinfo) {
            this.userinfo = userinfo;
        }

        public String getTicket() {
            return ticket;
        }

        public void setTicket(String ticket) {
            this.ticket = ticket;
        }

        public String getLoginid() {
            return loginid;
        }

        public void setLoginid(String loginid) {
            this.loginid = loginid;
        }

        public String getLogintype() {
            return logintype;
        }

        public void setLogintype(String logintype) {
            this.logintype = logintype;
        }

        public String getQuerycity() {
            return querycity;
        }

        public void setQuerycity(String querycity) {
            this.querycity = querycity;
        }

        public String getHxAccount() {
            return hxAccount;
        }

        public void setHxAccount(String hxAccount) {
            this.hxAccount = hxAccount;
        }

        public String getHxPwd() {
            return hxPwd;
        }

        public void setHxPwd(String hxPwd) {
            this.hxPwd = hxPwd;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getCustomerPic() {
            return customerPic;
        }

        public void setCustomerPic(String customerPic) {
            this.customerPic = customerPic;
        }

    }
}
