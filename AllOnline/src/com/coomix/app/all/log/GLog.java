package com.coomix.app.all.log;

import android.os.Environment;
import android.util.Log;
import com.coomix.app.all.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019-08-16.
 */
public class GLog {

    public static void v(String tag, String msg) {
        if (Constant.IS_DEBUG_MODE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (Constant.IS_DEBUG_MODE) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (Constant.IS_DEBUG_MODE) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (Constant.IS_DEBUG_MODE) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (Constant.IS_DEBUG_MODE) {
            Log.e(tag, msg);
        }
    }

}
