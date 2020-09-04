package com.coomix.app.all.share;

import android.view.View.OnClickListener;

/**
 * 保存popupwindow中item（TextView）的属性 PopupWindowUtil.showPopWindow使用
 *
 * @author goome
 */
public class TextSet {
    public String text;
    public int textResId;
    public int imageId;
    public boolean important;
    public OnClickListener onClickListener;
    //点击后自动关闭弹窗，默认true。有些不需要点击item关闭弹窗的可以设置成false
    public boolean bClickClose = true;

    /**
     * @param important 重要/敏感功能
     */
    public TextSet(int textResId, boolean important, OnClickListener onClickListener) {
        this(textResId, -1, important, true, onClickListener);
    }

    public TextSet(String text, boolean important, OnClickListener onClickListener) {
        this(0, -1, important, true, onClickListener);
        this.text = text;
    }

    public TextSet(int textResId, int imageId, boolean important, OnClickListener onClickListener) {
        this(textResId, imageId, important, true, onClickListener);
    }

    public TextSet(int textResId, int imageId, boolean important, boolean bClickClose,
        OnClickListener onClickListener) {
        this.imageId = imageId;
        this.textResId = textResId;
        this.important = important;
        this.onClickListener = onClickListener;
        this.bClickClose = bClickClose;
    }
}