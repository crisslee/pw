package com.coomix.app.all.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.model.bean.ImageInfo;
import com.coomix.app.all.model.response.CommunityImageInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

public class ImageCompressUtils {
    private static final String TAG = "ImageCompressUtils";

    /**
     * GIF图压缩在xMb以内,(减去报文长度)，大小由后台控制，默认5MB
     */
    public final static int IMAGE_MAX_SIZE_GIF = 5 * 1024 * 1024 - 50 * 1024; // gif默认改为100M
    /**
     * android仅提供了png,jpg,webp三种格式的图片压缩<br/>
     * 同时gif的图片暂时没有合适的压缩解决方案<br/>
     * 如果发现解析出的图片格式不在FILE_TYPE_MAP中，默认使用jpg
     */
    private static Map<String, String> FILE_TYPE_MAP;
    private static final String TYPE_JPG = "jpg";
    private static final String TYPE_PNG = "png";
    private static final String TYPE_GIF = "gif";

    /*采样的尺寸阈值*/
    private static final int SIZE_THRESHOLD = 1280;
    /*长宽比例因子阈值*/
    private static final int RATIO_THRESHOLD = 2;
    /*图片压缩质量*/
    private static final int COMPRESS_QUALITY = 62;

    /**
     * 图片处理以int状态码的方式返回，0为界限,0表示处理正常，负数为处理异常，
     * 正数为处理成功了但是有一些条件限制（之前对GIF大于n MB的流量网络需要提醒用户）
     */
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

    static {
        FILE_TYPE_MAP = new HashMap<String, String>();
        FILE_TYPE_MAP.put("ffd8ff", TYPE_JPG);
        FILE_TYPE_MAP.put("89504e", TYPE_PNG);
        FILE_TYPE_MAP.put("474946", TYPE_GIF);
    }

    private static int getGifMaxSize() {
        //3.6.0修改为不限制上传大小。为了更好的支持需求变动，先取后台配置的值再判断用默认的值，默认100M即相当于不限制
        /*int maxSize = BusOnlineApp.getAppConfig().getNetwork_upload_gif_pic_max_filesize();
        if (maxSize > 0)
        {
            return maxSize;
        }*/
        return IMAGE_MAX_SIZE_GIF;
    }

    public static File getCompressFile(Context context, String name) {
        return AllOnlineApp.mApp.getFileStreamPath(name);
    }

    private static File buildCompressFile(Context context, String fileType) {
        return getCompressFile(context, new StringBuilder().append("upload-")
            .append(System.currentTimeMillis())
            .append("-.")
            .append(fileType)
            .toString());
    }

    static void createCompressFile(File compressFile) throws IOException {
        compressFile.getParentFile().mkdirs();
        if (!compressFile.exists()) {
            boolean created = compressFile.createNewFile();
            if (!created) {
                if (Constant.IS_DEBUG_MODE) {
                    Log.e(TAG, "create new file failed. please check your setting.");
                }
            }
        }
    }

    /*获取图片文件类型*/
    static String extractImageType(String imagePath) {
        long extractStart = System.currentTimeMillis();
        String sampleData = "";
        BufferedSource bufferedSource = null;
        try {
            bufferedSource = Okio.buffer(Okio.source(new File(imagePath)));
            //读取前三个字节
            ByteString headers = bufferedSource.readByteString(3);
            sampleData = headers.hex().toLowerCase();
        } catch (FileNotFoundException e) {
            // nothing
        } catch (IOException e) {
            //nothing
        } finally {
            Utils.silentClose(bufferedSource);
        }
        long extractEnd = System.currentTimeMillis();
        if (Constant.IS_DEBUG_MODE) {
            Log.d(TAG, String.format("extract type time interval : %d ms.", extractEnd - extractStart));
        }
        return determineImageType(sampleData);
    }

    /*根据读取的数据，判断图片类型标识*/
    static String determineImageType(String dataSample) {
        if (Utils.isNull(dataSample)) {
            return TYPE_JPG;
        }
        String ret = FILE_TYPE_MAP.get(dataSample);
        if (Utils.isNull(ret)) {
            return TYPE_JPG;
        }
        if (Constant.IS_DEBUG_MODE) {
            Log.d(TAG, String.format("image type : %s.", ret));
        }
        return ret;
    }

    /**
     * 根据图片的Orientation，获取旋转的角度
     */
    static int calcRotateDegree(String imagePath) {
        long exifStart = System.currentTimeMillis();
        int ret = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    ret = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    ret = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    ret = 180;
                    break;
            }
        } catch (IOException e) {
            //Nothing
        }
        long exifEnd = System.currentTimeMillis();
        if (Constant.IS_DEBUG_MODE) {
            Log.d(TAG, String.format("extract exif time interval : %d ms.", exifEnd - exifStart));
            Log.d(TAG, String.format("rotate degree : %d.", ret));
        }
        return ret;
    }

    /**
     * 计算采样比例
     */
    static void calcInSampleSize(int reqWidth, int reqHeight, int width, int height, BitmapFactory.Options options) {
        long calcStart = System.currentTimeMillis();
        int sampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio;
            final int widthRatio;
            if (reqHeight == 0) {
                sampleSize = (int) Math.floor((float) width / (float) reqWidth);
            } else if (reqWidth == 0) {
                sampleSize = (int) Math.floor((float) height / (float) reqHeight);
            } else {
                heightRatio = (int) Math.floor((float) height / (float) reqHeight);
                widthRatio = (int) Math.floor((float) width / (float) reqWidth);
                sampleSize = Math.min(heightRatio, widthRatio);
            }
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        long calcEnd = System.currentTimeMillis();
        if (Constant.IS_DEBUG_MODE) {
            Log.d(TAG, String.format("calc inSample time interval : %d ms.", calcEnd - calcStart));
        }
    }

    static void decodeImageBoundary(String originalImagePath, BitmapFactory.Options options) {
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(originalImagePath, options);
    }

    /*计算压缩的目标尺寸*/
    static int[] calcCompressTargetSize(Context context, BitmapFactory.Options options) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int originWidth = options.outWidth;
        int originHeight = options.outHeight;
        int compressWidth = screenWidth;
        int compressHeight = screenHeight;
        if (originWidth <= SIZE_THRESHOLD && originHeight <= SIZE_THRESHOLD) {
            //都小于1280,保留原尺寸
            return new int[] { originWidth, originHeight };
        } else if (originWidth > SIZE_THRESHOLD && originHeight > SIZE_THRESHOLD) {
            //都大于1280
            boolean widthDetermine = originWidth > originHeight;
            int ratio = widthDetermine ? originWidth / originHeight : originHeight / originWidth;
            if (ratio < RATIO_THRESHOLD) {
                if (widthDetermine) {
                    compressWidth = SIZE_THRESHOLD;
                    compressHeight = Math.round(originHeight * SIZE_THRESHOLD * 1.0f / originWidth);
                } else {
                    compressHeight = SIZE_THRESHOLD;
                    compressWidth = Math.round(originWidth * SIZE_THRESHOLD * 1.0f / originHeight);
                }
                return new int[] { compressWidth, compressHeight };
            } else {
                if (widthDetermine) {
                    compressHeight = SIZE_THRESHOLD;
                    compressHeight = Math.round(originWidth * SIZE_THRESHOLD * 1.0f / originHeight);
                } else {
                    compressWidth = SIZE_THRESHOLD;
                    compressHeight = Math.round((originHeight * SIZE_THRESHOLD * 1.0f / originWidth));
                }
                return new int[] { compressWidth, compressHeight };
            }
        } else {
            //其中一边大于1280，另一边小于1280
            boolean widthDetermine = originWidth > originHeight;
            int ratio = widthDetermine ? originWidth / originHeight : originHeight / originWidth;
            if (ratio < RATIO_THRESHOLD) {
                if (widthDetermine) {
                    compressWidth = SIZE_THRESHOLD;
                    compressHeight = Math.round(originHeight * SIZE_THRESHOLD * 1.0f / originWidth);
                } else {
                    compressHeight = SIZE_THRESHOLD;
                    compressWidth = Math.round(originWidth * SIZE_THRESHOLD * 1.0f / originHeight);
                }
                return new int[] { compressWidth, compressHeight };
            } else {
                //原尺寸返回
                return new int[] { originWidth, originHeight };
            }
        }
    }

    /**
     * 针对图片的目标尺寸以及旋转角度进行处理
     */
    private static Bitmap scaleAndRotateIfNeeded(Matrix matrix, Bitmap bitmap, int[] targetSizes, int rotateDegree) {
        long start = System.currentTimeMillis();
        boolean changed = false;
        if (targetSizes[0] != bitmap.getWidth() && targetSizes[1] != bitmap.getHeight()) {
            changed = true;
            float scaleWidth = ((float) targetSizes[0]) / bitmap.getWidth();
            float scaleHeight = ((float) targetSizes[1]) / bitmap.getHeight();
            matrix.postScale(scaleWidth, scaleHeight);
        }
        if (rotateDegree > 0) {
            changed = true;
            matrix.postRotate(rotateDegree);
        }
        Bitmap newBitmap;
        if (changed) {
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            newBitmap = bitmap;
        }
        long end = System.currentTimeMillis();
        if (changed && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        if (Constant.IS_DEBUG_MODE) {
            Log.d(TAG, String.format("scale and rotate time interval : %d ms.", end - start));
        }
        return newBitmap;
    }

    /*压缩图片，输出到指定的文件*/
    private static String doFinalCompress(Context context, Bitmap bitmap, String fileType) {
        long compressStart = System.currentTimeMillis();
        String ret = null;
        File compressFile = buildCompressFile(context, fileType);
        BufferedSink bufferedSink = null;
        try {
            createCompressFile(compressFile);
            bufferedSink = Okio.buffer(Okio.sink(compressFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, bufferedSink.outputStream());
            ret = compressFile.getAbsolutePath();
        } catch (IOException e) {
            //Nothing
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            Utils.silentClose(bufferedSink);
        }
        long compressEnd = System.currentTimeMillis();
        if (Constant.IS_DEBUG_MODE) {
            Log.d(TAG, String.format("compress time interval : %d ms.", compressEnd - compressStart));
        }
        return ret;
    }

    /**
     * 压缩图片，返回压缩后的缓存地址
     */
    public static int compress(Context context, CommunityImageInfo imageInfo) {
        if (imageInfo == null) {
            return RESULT_PATH_NULL;
        }
        String originalImagePath = imageInfo.getSource_image();
        if (Utils.isNull(originalImagePath)) {
            return RESULT_PATH_NULL;
        }
        String fileType = extractImageType(originalImagePath);
        //Gif图片暂时没有找到合适的压缩算法，直接略过，返回原地址
        if (TextUtils.equals(fileType, TYPE_GIF)) {
            if (new File(originalImagePath).length() > getGifMaxSize()) {
                if (Constant.IS_DEBUG_MODE) {
                    Log.e(TAG, "gif file exceed size restrict.");
                }
                return RESULT_GIF_TOO_LARGE;
            }
            if (Constant.IS_DEBUG_MODE) {
                Log.e(TAG, ">>>>>>>>>>>>>>>>>ignore gif file,return origin file. <<<<<<<<<<<<<<<<<");
            }
            return RESULT_OK;
        }
        int rotateDegree = calcRotateDegree(originalImagePath);
        BitmapFactory.Options options = new BitmapFactory.Options();
        decodeImageBoundary(originalImagePath, options);
        int[] targetSizes = calcCompressTargetSize(context, options);
        calcInSampleSize(targetSizes[0], targetSizes[1], options.outWidth, options.outHeight, options);
        //do real work here
        Bitmap bitmap = BitmapFactory.decodeFile(originalImagePath, options);
        //protection
        //解析失败，可能传入的不是图片文件等，则原地址返回
        if (bitmap == null) {
            if (Constant.IS_DEBUG_MODE) {
                Log.e(TAG, ">>>>>>>>>>decode file fail,return origin file. <<<<<<<<<<<<<<<<<");
            }
            return RESULT_FAIL;
        }
        //scale to appointed size
        Matrix matrix = new Matrix();
        bitmap = scaleAndRotateIfNeeded(matrix, bitmap, targetSizes, rotateDegree);
        String ret = doFinalCompress(context, bitmap, fileType);
        //压缩出错，返回原地址
        if (Utils.isNull(ret)) {
            return RESULT_COMPRESS_EX;
        }
        if (Constant.IS_DEBUG_MODE) {
            Log.e(TAG, ">>>>>>>>>>compress single image accomplished.<<<<<<<<<<<<");
        }
        imageInfo.setSource_image(ret);
        imageInfo.setWidth(targetSizes[0]);
        imageInfo.setHeight(targetSizes[1]);
        return RESULT_OK;
    }

    /**
     * 压缩图片，返回压缩后的缓存地址
     */
    public static int compress(Context context, ImageInfo imageInfo) {
        if (imageInfo == null) {
            return RESULT_PATH_NULL;
        }
        String originalImagePath = imageInfo.getSource_image();
        if (Utils.isNull(originalImagePath)) {
            return RESULT_PATH_NULL;
        }
        String fileType = extractImageType(originalImagePath);
        //Gif图片暂时没有找到合适的压缩算法，直接略过，返回原地址
        if (TextUtils.equals(fileType, TYPE_GIF)) {
            if (new File(originalImagePath).length() > getGifMaxSize()) {
                if (Constant.IS_DEBUG_MODE) {
                    Log.e(TAG, "gif file exceed size restrict.");
                }
                return RESULT_GIF_TOO_LARGE;
            }
            if (Constant.IS_DEBUG_MODE) {
                Log.e(TAG, ">>>>>>>>>>>>>>>>>ignore gif file,return origin file. <<<<<<<<<<<<<<<<<");
            }
            return RESULT_OK;
        }
        int rotateDegree = calcRotateDegree(originalImagePath);
        BitmapFactory.Options options = new BitmapFactory.Options();
        decodeImageBoundary(originalImagePath, options);
        int[] targetSizes = calcCompressTargetSize(context, options);
        calcInSampleSize(targetSizes[0], targetSizes[1], options.outWidth, options.outHeight, options);
        //do real work here
        Bitmap bitmap = BitmapFactory.decodeFile(originalImagePath, options);
        //protection
        //解析失败，可能传入的不是图片文件等，则原地址返回
        if (bitmap == null) {
            if (Constant.IS_DEBUG_MODE) {
                Log.e(TAG, ">>>>>>>>>>decode file fail,return origin file. <<<<<<<<<<<<<<<<<");
            }
            return RESULT_FAIL;
        }
        //scale to appointed size
        Matrix matrix = new Matrix();
        bitmap = scaleAndRotateIfNeeded(matrix, bitmap, targetSizes, rotateDegree);
        String ret = doFinalCompress(context, bitmap, fileType);
        //压缩出错，返回原地址
        if (Utils.isNull(ret)) {
            return RESULT_COMPRESS_EX;
        }
        if (Constant.IS_DEBUG_MODE) {
            Log.e(TAG, ">>>>>>>>>>compress single image accomplished.<<<<<<<<<<<<");
        }
        imageInfo.setSource_image(ret);
        imageInfo.setWidth(targetSizes[0]);
        imageInfo.setHeight(targetSizes[1]);
        return RESULT_OK;
    }

    //for demo usage
    public static int[] decodeImageSize(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return new int[] { options.outWidth, options.outHeight };
    }
}
