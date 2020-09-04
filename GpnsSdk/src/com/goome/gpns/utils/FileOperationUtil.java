package com.goome.gpns.utils;

import android.os.Environment;
import com.goome.gpns.GPNSInterface;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FileOperationUtil {
    private static String defaultDir = "GpnsSDK";
    private static String ERR_FILE_NAME = "AppErrLog.txt";
    private static String FINGER_PRINT_FILE_NAME = "FingerPrint.txt";
    private static String EXCEPTION_FILE_NAME = "Exception.txt";

    public static void saveToFile(String content, String filename) {
        saveToFile(content, getDefaultDirectory(), filename);
    }

    public static synchronized void saveToFile(String content, String directory, String filename) {
        try {
            File dirFile = new File(directory);
            if (!dirFile.exists()) {
                boolean success = dirFile.mkdirs();
                if (!success) {
                    LogUtil.showMsg("文件夹:" + dirFile.getName() + "创建失败");
                } else {
                    LogUtil.showMsg("文件夹:" + dirFile.getName() + "创建成功");
                }
            }
            File file = new File(directory + File.separator + filename);
            if (!file.exists()) {
                LogUtil.i("Create the file:" + file.getName());
                boolean success = file.createNewFile();
                if (!success) {
                    LogUtil.showMsg("文件:" + file.getName() + "创建失败");
                } else {
                    LogUtil.showMsg("文件:" + file.getName() + "创建成功");
                }
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            content = DateUtil.getCurrentDate() + "=>" + content + "\n\n";
            raf.write(content.getBytes());
            raf.close();
            LogUtil.i(content + "保存到文件:/" + file.getName());
        } catch (FileNotFoundException e) {
            LogUtil.e("saveToFile(String content,String directory, String filename) occur an exception");
            LogUtil.printException2Console(e);
            LogUtil.showMsg("saveToFile occur an exception");
        } catch (Exception e) {
            LogUtil.e("saveToFile(String content,String directory, String filename) occur an exception");
            LogUtil.printException2Console(e);
            LogUtil.showMsg("saveToFile occur an exception");
        }
    }

    public static void saveErrMsgToFile(String errMsg) {
        LogUtil.e(errMsg);
        saveToFile(errMsg, getDefaultDirectory(), ERR_FILE_NAME);
    }

    public static void saveFingerprintInfoToFile(String info) {
        saveToFile(info, getDefaultDirectory(), FINGER_PRINT_FILE_NAME);
    }

    public static void saveExceptionInfoToFile(String exceptionInfo) {
        LogUtil.e(exceptionInfo);
        // 获取不到包名引起死循环
        saveToFile(exceptionInfo, getDefaultDirectory(), EXCEPTION_FILE_NAME);
    }

    public static String getDefaultDirectory() {
        String rootDirectory = getSDPath1();
        return rootDirectory;
    }

    /**
     * 获取sd卡路径 .双sd卡时，根据“设置”里面的数据存储位置选择，获得的是内置sd卡还是外置sd卡
     *
     * @return sd卡路径
     */
    private static String getSDPath() {
        String sdcardPath = null;
        // 判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath(); // 获取根目录
        } else {
            // FileOperationUtil.saveExceptionInfoToFile("SD卡不存在");
            LogUtil.showMsg("SD卡不存在");
        }
        if (sdcardPath != null) {
            return sdcardPath;
        } else {
            return "";
        }
    }

    private static String getSDPath1() {
        String sdcardPath = "";
        // 首先判断是否有外部存储卡，如没有判断是否有内部存储卡，如没有，继续读取应用程序所在存储
        if (getExternalSdCardPath() != null) {
            sdcardPath = getExternalSdCardPath();
        } else if (GPNSInterface.appContext != null) {
            sdcardPath = GPNSInterface.appContext.getFilesDir().getAbsolutePath() + File.separator + defaultDir;
        } else {
            LogUtil.showMsg("SD卡不存在");
        }
        return sdcardPath;
    }

    /**
     * 遍历 "system/etc/vold.fstab” 文件，获取全部的Android的挂载点信息
     */
    private static ArrayList<String> getDevMountList() {
        ArrayList<String> out = new ArrayList<String>();
        String fstab = FileUtils.readFile("/system/etc/vold.fstab");
        if (fstab != null && fstab.length() != 0) {
            String[] toSearch = fstab.split(" ");
            for (int i = 0; i < toSearch.length; i++) {
                if (toSearch[i].contains("dev_mount")) {
                    if (new File(toSearch[i + 2]).exists()) {
                        out.add(toSearch[i + 2]);
                    }
                }
            }
        }

        return out;
    }

    /**
     * 获取扩展SD卡存储目录
     *
     * 如果有外接的SD卡，并且已挂载，则返回这个外置SD卡目录 否则：返回内置SD卡目录
     */
    public static String getExternalSdCardPath() {
        String path = null;
        File sdCardFile = null;

        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)
            && GPNSInterface.appContext != null) {
            sdCardFile = GPNSInterface.appContext.getExternalFilesDir(defaultDir);
            return sdCardFile != null ? sdCardFile.getAbsolutePath() : null;
        }

        ArrayList<String> devMountList = getDevMountList();

        for (String devMount : devMountList) {
            File file = new File(devMount);

            if (file.isDirectory() && file.canWrite()) {
                path = file.getAbsolutePath();

                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                File testWritable = new File(path, "test_" + timeStamp);

                if (testWritable.mkdirs()) {
                    testWritable.delete();
                    break;
                } else {
                    path = null;
                }
            }
        }

        if (path != null) {
            sdCardFile = new File(path);
            return sdCardFile.getAbsolutePath();
        }

        return null;
    }
}
