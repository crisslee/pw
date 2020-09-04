package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.ThemeAll;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/9/26.
 */
public class RespThemeAll extends RespBase implements Serializable {
    private static final long serialVersionUID = -4664313284982551867L;
    private ThemeAll data;

    public ThemeAll getData() {
        return data;
    }

    public void setData(ThemeAll data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RespThemeAll{" +
            "data=" + data +
            '}';
    }
}
