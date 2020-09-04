package com.coomix.app.all.model.response;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespAlarmCount extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private AlarmCount data;

    public AlarmCount getData() {
        return data;
    }

    public void setData(AlarmCount data) {
        this.data = data;
    }

    public class AlarmCount
    {
        public int count;
    }
}
