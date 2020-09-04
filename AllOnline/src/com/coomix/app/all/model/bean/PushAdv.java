package com.coomix.app.all.model.bean;

import java.io.Serializable;

public class PushAdv implements Serializable {

    private static final long serialVersionUID = -4199032349283504226L;

    private InstallerBean installer;
    private UploadgpsBean uploadgps;

    public InstallerBean getInstaller() {
        return installer;
    }

    public void setInstaller(InstallerBean installer) {
        this.installer = installer;
    }

    public UploadgpsBean getUploadgps() {
        return uploadgps;
    }

    public void setUploadgps(UploadgpsBean uploadgps) {
        this.uploadgps = uploadgps;
    }

    public static class InstallerBean {

        private String url;
        private String pic;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPic() {
            return pic;
        }

        public void setPic(String pic) {
            this.pic = pic;
        }
    }

    public static class UploadgpsBean {

        private String url;
        private long interval;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }
    }
}
