package com.coomix.app.all.model.response;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by ly on 2017/5/22.
 */

public class RespBindWechat extends RespBase {

    /**
     * success : false
     * errcode : 3041
     * msg : wxid already exists
     * data : {"userinfo":{"uid":839218,"name":"安卓大法好","sid":"0","city_code":"","sex":0,"label":"","img":"http://test-buspic.gpsoo.net/goome01/M00/23/9C/wKgCoVkNgqmEem0NAAAAAJBGpLY455.jpg","grade":0,"score":102,"create_time":1492997667,"modify_time":1495454304,"remark":"","tel":"","wxid":"omS6RwDQ7Dx0CU-ew5njxye0w--U","privilege":0,"background":[]},"loginid":"9068f21ca806f6806b5a6435647","logintype":"0","querycity":"869999"}
     */

    @Expose
    private DataBean data;


    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * userinfo : {"uid":839218,"name":"安卓大法好","sid":"0","city_code":"","sex":0,"label":"","img":"http://test-buspic.gpsoo.net/goome01/M00/23/9C/wKgCoVkNgqmEem0NAAAAAJBGpLY455.jpg","grade":0,"score":102,"create_time":1492997667,"modify_time":1495454304,"remark":"","tel":"","wxid":"omS6RwDQ7Dx0CU-ew5njxye0w--U","privilege":0,"background":[]}
         * loginid : 9068f21ca806f6806b5a6435647
         * logintype : 0
         * querycity : 869999
         */
        @Expose
        private UserinfoBean userinfo;
        @Expose
        private String loginid;
        @Expose
        private String logintype;
        @Expose
        private String querycity;
        @Expose
        private String ticket;

        public String getTicket() {
            return ticket;
        }

        public void setTicket(String ticket) {
            this.ticket = ticket;
        }

        public UserinfoBean getUserinfo() {
            return userinfo;
        }

        public void setUserinfo(UserinfoBean userinfo) {
            this.userinfo = userinfo;
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

        public static class UserinfoBean {
            /**
             * uid : 839218
             * name : 安卓大法好
             * sid : 0
             * city_code :
             * sex : 0
             * label :
             * img : http://test-buspic.gpsoo.net/goome01/M00/23/9C/wKgCoVkNgqmEem0NAAAAAJBGpLY455.jpg
             * grade : 0
             * score : 102
             * create_time : 1492997667
             * modify_time : 1495454304
             * remark :
             * tel :
             * wxid : omS6RwDQ7Dx0CU-ew5njxye0w--U
             * privilege : 0
             * background : []
             */
            @Expose
            private String uid;
            @Expose
            private String name;
            @Expose
            private String sid;
            @Expose
            private String city_code;
            @Expose
            private int sex;
            @Expose
            private String label;
            @Expose
            private String img;
            @Expose
            private int grade;
            @Expose
            private int score;
            @Expose
            private int create_time;
            @Expose
            private int modify_time;
            @Expose
            private String remark;
            @Expose
            private String tel;
            @Expose
            private String wxid;
            @Expose
            private int privilege;
            @Expose
            private List<?> background;

            public String getUid() {
                return uid;
            }

            public void setUid(String uid) {
                this.uid = uid;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSid() {
                return sid;
            }

            public void setSid(String sid) {
                this.sid = sid;
            }

            public String getCity_code() {
                return city_code;
            }

            public void setCity_code(String city_code) {
                this.city_code = city_code;
            }

            public int getSex() {
                return sex;
            }

            public void setSex(int sex) {
                this.sex = sex;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public String getImg() {
                return img;
            }

            public void setImg(String img) {
                this.img = img;
            }

            public int getGrade() {
                return grade;
            }

            public void setGrade(int grade) {
                this.grade = grade;
            }

            public int getScore() {
                return score;
            }

            public void setScore(int score) {
                this.score = score;
            }

            public int getCreate_time() {
                return create_time;
            }

            public void setCreate_time(int create_time) {
                this.create_time = create_time;
            }

            public int getModify_time() {
                return modify_time;
            }

            public void setModify_time(int modify_time) {
                this.modify_time = modify_time;
            }

            public String getRemark() {
                return remark;
            }

            public void setRemark(String remark) {
                this.remark = remark;
            }

            public String getTel() {
                return tel;
            }

            public void setTel(String tel) {
                this.tel = tel;
            }

            public String getWxid() {
                return wxid;
            }

            public void setWxid(String wxid) {
                this.wxid = wxid;
            }

            public int getPrivilege() {
                return privilege;
            }

            public void setPrivilege(int privilege) {
                this.privilege = privilege;
            }

            public List<?> getBackground() {
                return background;
            }

            public void setBackground(List<?> background) {
                this.background = background;
            }
        }
    }
}
