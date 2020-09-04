package com.coomix.app.all.model.bean;

/**
 * Created by ly on 2018/2/1 14:02.
 */
public class AudioPackMd {
    private String title = "";
    private String price = "";
    private boolean checked;

    public AudioPackMd(String title, String price, boolean checked) {
        this.title = title;
        this.price = price;
        this.checked = checked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
