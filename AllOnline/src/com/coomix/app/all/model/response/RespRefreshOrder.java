package com.coomix.app.all.model.response;

// FIXME generate failure  field _$Data110

import com.google.gson.annotations.Expose;

/**
 * Created by ly on 2017/5/24.
 */

public class RespRefreshOrder extends RespBase {

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
         * order_id : 3737622
         * order_status : 5
         */

        @Expose
        private long order_id;
        @Expose
        private int order_status;

        public long getOrder_id() {
            return order_id;
        }

        public void setOrder_id(long order_id) {
            this.order_id = order_id;
        }

        public int getOrder_status() {
            return order_status;
        }

        public void setOrder_status(int order_status) {
            this.order_status = order_status;
        }
    }
}
