package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.TrackPoints;
import java.io.Serializable;

/**
 * 回放历史
 *
 * @author felixqiu
 * @since 2018/8/17.
 */
public class RespTrackPoints extends RespBase implements Serializable {
    private static final long serialVersionUID = -8834587837410537994L;

    private TrackPoints data;

    public TrackPoints getData() {
        return data;
    }

    public void setData(TrackPoints data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RespTrackPoints{" +
            "data=" + data +
            '}';
    }
}
