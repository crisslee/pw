package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.RechargeDataBundle;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/3/19.
 */
public class RespRechargeDataBundle extends RespBase implements Serializable {
    private static final long serialVersionUID = 8384131986736696086L;
    private RechargeDataBundle data;

    public RechargeDataBundle getData() {
        return data;
    }

    public void setData(RechargeDataBundle data) {
        this.data = data;
    }
}
