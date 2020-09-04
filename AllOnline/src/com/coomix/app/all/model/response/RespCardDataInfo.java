package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.CardDataInfo;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019/3/12.
 */
public class RespCardDataInfo extends RespBase implements Serializable {

    private static final long serialVersionUID = -487753874546322544L;
    private CardDataInfo data;

    public CardDataInfo getData() {
        return data;
    }

    public void setData(CardDataInfo data) {
        this.data = data;
    }
}
