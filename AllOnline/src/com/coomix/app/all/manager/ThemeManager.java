package com.coomix.app.all.manager;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.MainThread;
import android.text.TextUtils;

import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.ThemeAll;
import com.coomix.app.all.util.FileUtil;

import java.io.File;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/9/26.
 */
public class ThemeManager {
    private ThemeAll themeAll;

    private ThemeManager() {
    }

    private static class ThemeHolder {
        static ThemeManager INSTANCE = new ThemeManager();
    }

    public static ThemeManager getInstance() {
        return ThemeHolder.INSTANCE;
    }

    public void init(Context context) {
        ThemeAll themeAll;
        String path = FileUtil.getThemePath(context);
        if(!TextUtils.isEmpty(path) && new File(path).exists()) {
            themeAll = (ThemeAll) FileUtil.readObject(FileUtil.getThemePath(context));
        }else{
            themeAll = new ThemeAll();
        }
        setThemeAll(themeAll);
    }

    public ThemeAll getThemeAll() {
        if(themeAll == null){
            themeAll = new ThemeAll();
        }
        return themeAll;
    }

    @MainThread
    public void setThemeAll(ThemeAll themeAll) {
        this.themeAll = themeAll;
        MapIconManager.getInstance().initIcons();
    }

    public GradientDrawable getDrawable(int color, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setStroke(1, color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    public GradientDrawable getDefaultDrawable(Context context){
        return getDrawable(getThemeAll().getThemeColor().getColorUntouchableBtn(),
            context.getResources().getDimension(R.dimen.space));
    }

    public GradientDrawable getBGColorDrawable(Context context){
        return getDrawable(getThemeAll().getThemeColor().getColorWhole(),
                context.getResources().getDimension(R.dimen.space));
    }
}
