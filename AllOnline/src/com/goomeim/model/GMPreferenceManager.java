package com.goomeim.model;

import android.content.Context;
import android.content.SharedPreferences;
import com.coomix.app.all.AllOnlineApp;
import java.util.Set;

public class GMPreferenceManager {
    private SharedPreferences.Editor editor;
    private SharedPreferences mSharedPreferences;
    private static final String KEY_AT_GROUPS = "GM_AT_GROUPS";

    private GMPreferenceManager() {
        mSharedPreferences = AllOnlineApp.mApp.getSharedPreferences("GM_SP_AT_MESSAGE", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
    }

    private static GMPreferenceManager instance;

    public synchronized static GMPreferenceManager getInstance() {
        if (instance == null) {
            instance = new GMPreferenceManager();
        }
        return instance;
    }

    public void setAtMeGroups(Set<String> groups) {
        editor.remove(KEY_AT_GROUPS);
        editor.putStringSet(KEY_AT_GROUPS, groups);
        editor.apply();
    }

    public Set<String> getAtMeGroups() {
        return mSharedPreferences.getStringSet(KEY_AT_GROUPS, null);
    }
}
