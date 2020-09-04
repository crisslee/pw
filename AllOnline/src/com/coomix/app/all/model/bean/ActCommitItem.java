package com.coomix.app.all.model.bean;

import android.text.TextUtils;
import android.view.View;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ActCommitItem.java <br/>
 * 活动详情中，报名需要显示的各ui类型.手动解析json
 *
 * @author shishenglong
 * @since 2016年8月17日 上午10:58:39
 */
public class ActCommitItem implements Serializable {
    private static final long serialVersionUID = 1L;
    /***
     * 姓名，qq/微信等正常输入的文本内容
     */
    public static final int INPUT_TEXT_NORMAL = 0;
    /***
     * 单项选择，例如性别：男，女，其他
     */
    public static final int RADIO_CONTENT = 1;
    /***
     * 弹窗选择，最多支持3级，例如地址：广东，深圳市，南山区
     */
    public static final int POP_SELECT_CONTENT = 2;
    /***
     * 地图选取位置
     */
    public static final int MAP_SELECT_LOCATION = 3;
    /***
     * 上传图片最多9张，最少0张
     */
    public static final int UPLOAD_IMAGE = 4;
    /***
     * 电话，纯数字内容
     */
    public static final int INPUT_NUMBERS = 5;

    private int id;
    private String name;
    private int type; //0:手机号，1:性别，2:所在地区，3:上班地点，4：上传照片
    private String hint; //输入提示
    private String regex; //正则表达式--对输入内容限制判断
    private int level_num; //地区分层 比如3层--广东-深圳-南山区
    private Object value; //可能是数字也可能是数组
    private ArrayList<ActCommitItemOption> options; //性别或地区的选项列表
    private ArrayList<Integer> default_option; //默认选中的项
    private int required = 0;  // 是否必填, 1-必填，0-可选
    private int is_common;   // 是否共有, 1-所有报名人信息都含此项，0-只有第一个报名人信息包含此项
    private boolean key_tel; // 本人电话（兼容以前的设计，通过此标志提取出本人电话，并填入DB的tel字段）.App不做处理

    /****
     * 不序列化*******存储界面中用于展示的View
     *****/
    private transient View view;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getHint() {
        if (hint == null) {
            return "";
        }
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public int getLevel_num() {
        return level_num;
    }

    public void setLevel_num(int level_num) {
        this.level_num = level_num;
    }

    public ArrayList<ActCommitItemOption> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<ActCommitItemOption> options) {
        this.options = options;
    }

    public ArrayList<Integer> getDefault_option() {
        return default_option;
    }

    public void setDefault_option(ArrayList<Integer> default_option) {
        this.default_option = default_option;
    }

    public Object getValue() {
        return value;
    }

    public int getIs_common() {
        return is_common;
    }

    public void setIs_common(int is_common) {
        this.is_common = is_common;
    }

    public void setValue(Object object) {
        this.value = object;
    }

    public int getRequired() {
        return required;
    }

    public void setRequired(int required) {
        this.required = required;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    /**
     * 从携带的value或default中（json格式）解析出选择的信息的下标，比如地址..  [0,0,0,0]
     */
    public String getPopSelectValue() {
        ArrayList<Integer> listData = null;
        if (getValue() != null && getValue() instanceof ArrayList) {
            listData = (ArrayList<Integer>) getValue();
        } else if (getDefault_option() != null && getDefault_option().size() > 0) {
            listData = getDefault_option();
            setValue(listData);
        } else {
            return null;
        }

        String content = "";
        try {
            if (listData != null && listData.size() > 0 && listData.size() == getLevel_num()) {
                ArrayList<ActCommitItemOption> listOptions = getOptions();
                for (int i = 0; i < listData.size(); i++) {
                    int id = listData.get(i);
                    if (listOptions != null) {
                        for (int j = 0; j < listOptions.size(); j++) {
                            if (id == listOptions.get(j).getId()) {
                                content += listOptions.get(j).getName();
                                listOptions = listOptions.get(j).getNext_level();
                                break;
                            }
                        }
                    } else {
                        return content;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 从vale中解析出位置信息的名字{"lng":"123.456","lat":"25.1456","name":"南山"}
     */
    public String getLocationValue() {
        String content = "";

        if (getValue() == null) {
            return content;
        }

        try {
            MyLocationInfo myLocationInfo = (MyLocationInfo) getValue();
            if (myLocationInfo != null) {
                content = myLocationInfo.getName();
            }
        } catch (Exception e) {

        }
        return content;
    }

    /**
     * 已经上传的网络图片["url1","url2","url3"...]
     */
    public ArrayList<ImageInfo> getImagesListValue() {
        ArrayList<ImageInfo> list = new ArrayList<ImageInfo>();

        if (getValue() == null || !(getValue() instanceof ArrayList)) {
            return list;
        }
        String content = "";
        try {
            ArrayList<String> listData = (ArrayList<String>) getValue();
            if (listData != null) {
                for (int i = 0; i < listData.size(); i++) {
                    String url = listData.get(i);
                    if (!TextUtils.isEmpty(url)) {
                        ImageInfo imageInfo = new ImageInfo();
                        imageInfo.setDomain("");
                        imageInfo.setImg_path(url);
                        imageInfo.setNet(true);
                        list.add(imageInfo);
                    }
                }
            }
        } catch (Exception e) {

        }
        return list;
    }

    public void setLocationValue(double lng, double lat, String name) {
        MyLocationInfo locationInfo = new MyLocationInfo();
        locationInfo.setLat(lat);
        locationInfo.setLng(lng);
        locationInfo.setName(name);
        setValue(locationInfo);
    }

    public void setPopSelectValue(int[] ids) {
        if (ids != null && ids.length > 0) {
            ArrayList<Integer> listData = new ArrayList<Integer>();
            for (int i = 0; i < ids.length; i++) {
                listData.add(ids[i]);
            }

            setValue(listData);
        }
    }

    public void parseValue(JsonObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        if (jsonObject.has("id")) {
            setId(jsonObject.get("id").getAsInt());
        }
        if (jsonObject.has("name")) {
            setName(jsonObject.get("name").getAsString());
        }
        if (jsonObject.has("type")) {
            setType(jsonObject.get("type").getAsInt());
        }
        if (jsonObject.has("hint")) {
            setHint(jsonObject.get("hint").getAsString());
        }
        if (jsonObject.has("regex")) {
            setRegex(jsonObject.get("regex").getAsString());
        }
        if (jsonObject.has("level_num")) {
            setLevel_num(jsonObject.get("level_num").getAsInt());
        }
        if (jsonObject.has("required")) {
            setRequired(jsonObject.get("required").getAsInt());
        }
        if (jsonObject.has("is_common")) {
            setIs_common(jsonObject.get("is_common").getAsInt());
        }
        if (jsonObject.has("options")) {
            JsonArray jsonArray = jsonObject.get("options").getAsJsonArray();
            ArrayList<ActCommitItemOption> listOptions = new ArrayList<ActCommitItemOption>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject tempJsonObj = jsonArray.get(i).getAsJsonObject();
                ActCommitItemOption actCommitItemOption =
                    new Gson().fromJson(tempJsonObj.toString(), ActCommitItemOption.class);
                listOptions.add(actCommitItemOption);
            }
            setOptions(listOptions);
        }

        if (jsonObject.has("default_option")) {
            ArrayList<Integer> listDef = new ArrayList<Integer>();
            JsonArray jsonArray = jsonObject.get("default_option").getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                listDef.add(jsonArray.get(i).getAsInt());
            }
            setDefault_option(listDef);
        }

        if (!jsonObject.has("value")) {
            return;
        }

        //解析value  -- Object
        try {
            JsonElement jsonElement = jsonObject.get("value");
            String data = jsonElement.toString();
            if (data != null && data.startsWith("{") && data.endsWith("}")) {
                JsonObject jsonObject1 = jsonElement.getAsJsonObject();
                MyLocationInfo myLocationInfo = new MyLocationInfo();
                myLocationInfo.setLng(jsonObject1.get("lng").getAsDouble());
                myLocationInfo.setLat(jsonObject1.get("lat").getAsDouble());
                myLocationInfo.setName(jsonObject1.get("name").getAsString());
                setValue(myLocationInfo);
                return;
            }
        } catch (Exception e) {

        }

        try {
            JsonElement jsonElement = jsonObject.get("value");
            String data = jsonElement.toString();
            if (data != null && data.startsWith("[") && data.endsWith("]")) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                ArrayList<String> listStr = new ArrayList<String>();
                ArrayList<Integer> listInt = new ArrayList<Integer>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    try {
                        listInt.add(jsonArray.get(i).getAsInt());
                    } catch (Exception e) {

                    }
                    try {
                        listStr.add(jsonArray.get(i).getAsString());
                    } catch (Exception e) {

                    }
                }
                if (listInt.size() > 0) {
                    setValue(listInt);
                } else if (listStr.size() > 0) {
                    setValue(listStr);
                }
                return;
            }
        } catch (Exception e) {

        }

        try {
            JsonElement jsonElement = jsonObject.get("value");
            String data = jsonElement.toString();
            if (data != null && data.startsWith("\"") && data.endsWith("\"")) {
                String value = jsonElement.getAsString();
                setValue(value);
                return;
            }
        } catch (Exception e) {

        }

        try {
            int value = jsonObject.get("value").getAsInt();
            setValue(value);
            return;
        } catch (Exception e) {

        }
    }

    //判断该项是否有过提交
    public boolean isValueCommitted() {
        boolean isValueCommitted = false;
        switch (type) {
            case INPUT_TEXT_NORMAL:
            case INPUT_NUMBERS:
                String str = (String) getValue();
                isValueCommitted = str != null && str.length() > 0;
                break;
            case RADIO_CONTENT:
                List<Integer> list = (ArrayList<Integer>) getValue();
                isValueCommitted = list != null && list.get(0) != null && list.get(0) >= 0;
                break;
            case POP_SELECT_CONTENT:
                str = getPopSelectValue();
                isValueCommitted = str != null && str.length() > 0;
                break;
            case MAP_SELECT_LOCATION:
                str = getLocationValue();
                isValueCommitted = str != null && str.length() > 0;
                break;
            case UPLOAD_IMAGE:
                List<ImageInfo> infos = getImagesListValue();
                isValueCommitted = infos.size() > 0;
                break;
        }
        return isValueCommitted;
    }
}
