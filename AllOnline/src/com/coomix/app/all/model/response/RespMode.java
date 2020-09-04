package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.DevMode;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/1/3.
 */
public class RespMode extends RespBase implements Serializable {

    private static final long serialVersionUID = 5272790489646611178L;
    private DevMode data;

    public DevMode getData() {
        return data;
    }

    public void setData(DevMode data) {
        this.data = data;
    }
}
