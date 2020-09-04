package com.coomix.app.framework.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by herry on 2016/12/22.
 */

public class KeyboardUtils {
    public static void hide(Context context, View focusedView) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(focusedView, InputMethodManager.SHOW_FORCED);
        imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0); //强制隐藏键盘
    }
}
