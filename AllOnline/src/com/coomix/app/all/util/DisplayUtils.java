package com.coomix.app.all.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.widget.TextView;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayUtils {
    /**
     * 将px值转换为dp值
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将dp值转换为px值
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 将px值转换为sp值
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidthPixels(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeightPixels(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }

    public static SpannableString getEmojiContent(final Context context, final TextView tv, CharSequence source) {
        if (TextUtils.isEmpty(source)) {
            return new SpannableString("");
        }
        SpannableString spannableString = new SpannableString(source);
        try {
            Resources res = context.getResources();

            // String regexEmoji = "\\[([\u4e00-\u9fa5\\w])+\\]";
            // 限定1-6个中文
            String regexEmoji = "\\[([\u4e00-\u9fa5\\w]){1,6}\\]";
            Pattern patternEmoji = Pattern.compile(regexEmoji);
            Matcher matcherEmoji = patternEmoji.matcher(spannableString);
            float textSize = tv.getTextSize();
            while (matcherEmoji.find()) {
                // 获取匹配到的具体字符
                String key = matcherEmoji.group();
                // 匹配字符串的开始位置
                int start = matcherEmoji.start();
                // 利用表情名字获取到对应的图片
                Integer imgRes = EmojiUtils.getImgByName(key);
                if (imgRes != null && imgRes > 0) {
                    // 压缩表情图片
                    spannableString =
                        setImageSpan(imgRes, textSize, res, spannableString, context, start, start + key.length());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return spannableString;
    }

    static Map<Integer, SoftReference<Bitmap>> imageCache = new HashMap<Integer, SoftReference<Bitmap>>();

    public static SpannableString setImageSpan(Integer imgRes, float textSize, Resources res,
        SpannableString spannString, Context context, int start, int end) {
        if (imgRes != null && imgRes > 0) {
            // 压缩表情图片
            int size = (int) (textSize * 1.1);
            Bitmap bitmap = null;
            if (imageCache.get(imgRes) != null) {
                bitmap = imageCache.get(imgRes).get();
            }
            if (bitmap == null || bitmap.isRecycled()) {
                bitmap = BitmapFactory.decodeResource(res, imgRes);
            }
            Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
            // if (bitmap != scaleBitmap) {
            // bitmap.recycle();
            // }
            ImageSpan span = new CommunityImageSpan(context, scaleBitmap);
            // if(VERSION.SDK_INT > Build.VERSION_CODES.KITKAT &&
            // VERSION.SDK_INT < 22) {
            // span = new ImageSpan(context, scaleBitmap);
            // } else {
            // span = new ImageSpan(context, scaleBitmap,
            // ImageSpan.ALIGN_BASELINE);
            // }
            spannString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannString;
    }
}
