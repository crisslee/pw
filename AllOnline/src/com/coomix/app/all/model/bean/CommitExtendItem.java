package com.coomix.app.all.model.bean;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;

public class CommitExtendItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int type;
    private Object value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void parseObjectValue(JsonObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        if (jsonObject.has("id")) {
            setId(jsonObject.get("id").getAsInt());
        }

        if (jsonObject.has("type")) {
            setType(jsonObject.get("type").getAsInt());
        }

        if (!jsonObject.has("value")) {
            return;
        }

        //解析value  -- Object
        try {
            JsonElement jsonElement = jsonObject.get("value");
            String data = jsonElement.toString();
            if (data != null && data.startsWith("{") && data.endsWith("}")) {
                JsonObject jsonObject1 = jsonObject.get("value").getAsJsonObject();
                MyLocationInfo myLocationInfo = new MyLocationInfo();
                myLocationInfo.setLng(jsonObject1.get("lng").getAsDouble());
                myLocationInfo.setLat(jsonObject1.get("lat").getAsDouble());
                myLocationInfo.setName(jsonObject1.get("name").getAsString());
                setValue(myLocationInfo);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            JsonElement jsonElement = jsonObject.get("value");
            String data = jsonElement.toString();
            if (data != null && data.startsWith("[") && data.endsWith("]")) {
                JsonArray jsonArray = jsonObject.get("value").getAsJsonArray();
                ArrayList<String> listStr = new ArrayList<String>();
                ArrayList<Integer> listInt = new ArrayList<Integer>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    try {
                        listInt.add(jsonArray.get(i).getAsInt());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        listStr.add(jsonArray.get(i).getAsString());
                    } catch (Exception e) {
                        e.printStackTrace();
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
            e.printStackTrace();
        }

        try {
            JsonElement jsonElement = jsonObject.get("value");
            String data = jsonElement.toString();
            if (data != null && data.startsWith("\"") && data.endsWith("\"")) {
                String value = jsonObject.get("value").getAsString();
                setValue(value);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int value = jsonObject.get("value").getAsInt();
            setValue(value);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
