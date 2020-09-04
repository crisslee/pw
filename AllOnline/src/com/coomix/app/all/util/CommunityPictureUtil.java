package com.coomix.app.all.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.bumptech.GlideApp;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.R;
import com.coomix.app.all.share.PopupWindowUtil;
import com.coomix.app.all.share.TextSet;
import com.muzhi.camerasdk.utils.FileType;
import com.muzhi.camerasdk.utils.FileUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutionException;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class CommunityPictureUtil {
    private final static String TAG = "PictureUtil";
    public final static int MAX_HEIGHT = 1920;
    public final static int MAX_WIDTH = 1080;
    /**
     * 普通图片默认为200KBK
     */
    private final static int IMAGE_MAX_SIZE_200K = 1024 * 200;
    /**
     * 长图压缩在1Mb以内,(减去报文长度)
     */
    private final static int IMAGE_MAX_SIZE_1MB = 1024 * 1024 - 50 * 1024;
    /**
     * GIF图压缩在xMb以内,(减去报文长度)，由后台配置（2017.01.03 社区第一个版本暂无配置）,默认4MB
     */
    private final static int IMAGE_MAX_SIZE_GIF = 4 * 1024 * 1024 - 50 * 1024;

    /**
     * 图片处理以int状态码的方式返回，0为界限,0表示处理正常，负数为处理异常，正数为处理成功了但是有一些条件限制（之前对GIF大于n MB的流量网络需要提醒用户）
     * */
    /**
     * 图片压缩以及拷贝正常
     */
    public static final int RESULT_OK = 0;
    /**
     * 图片压缩以及拷贝异常
     */
    public static final int RESULT_FAIL = -1;
    /**
     * 选择的GIF图片超出上传限制
     */
    public static final int RESULT_GIF_TOO_LARGE = -2;
    /**
     * 选择本地图片路径为空
     */
    public static final int RESULT_PATH_NULL = -3;
    /**
     * 图片异常
     */
    public static final int RESULT_COMPRESS_EX = -4;

    public static Bitmap getBmp(String path, int size) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回bm为空

        // System.out.println("文件初始大小"+options.outWidth+":"+options.outHeight);
        double scale = (double) options.outWidth / (double) options.outHeight;
        double width = Math.sqrt(scale * size);
        double height = Math.sqrt(size / scale);
        // 计算缩放比
        int beWidth = (int) (options.outWidth / width);
        int beHeight = (int) (options.outHeight / height);
        int be = Math.max(beWidth, beHeight);
        // System.out.println("文件缩放大小"+width+":"+height);
        if (be <= 0) {
            be = 1;
        }
        // System.out.println("文件比例大小"+be);
        options.inSampleSize = be;
        // 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(path, options);
        // System.out.println("文件bitm大小"+bitmap.getWidth()+":"+bitmap.getHeight());
        Bitmap newbitmap = bitmap;
        try {
            newbitmap = rotaingImageView(readPictureDegree(path), bitmap);
        } catch (Throwable e) {
            System.gc();
            if (Constant.IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            try {
                newbitmap = rotaingImageView(readPictureDegree(path), bitmap);
            } catch (Throwable e2) {
                if (Constant.IS_DEBUG_MODE) {
                    e2.printStackTrace();
                }
            }
        }
        // bitmap.recycle();
        return newbitmap;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation =
                exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            // Log.i("PhotoView", "=========orientation: " + orientation);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        if (angle == 0) {
            return bitmap;
        }
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        if (Constant.IS_DEBUG_MODE) {
            System.out.println("angle2=" + angle);
        }
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public static int getGifMaxSize() {
        //3.6.0修改为不限制上传大小。为了更好的支持需求变动，先取后台配置的值再判断用默认的值，默认100M即相当于不限制
        //int maxSize = BusOnlineApp.getAppConfig().getNetwork_upload_gif_pic_max_filesize();
        //if (maxSize > 0)
        //{
        //    return maxSize;
        //}
        return IMAGE_MAX_SIZE_GIF;
    }

    /**
     * GIF图片不做压缩，直接拷贝
     **/
    private static int copyGifImg(String sourceImgPath, File desFile, int maxSize) {
        File sourceFile = new File(sourceImgPath);
        if (sourceFile == null || !sourceFile.exists()) {
            return RESULT_PATH_NULL;
        }

        long length = sourceFile.length();
        if (length <= maxSize) {
            return FileUtils.copyFile(sourceImgPath, desFile.getAbsolutePath()) ? RESULT_OK : RESULT_FAIL;
        } else {
            return RESULT_GIF_TOO_LARGE;
        }
    }

    private static ByteArrayOutputStream compressJpegImg(Bitmap bmp, String sourceImgPath, int maxSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        bmp = BitmapFactory.decodeFile(sourceImgPath, options);
        int inSampleSize = 1;
        boolean bLongBigBitmap = PhotoViewAttacher.isLongImg(options.outWidth, options.outHeight);
        if (!bLongBigBitmap) {
            // 普通图片
            inSampleSize = calculateInSampleSize(options);
        }

        int quality = 95; // 默认值95，即对所有图片都默认压缩一次，不管原始图片大小，先压缩一次之后再对应处理
        if (inSampleSize > 1) {
            /**
             * 对于普通图片压缩比大于2的，第一次的默认质量压缩做大些，防止OOM 经测试10MB的图片inSampleSize= 1，
             * 即仅仅被80%的质量压缩后大概在1.x Mb
             */
            quality = 81;
        }

        BitmapFactory.Options newOptions = new BitmapFactory.Options();
        newOptions.inSampleSize = inSampleSize;
        newOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        bmp = BitmapFactory.decodeFile(sourceImgPath, newOptions);
        try {
            bmp = rotaingImageView(readPictureDegree(sourceImgPath), bmp);
        } catch (Throwable e) {
            System.gc();
            if (Constant.IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            try {
                bmp = rotaingImageView(readPictureDegree(sourceImgPath), bmp);
            } catch (Throwable e2) {
                if (Constant.IS_DEBUG_MODE) {
                    e2.printStackTrace();
                }
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, os);

        if (Constant.IS_DEBUG_MODE) {
            Log.i(TAG, "==缩放并压缩质量一次后图片大小: "
                + (os.toByteArray().length / 1024)
                + "KB, 压缩质量:"
                + quality
                + "%, 缩放倍数: "
                + inSampleSize);
        }
        if (bLongBigBitmap) {
            /** 长图压缩在1MB以内 */
            bmp = compressLongImg(bmp, os, quality);
        } else {
            /** 普通图片压缩在200Kb以内 */
            bmp = compressNormalImg(bmp, os, quality, maxSize);
        }
        return os;
    }

    /**
     * 长图压缩在1MB以内
     */
    private static Bitmap compressLongImg(Bitmap bmp, ByteArrayOutputStream os, int quality) {
        if (os.toByteArray().length / 1024 > 5 * 1024) {
            quality = 60;
        }
        int i = 0;
        while (os.toByteArray().length > IMAGE_MAX_SIZE_1MB && i < 20) {
            i++;
            try {
                os.reset();
                quality = quality * 90 / 100;
                if (quality <= 0) {
                    quality = 5;
                }
                // Log.i(TAG, "==长图压缩质量quality: " + quality + "%， 压缩次数： " + (i +
                // 1));
                bmp.compress(Bitmap.CompressFormat.JPEG, quality, os);
            } catch (Exception e) {
            }
        }
        if (Constant.IS_DEBUG_MODE) {
            Log.i(TAG, "长图：" + os.toByteArray().length / 1024 + "Kb");
        }
        return bmp;
    }

    /**
     * 普通图片压缩在200Kb以内
     */
    private static Bitmap compressNormalImg(Bitmap bmp, ByteArrayOutputStream os, int quality, int maxSize) {
        int length = os.toByteArray().length / 1024;
        if (length >= 1000) {
            quality = 20;
        } else if (length >= 300) {
            quality -= (length - 200) / 20 * 0.8;
        }

        if (quality <= 0) {
            quality = 50;
        }
        int i = 0;

        while (os.toByteArray().length > maxSize && i < 20) {
            i++;
            try {
                os.reset();
                quality = quality * 91 / 100;
                if (quality <= 0) {
                    quality = 5;
                }
                if (Constant.IS_DEBUG_MODE) {
                    Log.i(TAG, "==普通图片压缩质量quality: " + quality + "%，  压缩次数： " + (i + 1));
                }
                bmp.compress(Bitmap.CompressFormat.JPEG, quality, os);
            } catch (Exception e) {
            }
        }
        if (Constant.IS_DEBUG_MODE) {
            Log.i(TAG, "普通：" + os.toByteArray().length / 1024 + "Kb");
        }
        return bmp;
    }

    /**
     * 根据图片的宽高,以定义的MAX_WIDTH和MAX_HEIGHT做参照，计算图片需要缩放的倍数
     **/
    private static int calculateInSampleSize(BitmapFactory.Options options) {
        final int imageHeight = options.outHeight;
        final int imageWidth = options.outWidth;

        if (Constant.IS_DEBUG_MODE) {
            Log.i(TAG, "==图片的原始width*height: " + imageWidth + " * " + imageHeight);
        }
        if (imageWidth <= MAX_WIDTH && imageHeight <= MAX_HEIGHT) {
            return 1;
        } else {
            double scale = imageWidth >= imageHeight ? imageWidth / MAX_WIDTH : imageHeight / MAX_HEIGHT;
            double log = Math.log(scale) / Math.log(2);
            double logCeil = Math.ceil(log);// 向上舍入
            return (int) Math.pow(2, logCeil);// 2的x数倍
        }
    }

    /**
     * 获取指定文件大小
     *
     * @throws IOException 读取错误
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            System.out.println("文件--getFileSize11-" + System.currentTimeMillis());
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
            System.out.println("文件--getFileSize12-" + System.currentTimeMillis() + "  " + size);
        } else {
            // file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }

    private static long getFileSize(String file) {
        long size = 0;
        RandomAccessFile mRandomAccessFile;
        try {
            System.out.println("文件--getFileSize21-" + System.currentTimeMillis());
            mRandomAccessFile = new RandomAccessFile(file, "r");
            size = mRandomAccessFile.length();
            System.out.println("文件--getFileSize22-" + System.currentTimeMillis() + "  " + size);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    public static String getSavedDir() {
        String newDirPath = Environment.getExternalStorageDirectory() + "/coomix/pic";
        File f = new File(newDirPath);
        if (!f.exists() || !f.isDirectory()) {
            f.mkdirs();
        }
        return newDirPath;
    }

    public static String getSavedFile(String type) {
        return getSavedFilePath(0, type);
    }

    private static String getSavedFilePath(int num, String type) {
        String newDirPath = getSavedDir();
        String name = "";
        if (CommunityUtil.isEmptyTrimString(name)) {
            name = "coomix_" + System.currentTimeMillis();
        }
        if (num > 0) {
            name = name + "_" + num;
        }
        String newPath = newDirPath + File.separator + name + type;
        File newFile = new File(newPath);
        if (newFile.exists() && newFile.isFile()) {
            return getSavedFilePath(++num, type);
        }
        return newPath;
    }

    /**
     * 复制单个文件
     *
     * @param oldFile File 原文件
     * @param type 为null则默认为jpeg, type 文件格式，例如.JPEG
     * @return boolean
     */
    public static String copyCacheFileToCoomixDir(Context context, File oldFile, String type) {
        String newPath;
        Uri uri;
        try {
            if (BitmapUtils.getAvailableSDcard2(context)) {
                // int bytesum = 0;
                int byteread = 0;
                if (oldFile != null && oldFile.exists()) { // 文件存在时
                    newPath = getSavedFile(type);
                    File newFile = new File(newPath);
                    InputStream inStream = new FileInputStream(oldFile); // 读入原文件
                    FileOutputStream fs = new FileOutputStream(newFile);
                    byte[] buffer = new byte[1024];
                    while ((byteread = inStream.read(buffer)) != -1) {
                        // bytesum += byteread; // 字节数 文件大小
                        fs.write(buffer, 0, byteread);
                    }
                    fs.flush();
                    inStream.close();
                    fs.close();
                    uri = Uri.fromFile(newFile);
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        if (uri != null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(uri);
            context.sendBroadcast(intent);
        }
        return newPath;
    }

    /**
     * 根据图片File进行保存
     */
    public static void showMorePopWindow(final Context context, final View viewParent, final File localPicFile) {
        if (localPicFile == null || !localPicFile.exists()) {
            Toast.makeText(context, R.string.save_picture_failed_downloading, Toast.LENGTH_SHORT).show();
            return;
        }
        TextSet setItem1 = null;
        TextSet setItem2 = new TextSet(R.string.save_pictrue, false, new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 保存图片
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String type = FileType.JPEG;
                            String path = localPicFile.getPath();
                            if (path != null && path.contains(".")) {
                                type = path.substring(path.lastIndexOf(".")).trim();
                            }
                            if (!type.equals(FileType.GIF)) {
                                type = FileType.JPEG;
                            }

                            final String result = copyCacheFileToCoomixDir(context, localPicFile, type);

                            viewParent.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (CommunityUtil.isEmptyTrimString(result)) {
                                        Toast.makeText(context, R.string.save_picture_failed, Toast.LENGTH_SHORT)
                                            .show();
                                    } else {
                                        Toast.makeText(context, R.string.save_picture_success, Toast.LENGTH_SHORT)
                                            .show();
                                    }
                                }
                            });
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        PopupWindowUtil.showPopWindow(context, viewParent, R.string.save_picture_hint, setItem1, setItem2, true);
    }

    /**
     * 根据图片网络地址进行保存
     */
    public static void showMorePopWindow(final Context context, final View viewParent, final String path) {
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(context, R.string.save_picture_failed_pathisempty, Toast.LENGTH_SHORT).show();
            return;
        }
        TextSet setItem1 = null;
        TextSet setItem2 = new TextSet(R.string.save_pictrue, false, new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 保存图片
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            File file = null;
                            String type = FileType.JPEG;
                            if (path.contains(".")) {
                                type = path.substring(path.lastIndexOf(".")).trim();
                            }
                            if (!type.equals(FileType.JPEG) && !type.equals(FileType.GIF)) {
                                type = FileType.JPEG;
                            }
                            try {
                                // 大图预览的时候，已经缓存的是全尺寸的图片，此方法也相当于是从缓存拿的图片

                                file = GlideApp.with(context)
                                    .load(path)
                                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                    .get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            final String result;
                            if (file == null || !file.exists()) {
                                result = "";
                            } else {
                                result = copyCacheFileToCoomixDir(context, file, type);
                            }

                            viewParent.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (CommunityUtil.isEmptyTrimString(result)) {
                                        Toast.makeText(context, R.string.save_picture_failed, Toast.LENGTH_SHORT)
                                            .show();
                                    } else {
                                        Toast.makeText(context, R.string.save_picture_success, Toast.LENGTH_SHORT)
                                            .show();
                                    }
                                }
                            });
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        PopupWindowUtil.showPopWindow(context, viewParent, R.string.save_picture_hint, setItem1, setItem2, true);
    }

    public static void showMorePopWindow(final Context context, final View viewParent, final PhotoView photoView) {
        if (photoView == null) {
            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
            return;
        }
        TextSet setItem1 = null;
        TextSet setItem2 = new TextSet(R.string.save_pictrue, false, new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 保存图片
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = photoView.getDrawingCache();
                            final File file = new File(getSavedFile(FileType.JPEG));
                            try {
                                file.createNewFile();
                                FileOutputStream out = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                out.flush();
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                viewParent.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                            Uri uri = Uri.fromFile(file);
                            if (uri != null) {
                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                intent.setData(uri);
                                context.sendBroadcast(intent);
                            }

                            viewParent.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        PopupWindowUtil.showPopWindow(context, viewParent, R.string.save_pictrue_hint, setItem1, setItem2, true);
    }

    public static Bitmap drawable2Bitamp(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (w <= 0) {
            w = MAX_WIDTH;
        }
        if (h <= 0) {
            h = MAX_HEIGHT;
        }
        Bitmap.Config config =
            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 一般在预览本地图片时候调用，重新使用图片的宽高，避免图片的分辨率过大造成OOM 根据当前手机屏幕的分辨率做图片缩放
     */
    public static int[] getBitmapResizeWidthHeight(String filePath) {
        int defaultWidth = AllOnlineApp.screenWidth;
        int defaultHeight = AllOnlineApp.screenHeight;
        if (TextUtils.isEmpty(filePath)) {
            return new int[] { defaultWidth, defaultHeight };
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int[] widthHeight = new int[] { options.outWidth, options.outHeight };
        if (!PhotoViewAttacher.isLongImg(options.outWidth, options.outHeight)) {
            float widthScale = options.outWidth / defaultWidth;
            float heightScale = options.outHeight / defaultHeight;
            if (widthScale > 1 || heightScale > 1) {
                float num = widthScale > heightScale ? widthScale : heightScale;
                widthHeight[0] = (int) (options.outWidth / num);
                widthHeight[1] = (int) (options.outHeight / num);
            }
        }

        if (widthHeight[0] <= 0 || widthHeight[1] <= 0) {
            widthHeight[0] = defaultWidth;
            widthHeight[1] = defaultHeight;
        }

        return widthHeight;
    }

    public static Bitmap downloadPicByUrl(Context context, String picUrl) {
        Bitmap bitmap = null;

        try {
            FutureTarget<Drawable> futureTarget =
                GlideApp.with(context).load(picUrl).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            bitmap = ((BitmapDrawable) futureTarget.get()).getBitmap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
