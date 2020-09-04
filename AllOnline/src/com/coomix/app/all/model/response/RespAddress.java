package com.coomix.app.all.model.response;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespAddress extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private Address data;

    public Address getData() {
        return data;
    }

    public void setData(Address data) {
        this.data = data;
    }

    public class Address implements Serializable {
        private static final long serialVersionUID = 8549302668964769300L;
        public String address;
    }
}
