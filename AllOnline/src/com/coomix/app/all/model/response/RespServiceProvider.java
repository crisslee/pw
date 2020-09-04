package com.coomix.app.all.model.response;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/8/23.
 */
public class RespServiceProvider extends RespBase implements Serializable {
    private static final long serialVersionUID = -4484762068145065467L;

    private ServiceProvider data;

    public ServiceProvider getData() {
        return data;
    }

    public void setData(ServiceProvider data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RespServiceProvider{" +
            "data=" + data +
            '}';
    }

    public class ServiceProvider implements Serializable {
        private static final long serialVersionUID = 4491011621810135862L;
        public String sp_name;
        public String sp_phone;
        public String sp_addr;
        public String sp_contact;

        @Override
        public String toString() {
            return "ServiceProvider{" +
                "sp_name='" + sp_name + '\'' +
                ", sp_phone='" + sp_phone + '\'' +
                ", sp_addr='" + sp_addr + '\'' +
                ", sp_contact='" + sp_contact + '\'' +
                '}';
        }
    }
}
