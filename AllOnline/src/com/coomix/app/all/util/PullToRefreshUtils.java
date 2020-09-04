package com.coomix.app.all.util;

import com.handmark.pulltorefresh.library.IPullToRefresh;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

/**
 * 下拉刷新控件utils
 *
 * @author 刘生健
 * @since 2016-4-6 下午02:41:45
 */
public class PullToRefreshUtils {

    /**
     * 关闭上拉加载更多功能
     */
    public static void disablePullUp(IPullToRefresh pullToRefresh) {
        if (pullToRefresh == null || pullToRefresh.getMode() == Mode.DISABLED) {
            return;
        }
        if (pullToRefresh.getMode() == Mode.PULL_FROM_END || pullToRefresh.getMode() == Mode.PULL_UP_TO_REFRESH) {
            pullToRefresh.setMode(Mode.DISABLED);
        } else {
            pullToRefresh.setMode(Mode.PULL_FROM_START);
        }
    }
}
