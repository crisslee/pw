package com.coomix.app.all.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {
    public static final int BUFFER = 1024;
    /** gzi压缩拓展名 .gz */
    public static final String EXT = ".gz";

    /**
     * 进行gzip压缩，生成文件名为file文件名+{@link #EXT} (.gz)
     */
    public static String fileToGzip(File file) {
        InputStream is = null;
        OutputStream os = null;
        GZIPOutputStream gos = null;
        try {
            String gzPath = file.getAbsolutePath() + EXT;
            os = new FileOutputStream(gzPath);
            is = new FileInputStream(file);
            gos = new GZIPOutputStream(os);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = is.read(data, 0, BUFFER)) != -1) {
                gos.write(data, 0, count);
            }
            return gzPath;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                gos.finish();
                gos.flush();
                gos.close();
                os.flush();
                os.close();
                is.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }
}
