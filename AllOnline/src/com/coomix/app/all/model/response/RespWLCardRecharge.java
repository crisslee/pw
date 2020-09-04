package com.coomix.app.all.model.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/11.
 */
public class RespWLCardRecharge extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private WLCardPriceList data;

    public WLCardPriceList getData() {
        return data;
    }

    public void setData(WLCardPriceList data) {
        this.data = data;
    }

    public class WLCardPriceList implements Serializable {
        private static final long serialVersionUID = 1L;
        private ArrayList<WLCardRecharge> records;

        public ArrayList<WLCardRecharge> getRecords() {
            if(records == null){
                records = new ArrayList<WLCardRecharge>();
            }
            return records;
        }

        public void setRecords(ArrayList<WLCardRecharge> records) {
            this.records = records;
        }
    }
}
