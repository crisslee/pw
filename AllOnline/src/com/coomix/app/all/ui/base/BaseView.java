package com.coomix.app.all.ui.base;

/**
 * Created by ly on 2017/9/26 11:15.
 */
public interface BaseView {
    void hideLoading();

    void showLoading(String msg);

    void showErr(String msg);
}
