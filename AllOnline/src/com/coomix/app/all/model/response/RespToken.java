package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.Token;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/8/6.
 */
public class RespToken extends RespBase implements Serializable {
    private static final long serialVersionUID = 2956933980897314392L;

    private Token data;

    public Token getData() {
        return data;
    }

    public void setData(Token data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RespToken{" +
            "data=" + data +
            '}';
    }
}
