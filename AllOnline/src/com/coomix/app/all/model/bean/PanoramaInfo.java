package com.coomix.app.all.model.bean;

import java.io.Serializable;
import org.json.JSONObject;

public class PanoramaInfo implements Serializable {

    /**
     * json
     * {"x":13738359,"switch":true,"y":5101659,"mode":"night","z":0,"rname":
     * "中华路","type":"street","switchid":"01011700001306241554171725F","id":
     * "01011700001306252047085865F"}
     */
    private static final long serialVersionUID = -6340637206961057951L;

    public String id; // 当前场景点pid
    public String switchid; // 切换后的全景pid
    public boolean isSwitch; // 是否支持日夜景，场景可以切换
    public String mode; // 当前日夜景模式，day白天，night夜晚
    public String type; // 全景类型，street街景，inter内景
    public String rname; // 道路名称，遇到交叉点，默认显示“百度全景”
    public double movedir;// 移动方向，正北为0，顺时针
    public int x; // 当前场景点x坐标
    public int y; // 当前场景点y坐标
    public double z; // 当前场景点z坐标

    public PanoramaInfo(String jsonInfo) {
        try {
            JSONObject json = new JSONObject(jsonInfo.toLowerCase());
            if (json.has("id")) {
                this.id = json.getString("id");
            }
            if (json.has("switchid")) {
                this.switchid = json.getString("switchid");
            }
            if (json.has("switch")) {
                this.isSwitch = json.getBoolean("switch");
            }
            if (json.has("mode")) {
                this.mode = json.getString("mode");
            }
            if (json.has("type")) {
                this.type = json.getString("type");
            }
            if (json.has("rname")) {
                this.rname = json.getString("rname");
            }
            if (json.has("movedir")) {
                this.movedir = json.getDouble("movedir");
            }
            if (json.has("x")) {
                this.x = json.getInt("x");
            }
            if (json.has("y")) {
                this.y = json.getInt("y");
            }
            if (json.has("z")) {
                this.z = json.getDouble("z");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
