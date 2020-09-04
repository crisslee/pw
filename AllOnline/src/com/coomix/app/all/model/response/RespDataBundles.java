package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.DataBundles;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/3/18.
 */
public class RespDataBundles extends RespBase implements Serializable {

    private static final long serialVersionUID = 1709981286133123040L;
    private DataBundles data;

    public DataBundles getData() {
        return data;
    }

    public void setData(DataBundles data) {
        this.data = data;
    }
}
