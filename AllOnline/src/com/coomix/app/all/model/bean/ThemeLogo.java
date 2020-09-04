package com.coomix.app.all.model.bean;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/9/26.
 */
public class ThemeLogo implements Serializable {
    private static final long serialVersionUID = 4412306987430120969L;
    public String url;

    @Override
    public String toString() {
        return "ThemeLogo{" +
            "url='" + url + '\'' +
            '}';
    }
}
