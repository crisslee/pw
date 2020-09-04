package com.coomix.app.all.model.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespWLCardInfo extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private CardInfoList data;

    public CardInfoList getData() {
        return data;
    }

    public void setData(CardInfoList data) {
        this.data = data;
    }

    public class CardInfoList implements Serializable {
        private static final long serialVersionUID = 1L;
        public ArrayList<WLCardInfo> card_info;
    }
}
