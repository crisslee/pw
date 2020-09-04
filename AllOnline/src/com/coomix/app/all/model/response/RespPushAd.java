package com.coomix.app.all.model.response;

import com.google.gson.annotations.Expose;

/**
 * Created by ly on 2017/7/17.
 */

public class RespPushAd extends RespBase {

    /**
     * data : {"url":"","pic":"","push":true}
     */

    @Expose
    private PushedAd data;

    public PushedAd getData() {
        return data;
    }

    public void setData(PushedAd data) {
        this.data = data;
    }

    public static class PushedAd {
        /**
         * url :
         * pic :
         * push : true
         */
        @Expose
        private String url;
        @Expose
        private String pic;
        @Expose
        private boolean push;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPic() {
            return pic;
        }

        public void setPic(String pic) {
            this.pic = pic;
        }

        public boolean isPush() {
            return push;
        }

        public void setPush(boolean push) {
            this.push = push;
        }
    }
}
