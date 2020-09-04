package com.coomix.app.all.model.response;

import com.coomix.app.all.model.bean.ActCategory;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;

/**
 * Created by ly on 2017/8/24.
 */

public class RespActCategory extends RespBase {

    @Expose
    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        @Expose
        private ArrayList<ActCategory> categories;

        public ArrayList<ActCategory> getCategories() {
            return categories;
        }

        public void setCategories(ArrayList<ActCategory> categories) {
            this.categories = categories;
        }


    }

}
