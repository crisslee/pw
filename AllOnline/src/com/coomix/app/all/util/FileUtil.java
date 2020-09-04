package com.coomix.app.all.util;

import android.content.Context;
import android.util.Log;
import com.coomix.app.all.Constant;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2018/8/13.
 */
public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();
    private static final String TOKEN = "_token.dat";
    private static final String SP_INFO = "_spinfo.dat";
    private static final String THEME = "_theme.dat";
    private static final String POWER_MODE = "_mode.dat";

    public static String getTokenPath(Context context) {
        File f = context.getApplicationContext().getFileStreamPath(TOKEN);
        return f.getAbsolutePath();
    }

    public static boolean deleteTokenFile(Context context) {
        try {
            File f = new File(getTokenPath(context));
            return !f.exists() || f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getSpPath(Context context) {
        File f = context.getApplicationContext().getFileStreamPath(SP_INFO);
        return f.getAbsolutePath();
    }

    public static boolean deleteSpFile(Context context) {
        try {
            File f = new File(getSpPath(context));
            return !f.exists() || f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getThemePath(Context context) {
        File f = context.getApplicationContext().getFileStreamPath(THEME);
        return f.getAbsolutePath();
    }

    public static String getModePath(Context context) {
        File f = context.getApplicationContext().getFileStreamPath(POWER_MODE);
        return f.getAbsolutePath();
    }

    public static void saveOjbect(Context context, Object o, String path) {
        if (!(o instanceof Serializable)) {
            return;
        }
        FileOutputStream f = null;
        ObjectOutputStream out = null;
        try {
            File file = new File(path);
            if (Constant.IS_DEBUG_MODE) {
                Log.d(TAG, "path=" + path + ", file path=" + file.getAbsolutePath());
            }
            if (file.exists()) {
                if (Constant.IS_DEBUG_MODE) {
                    Log.d(TAG, "file exist");
                }
                file.delete();
            }
            f = new FileOutputStream(file);
            out = new ObjectOutputStream(f);
            out.writeObject(o);
            out.flush();
        } catch (FileNotFoundException fe) {
            fe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (f != null) {
                    f.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object readObject(String path) {
        ObjectInputStream in = null;
        Object o = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                if (Constant.IS_DEBUG_MODE) {
                    Log.i(TAG, "readObject error, file not exist.");
                }
                return null;
            }
            FileInputStream f = new FileInputStream(file);
            in = new ObjectInputStream(f);
            o = in.readObject();
            f.close();
        } catch (Exception e) {
            o = null;
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return o;
    }
}
