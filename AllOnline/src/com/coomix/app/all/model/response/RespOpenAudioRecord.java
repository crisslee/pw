package com.coomix.app.all.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ly on 2018/2/9 17:06.
 */

public class RespOpenAudioRecord extends RespBase {
    /**
     * data : {"data":{"status":"0"}}
     */

    @SerializedName("data")
    private DataBeanX data;

    public DataBeanX getData() {
        return data;
    }

    public void setData(DataBeanX data) {
        this.data = data;
    }

    public static class DataBeanX {

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
             * data : {"status":"0"}
             * 0:设备之前为关闭录音状态
             * 1:设备以前为开启录音状态
             */
            @SerializedName("status")
            private int status;

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }
        }
    }
}
