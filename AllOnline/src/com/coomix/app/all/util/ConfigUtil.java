package com.coomix.app.all.util;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.log.AppConfigs;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2017/9/7.
 */
public class ConfigUtil {
    public static boolean isUseGMIMGroup() {
        AppConfigs config = AllOnlineApp.getAppConfig();
        //return config.getCarol_group_msg_using_gmim() == 1;
        return true;
    }
}
