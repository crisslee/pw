package com.coomix.app.all.model.bean;

public class MineBean {
    public static final int VIEW_TYPE_HEADER = 1;
    public static final int VIEW_TYPE_EMPTY = 2;
    public static final int VIEW_TYPE_NORMAL = 3;
    public static final int VIEW_TYPE_WITH_TIP = 4;
    public static final int VIEW_TYPE_SHORT_DIVIDER = 5;
    public static final int VIEW_TYPE_FULL_DIVIDER = 6;
    public static final int VIEW_TYPE_ERROR_TIP = 7;

    private int mType;
    private String mIconPath;
    private int mIconResId;
    private int mNonIconResId;
    private String mTitle;
    private int mNotifyCount;
    private Class<?> mActivityClass;
    private boolean mClickable;

    /* header */
    public MineBean(String iconPath, String title, Class<?> activityClass, boolean clickable) {
        mType = VIEW_TYPE_HEADER;
        mIconPath = iconPath;
        mTitle = title;
        mActivityClass = activityClass;
        mClickable = clickable;
    }

    /* divider & empty */
    public MineBean(int type) {
        mType = type;
        mClickable = true;
    }

    /* normal */
    public MineBean(int iconResId, int nonIconResId, String title, Class<?> activityClass, boolean clickable) {
        mType = VIEW_TYPE_NORMAL;
        mIconResId = iconResId;
        mNonIconResId = nonIconResId;
        mTitle = title;
        mActivityClass = activityClass;
        mClickable = clickable;
    }

    /* normal with tip */
    public MineBean(int iconResId, int nonIconResId, String title, int notifyCount, Class<?> activityClass,
        boolean clickable) {
        mType = VIEW_TYPE_WITH_TIP;
        mIconResId = iconResId;
        mNonIconResId = nonIconResId;
        mTitle = title;
        mNotifyCount = notifyCount;
        mActivityClass = activityClass;
        mClickable = clickable;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public String getIconPath() {
        return mIconPath;
    }

    public void setIconPath(String mIconPath) {
        this.mIconPath = mIconPath;
    }

    public int getIconResId() {
        return mIconResId;
    }

    public void setIconResId(int iconResId) {
        this.mIconResId = mIconResId;
    }

    public int getNonIconResId() {
        return mNonIconResId;
    }

    public void setNonIconResId(int nonIconResId) {
        this.mNonIconResId = nonIconResId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getNotifyCount() {
        return mNotifyCount;
    }

    public void setNotifyCount(int mNotifyCount) {
        this.mNotifyCount = mNotifyCount;
    }

    public Class<?> getActivityClass() {
        return mActivityClass;
    }

    public void setActivityClassName(Class<?> activityClass) {
        this.mActivityClass = activityClass;
    }

    public boolean isClickable() {
        return mClickable;
    }

    public void setClickable(boolean clickable) {
        this.mClickable = clickable;
    }

    @Override
    public String toString() {
        return "MineBean{" +
            "mType=" + mType +
            ", mIconPath='" + mIconPath + '\'' +
            ", mIconResId=" + mIconResId +
            ", mNonIconResId=" + mNonIconResId +
            ", mTitle='" + mTitle + '\'' +
            ", mNotifyCount=" + mNotifyCount +
            ", mActivityClass=" + mActivityClass +
            ", mClickable=" + mClickable +
            '}';
    }
}
