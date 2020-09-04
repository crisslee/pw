package com.coomix.app.all.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.R;
import com.coomix.app.all.model.response.Picture;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ViewUtil {

    /**
     * 避免偶发性bug: 进入界面后刷新不出数据，此后下拉刷新失去请求数据的功能
     *
     * @param ptrBase
     */
    public static void refreshDataDelay(final PullToRefreshBase ptrBase) {
        refreshDataDelay(ptrBase, false);
    }

    /**
     * 避免偶发性bug: 进入界面后刷新不出数据，此后下拉刷新失去请求数据的功能
     *
     * @param ptrBase
     * @param resetToBoth 重新设置mode为Mode.BOTH
     */
    public static void refreshDataDelay(final PullToRefreshBase ptrBase, final boolean resetToBoth) {
        if (ptrBase != null) {
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (ptrBase != null) {
                        ptrBase.post(new Runnable() {
                            @Override
                            public void run() {
                                if (ptrBase != null) {
                                    ptrBase.setRefreshing();
                                    if (resetToBoth) {
                                        ptrBase.setMode(Mode.BOTH);
                                    }
                                }
                            }
                        });
                    }
                }

                ;
            }.start();
        }
    }

    public static SpannableStringBuilder changeTextColor(CharSequence str, int color, int start, int end) {
        SpannableStringBuilder builder = new SpannableStringBuilder(str);
        builder.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    /**
     * 判断ListView的内容是否全部显示；如果有足够的空间全部显示，关闭上拉加载更多功能及不显示“别拉了，到底啦”View；
     *
     * @param parentHeight parentView的高度
     * @param listView
     * @return
     */
    public static boolean isListViewShowCompletely(int parentHeight, ListView listView) {
        if (parentHeight <= 0) {
            return false;
        }

        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return true;
        }

        int itemsHeight = 0;

        int i = listView.getHeaderViewsCount();
        int j = listView.getFooterViewsCount();
        int total = listAdapter.getCount();
        int max = total - j;
        int dividerCount = total - i - j;

        for (; i < max; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (null != listItem && listItem.getVisibility() != View.GONE) {
                listItem.measure(0, 0);
                itemsHeight += listItem.getMeasuredHeight();
                if (itemsHeight > parentHeight) {
                    return false;
                }
            } else {
                break;
            }
        }

        int totalHeight = itemsHeight + (listView.getDividerHeight() * dividerCount);

        int offset = (int) (30 * ResizeUtil.getResizeFactor());
        return parentHeight > totalHeight + offset;
    }

    /**
     * 计算ListView的高度
     *
     * @param listView
     * @return
     */
    public static int caculateListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return 0;
        }

        int itemsHeight = 0;

        int i = listView.getHeaderViewsCount();
        int max = listAdapter.getCount() - listView.getFooterViewsCount();
        int dividerCount = max - i;

        for (; i < max; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (null != listItem && listItem.getVisibility() != View.GONE) {
                listItem.measure(0, 0);
                itemsHeight += listItem.getMeasuredHeight();
            } else {
                break;
            }
        }

        int totalHeight = itemsHeight + (listView.getDividerHeight() * dividerCount);
        return totalHeight;
    }

    /**
     * 设置Listview的高度
     */
    public static void setListViewHeight(ListView listView) {
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = caculateListViewHeight(listView);
        listView.setLayoutParams(params);
    }

    public static final int MAX_TEXT_LENGTH = 200;
    public static final int MAX_TEXT_LENGTH_COMMENT = 300;

    public static void changeTextViewState(TextView textView, boolean enable) {
        if (textView.isEnabled() == enable) {
            return;
        }
        if (enable) {
            textView.setEnabled(true);
            textView.setBackgroundResource(R.drawable.rounded_corners_bg_blue);
            textView.setTextColor(CommunityUtil.getColor(R.color.white));

        } else {
            textView.setEnabled(false);
            textView.setBackgroundResource(R.drawable.rounded_corners_tag_grey);
            textView.setTextColor(CommunityUtil.getColor(R.color.color_text_l));
        }
    }

    public static boolean isOutOfView(View v, MotionEvent event) {
        if (v != null) {
            int[] leftTop = {0, 0};
            // 获取指定View的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
                // 点击的是指定View
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回宽高数组
     *
     * @param picture
     * @return
     */
    public static int[] getWidthAndHeight(Picture picture, int maxWidth) {
        float width;
        float height;
        // 要展示的图片长宽比（最大2，最小0.5）
        float radio;
        if (picture == null || picture.getWidth() <= 0 || picture.getHeight() <= 0) {
            return new int[]{300, 300};
        }
        // 原图比例
        radio = ((float) picture.getWidth()) / picture.getHeight();
        if (radio > 2f) {
            // 横向长图
            radio = 2f;
        } else if (radio < 0.5f) {
            // 竖向长图
            radio = 0.5f;
        }
        if (radio >= 1) {
            width = maxWidth;
            height = width / radio;
        } else {
            height = maxWidth;
            width = height * radio;
        }
        if (width > picture.getWidth()) {
            width = picture.getWidth();
            height = height * picture.getWidth() / width;
        }
        if (height > picture.getHeight()) {
            height = picture.getHeight();
            width = width * picture.getHeight() / height;
        }
        if (width <= 0) {
            width = AllOnlineApp.screenWidth / 3;
        }
        if (height <= 0) {
            height = AllOnlineApp.screenHeight / 3;
        }
        return new int[]{(int) width, (int) height};
    }

    /**
     * 判断TextView的内容宽度是否超出其可用宽度
     *
     * @param tv
     * @return
     */
    public static boolean isOverFlowed(TextView tv) {
        int availableWidth = tv.getWidth() - tv.getPaddingLeft() - tv.getPaddingRight();
        Paint textViewPaint = tv.getPaint();
        float textWidth = textViewPaint.measureText(tv.getText().toString());
        if (textWidth > availableWidth) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * caculate the bitmap sampleSize
     */
    public final static int caculateInSampleSize(BitmapFactory.Options options, int rqsW, int rqsH) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (rqsW == 0 || rqsH == 0)
            return 1;
        if (height > rqsH || width > rqsW) {
            final int heightRatio = Math.round((float) height / (float) rqsH);
            final int widthRatio = Math.round((float) width / (float) rqsW);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 处理“n条新消息”，“n张”等数字和文字大小不一样的情况
     *
     * @param textView
     * @param divideIndex
     */
    public static void handleTextView(TextView textView, int divideIndex, float numSize, float textSize) {
        String text = textView.getText().toString();
        SpannableString span = new SpannableString(text);
        span.setSpan(new AbsoluteSizeSpan((int) numSize), 0, divideIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new AbsoluteSizeSpan((int) textSize), divideIndex, text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(span);
    }

    public static void setPtrStateText(PullToRefreshBase ptrListView) {
        ILoadingLayout startLoadingLayout = ptrListView.getLoadingLayoutProxy(true, false);
        startLoadingLayout.setPullLabel(CommunityUtil.getString(R.string.pull_to_refresh));
        startLoadingLayout.setRefreshingLabel(CommunityUtil.getString(R.string.community_loading));
        startLoadingLayout.setReleaseLabel(CommunityUtil.getString(R.string.release_to_load));
        startLoadingLayout.setLoadingDrawable(ptrListView.getResources().getDrawable(R.drawable.loading_drawable));

        ILoadingLayout endLoadingLayout = ptrListView.getLoadingLayoutProxy(false, true);
        endLoadingLayout.setPullLabel(CommunityUtil.getString(R.string.load_more));
        endLoadingLayout.setRefreshingLabel(CommunityUtil.getString(R.string.community_loading));
        endLoadingLayout.setReleaseLabel(CommunityUtil.getString(R.string.release_to_load));
        endLoadingLayout.setLoadingDrawable(ptrListView.getResources().getDrawable(R.drawable.loading_drawable));
    }

    // private static int topiclistContentMin = 0;
    public static int[] getTopicSingleImageWH(Context context, Picture picture, int MAX_WIDTH) {
        /*if(topiclistContentMin == 0) {
            topiclistContentMin = context.getResources().getDimensionPixelSize(R.dimen.topiclist_content_min);
        }*/
        if (MAX_WIDTH <= 0) {
            // System.out.println("---------:::: MAX_WIDTH <= 0" + picture.getPicture());
            MAX_WIDTH = context.getResources().getDimensionPixelSize(R.dimen.topiclist_content_min);//topiclistContentMin;
        }
        int topiclistContentMin = MAX_WIDTH;
        int imageWidth = picture.getWidth();
        int imageHeight = picture.getHeight();
        if (imageWidth <= 0 || imageHeight <= 0) {
            // System.out.println("---------:::: imageWidth <= 0 || imageHeight <= 0" + picture.getPicture());
            return new int[]{MAX_WIDTH, MAX_WIDTH};
        }
        int scaleWidth, scaleHeight;
        if (imageWidth < topiclistContentMin && imageHeight < topiclistContentMin) {
            if (imageWidth > imageHeight) {
                scaleWidth = topiclistContentMin;
                scaleHeight = (scaleWidth * imageHeight) / imageWidth;
            } else {
                scaleHeight = topiclistContentMin;
                scaleWidth = (scaleHeight * imageWidth) / imageHeight;
            }
        } else {
            if (imageWidth > imageHeight) {
                scaleWidth = Math.min(imageWidth, MAX_WIDTH);
                scaleHeight = (scaleWidth * imageHeight) / imageWidth;
            } else {
                scaleHeight = Math.min(imageHeight, MAX_WIDTH);
                scaleWidth = (scaleHeight * imageWidth) / imageHeight;
            }
        }
        // System.out.println("---------::: " + MAX_WIDTH +"  " + topiclistContentMin +"  " + imageWidth +"  "
        // + imageHeight +"  " + scaleWidth +"  " + scaleHeight +"  " + picture.getPicture());
        return new int[]{scaleWidth, scaleHeight};
    }

    public static int[] getTopicSingleImageWH2(Context context, Picture picture, int MAX_WIDTH) {
        // 决定是要裁剪图片，还是不用裁剪，
        // 是让高显示到MAX_HEIGHT，还是让宽显示到MAX_WIDTH
        if (MAX_WIDTH <= 0) {
            MAX_WIDTH = AllOnlineApp.screenWidth * 4 / 5;
        }
        int[] widthHeight = new int[]{AllOnlineApp.screenWidth / 2, AllOnlineApp.screenHeight / 3};
        final int MAX_HEIGHT = MAX_WIDTH * AllOnlineApp.screenWidth / AllOnlineApp.screenHeight;
        // final int MIN_WIDTH = MAX_WIDTH / 5;
        // final int MIN_HEIGHT = MAX_WIDTH / 2;
        int imageWidth = picture.getWidth();
        int imageHeight = picture.getHeight();
        if (imageWidth <= 0 || imageHeight <= 0) {
            widthHeight[0] = (int) (MAX_WIDTH / 2);
            widthHeight[1] = (int) MAX_HEIGHT * 2 / 3;
        } else {
            if (PhotoViewAttacher.isLongImg(imageWidth, imageHeight)) {
                if (imageHeight > imageWidth) {
                    widthHeight[0] = MAX_HEIGHT / 5;
                    widthHeight[1] = MAX_HEIGHT;
                } else {
                    widthHeight[0] = MAX_WIDTH;
                    widthHeight[1] = MAX_HEIGHT / 2;
                }
            } else {
                if (imageHeight > imageWidth) {
                    widthHeight[0] = MAX_HEIGHT * imageWidth / imageHeight;
                    widthHeight[1] = MAX_HEIGHT;
                } else {
                    widthHeight[0] = MAX_WIDTH;
                    widthHeight[1] = MAX_WIDTH * imageHeight / imageWidth;
                }
            }
        }
        if (widthHeight[1] > MAX_HEIGHT) {
            widthHeight[0] = MAX_HEIGHT * imageWidth / imageHeight;
            widthHeight[1] = MAX_HEIGHT;
        } else if (widthHeight[0] > MAX_WIDTH) {
            widthHeight[0] = MAX_WIDTH;
            widthHeight[1] = MAX_WIDTH * imageHeight / imageWidth;
        }
        return widthHeight;
    }

    public static void setBg(View v, Drawable d) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackground(d);
        } else {
            v.setBackgroundDrawable(d);
        }
    }

    public static int[] getSingleImageWH(Context context, int srcWidth, int srcHeight, int MAX_WIDTH) {
        // 决定是要裁剪图片，还是不用裁剪，
        // 是让高显示到MAX_HEIGHT，还是让宽显示到MAX_WIDTH
        if (MAX_WIDTH <= 0) {
            MAX_WIDTH = AllOnlineApp.screenWidth * 4 / 5;
        }
        int[] widthHeight = new int[] { AllOnlineApp.screenWidth / 2, AllOnlineApp.screenHeight / 3 };
        final int MAX_HEIGHT = MAX_WIDTH * AllOnlineApp.screenWidth / AllOnlineApp.screenHeight;
        if (srcWidth <= 0 || srcHeight <= 0) {
            widthHeight[0] = (int) (MAX_WIDTH / 2);
            widthHeight[1] = (int) MAX_HEIGHT * 2 / 3;
        } else {
            if (PhotoViewAttacher.isLongImg(srcWidth, srcHeight)) {
                if (srcHeight > srcWidth) {
                    widthHeight[0] = MAX_HEIGHT / 5;
                    widthHeight[1] = MAX_HEIGHT;
                } else {
                    widthHeight[0] = MAX_WIDTH;
                    widthHeight[1] = MAX_HEIGHT / 2;
                }
            } else {
                if (srcHeight > srcWidth) {
                    widthHeight[0] = MAX_HEIGHT * srcWidth / srcHeight;
                    widthHeight[1] = MAX_HEIGHT;
                } else {
                    widthHeight[0] = MAX_WIDTH;
                    widthHeight[1] = MAX_WIDTH * srcHeight / srcWidth;
                }
            }
        }
        if (widthHeight[1] > MAX_HEIGHT) {
            widthHeight[0] = MAX_HEIGHT * srcWidth / srcHeight;
            widthHeight[1] = MAX_HEIGHT;
        } else if (widthHeight[0] > MAX_WIDTH) {
            widthHeight[0] = MAX_WIDTH;
            widthHeight[1] = MAX_WIDTH * srcHeight / srcWidth;
        }
        return widthHeight;
    }
}
