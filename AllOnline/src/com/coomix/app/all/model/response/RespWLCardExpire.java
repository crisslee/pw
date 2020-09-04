package com.coomix.app.all.model.response;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespWLCardExpire extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private WLCardExpire data;

    public WLCardExpire getData() {
        return data;
    }

    public void setData(WLCardExpire data) {
        this.data = data;
    }

    public class WLCardExpire implements Serializable {
        private static final long serialVersionUID = 1L;
        private int total;
        private String distribute;

        public int getTotal() {
            return total;
        }

        public String getDistribute() {
            return distribute;
        }
    }
}
