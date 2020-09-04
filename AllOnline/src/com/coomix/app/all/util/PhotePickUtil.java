package com.coomix.app.all.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.muzhi.camerasdk.model.CameraSdkParameterInfo;
import com.muzhi.camerasdk.utils.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotePickUtil {

    /**
     * 选择相机
     */
    public static File showCameraAction(Activity activity, File file) {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            file = FileUtils.createTmpFile(activity);
            try {
                File dir = file.getParentFile();
                if (dir != null && !dir.exists()) {
                    dir.mkdirs();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            activity.startActivityForResult(cameraIntent, CameraSdkParameterInfo.TAKE_PICTURE_FROM_CAMERA);
        } else {
            Toast.makeText(activity, R.string.camerasdk_msg_no_camera, Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    /**
     * 调用系统的裁剪
     */
    public static void cropPhoto(Activity activity, Uri uri, int requestId, Uri tempUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", activity.getResources().getDimensionPixelSize(R.dimen.crop_photo_size));
        intent.putExtra("outputY", activity.getResources().getDimensionPixelSize(R.dimen.crop_photo_size));
        // intent.putExtra("return-data", true);
        intent.putExtra("return-data", false);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        activity.startActivityForResult(intent, requestId);
    }

    public static String saveBitmap(Bitmap mBitmap, String name) {
        String result = null;
        FileOutputStream b = null;
        File file = AllOnlineApp.mApp.getFilesDir();
        file.mkdirs();// 创建文件夹
        String filePath = file.getPath() + File.separator + name;// 图片名字
        try {
            b = new FileOutputStream(filePath);
            // 把数据写入文件
            if (mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b)) {
                result = filePath;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭流
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
