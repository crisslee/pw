package com.coomix.app.all.model.response;

import java.io.Serializable;

/**
 * File Description:
 *
 * @author ssl
 * @since 2018/8/10.
 */
public class RespUploadImage extends RespBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private ImageInfo data;

    public ImageInfo getData() {
        return data;
    }

    public void setData(ImageInfo data) {
        this.data = data;
    }

    public class ImageInfo implements Serializable {
        //识别的车牌号信息
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String plate) {
            this.url = plate;
        }
    }
}
