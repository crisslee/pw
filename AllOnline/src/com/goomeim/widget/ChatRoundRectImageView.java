package com.goomeim.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.coomix.app.all.R;

/**
 * 自定义的圆角矩形ImageView，可以直接当组件在布局中使用。
 *
 * @author lsj
 */
public class ChatRoundRectImageView extends ImageView
{
    private Paint paint;
    private int back = R.drawable.ease_chatfrom_bg_normal;
    private float srcWidth, srcHeight;

    public void setBack(int resId)
    {
        this.back = resId;
    }

    public ChatRoundRectImageView(Context context)
    {
        this(context, null);
    }

    public ChatRoundRectImageView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ChatRoundRectImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        paint = new Paint();
    }

    public void setSrcWidthHeight(float srcWidth, float srcHeight)
    {
        this.srcWidth = srcWidth;
        this.srcHeight = srcHeight;
    }

    /**
     * 绘制圆角矩形图片
     *
     * @author lsj
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        Drawable drawable = getDrawable();
        if (null != drawable)
        {
            if (!(drawable instanceof BitmapDrawable))
            {
                super.onDraw(canvas);
                return;
            }
            if (back == 0)
            {
                back = R.drawable.ease_chatfrom_bg_normal;
            }
            Bitmap bitmap_bg = BitmapFactory.decodeResource(getResources(), back);
            Bitmap bitmap_in = ((BitmapDrawable) drawable).getBitmap();
            float width = getWidth();
            float height = getHeight();
            if (srcWidth < 1f || srcHeight < 1f)
            {
                srcWidth = bitmap_in.getWidth();
                srcHeight = bitmap_in.getHeight();
            }
            if (srcWidth / width > srcHeight / height)
            {
                height = width * srcHeight / srcWidth;
                if (height < width / 4)
                {
                    height = width / 4;
                }
            }
            else
            {
                width = height * srcWidth / srcHeight;
                if (width < height / 4)
                {
                    width = height / 4;
                }
            }
            Bitmap bp = getRoundCornerImage(bitmap_bg, bitmap_in, (int) width, (int) height);
            Bitmap bp2 = getShardImage(bitmap_bg, bp, (int) width, (int) height);
            // srcWidth = bp2.getWidth();
            // srcHeight = bp2.getHeight();
            Rect rectSrc = new Rect(0, 0, (int) width, (int) height);
            Rect rectDest = new Rect(0, 0, (int) width, (int) height);

            paint.reset();
            canvas.drawBitmap(bp2, rectSrc, rectDest, paint);
        }
        else
        {
            super.onDraw(canvas);
        }
    }

    public Bitmap getRoundCornerImage(Bitmap bitmap_bg, Bitmap bitmap_in, int width, int height)
    {
        Bitmap roundConcerImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(roundConcerImage);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        Rect rectF = new Rect(0, 0, bitmap_in.getWidth(), bitmap_in.getHeight());
        paint.setAntiAlias(true);
        NinePatch patch = new NinePatch(bitmap_bg, bitmap_bg.getNinePatchChunk(), null);
        patch.draw(canvas, rect);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap_in, rectF, rect, paint);
        return roundConcerImage;
    }

    public Bitmap getShardImage(Bitmap bitmap_bg, Bitmap bitmap_in, int width, int height)
    {
        Bitmap roundConcerImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(roundConcerImage);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        paint.setAntiAlias(true);
        NinePatch patch = new NinePatch(bitmap_bg, bitmap_bg.getNinePatchChunk(), null);
        patch.draw(canvas, rect);
        Rect rect2 = new Rect(2, 2, width - 2, height - 2);
        canvas.drawBitmap(bitmap_in, rect, rect2, paint);
        return roundConcerImage;
    }
}
