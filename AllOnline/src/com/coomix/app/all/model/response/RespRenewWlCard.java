package com.coomix.app.all.model.response;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespRenewWlCard extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private RenewWlCard data;

    public RenewWlCard getData() {
        return data;
    }

}
