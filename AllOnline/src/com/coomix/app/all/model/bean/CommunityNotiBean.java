package com.coomix.app.all.model.bean;

/**
 * Created by herry on 2016/12/28.
 */
public class CommunityNotiBean {
    private String content;
    private int appid;
    private Extras extras;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getAppid() {
        return appid;
    }

    public void setAppid(int appid) {
        this.appid = appid;
    }

    public Extras getExtras() {
        return extras;
    }

    public void setExtras(Extras extras) {
        this.extras = extras;
    }

    @Override
    public String toString() {
        return "CommunityNotiBean{" +
                "content='" + content + '\'' +
                ", appid=" + appid +
                ", extras=" + extras +
                '}';
    }

    public static class Extras {
        private String alarm;
        private int silent;
        private String sound;
        private String shake;
        private int type;
        private int source;

        public String getAlarm() {
            return alarm;
        }

        public void setAlarm(String alarm) {
            this.alarm = alarm;
        }

        public int getSilent() {
            return silent;
        }

        public void setSilent(int silent) {
            this.silent = silent;
        }

        public String getSound() {
            return sound;
        }

        public void setSound(String sound) {
            this.sound = sound;
        }

        public String getShake() {
            return shake;
        }

        public void setShake(String shake) {
            this.shake = shake;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getSource() {
            return source;
        }

        public void setSource(int source) {
            this.source = source;
        }

        @Override
        public String toString() {
            return "Extras{" +
                    "alarm='" + alarm + '\'' +
                    ", silent=" + silent +
                    ", sound='" + sound + '\'' +
                    ", shake='" + shake + '\'' +
                    ", type=" + type +
                    ", source=" + source +
                    '}';
        }
    }
}
