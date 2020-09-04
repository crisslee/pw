package com.coomix.app.all.share;

import android.content.Context;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.coomix.app.all.R;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * popupwindow工具类，包含处理兼容性的方法
 *
 * @author 刘生健
 * @since 2015-6-17 下午03:23:22
 */
public class PopupWindowUtil {

    /**
     * 在new PopupWindow()后调用 兼容4.0(ICE_CREAM_SANDWICH)以下版本 <br>
     * 防止错误<br>
     * java.lang.NullPointerException<br>
     * at android.widget.PopupWindow$1.onScrollChanged(PopupWindow.java:
     */
    public static void fixPopupWindow(final PopupWindow window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                final Field fAnchor = PopupWindow.class.getDeclaredField("mAnchor");
                fAnchor.setAccessible(true);
                Field listener = PopupWindow.class.getDeclaredField("mOnScrollChangedListener");
                listener.setAccessible(true);
                final ViewTreeObserver.OnScrollChangedListener originalListener =
                    (ViewTreeObserver.OnScrollChangedListener) listener.get(window);
                ViewTreeObserver.OnScrollChangedListener newListener = new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        try {
                            @SuppressWarnings("unchecked")
                            WeakReference<View> mAnchor = (WeakReference<View>) fAnchor.get(window);
                            if (mAnchor == null || mAnchor.get() == null) {
                                return;
                            } else {
                                originalListener.onScrollChanged();
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                };
                listener.set(window, newListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 底部功能按钮
     *
     * @param resTitle 说明文字resId（如 选择背景图片），<=0时不可见，
     * @param item1TextSet 功能1，为null时不可见
     * @param item2TextSet 功能2，为null时不可见
     */
    public static void showPopWindow(Context context, View viewParent, int resTitle, final TextSet item1TextSet,
        final TextSet item2TextSet, boolean outsideTouchable) {

        ArrayList<TextSet> list = new ArrayList<TextSet>();

        if (item1TextSet != null) {
            list.add(item1TextSet);
        }

        if (item2TextSet != null) {
            list.add(item2TextSet);
        }

        showPopWindow(context, viewParent, resTitle, list, outsideTouchable);
    }

    public static void showPopWindow(Context context, View viewParent, int resTitle, String cancelText,
        final ArrayList<TextSet> list, boolean outsideTouchable) {
        String title = "";
        if (resTitle > 0) {
            title = context.getString(resTitle);
        }
        showPopWindow(context, viewParent, title, cancelText, list, outsideTouchable);
    }

    public static void showPopWindow(Context context, View viewParent, int resTitle, final ArrayList<TextSet> list,
        boolean outsideTouchable, PopupWindow.OnDismissListener... dismissListener) {
        String title = null;
        if (resTitle > 0) {
            title = context.getString(resTitle);
        }
        showPopWindow(context, viewParent, title, null, list, outsideTouchable, dismissListener);
    }

    public static void showPopWindow(Context context, View viewParent, String title, final ArrayList<TextSet> list,
        boolean outsideTouchable, PopupWindow.OnDismissListener... dismissListener) {
        showPopWindow(context, viewParent, title, null, list, outsideTouchable, dismissListener);
    }

    public static void showPopWindow(Context context, View viewParent, String title, String cancelText,
        final ArrayList<TextSet> list, boolean outsideTouchable, PopupWindow.OnDismissListener... dismissListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.popwindow_bottom, null);
        TextView desc = (TextView) view.findViewById(R.id.pop_desc);
        View seperator1 = view.findViewById(R.id.pop_seperator1);
        TextView cancelView = (TextView) view.findViewById(R.id.pop_cancel);
        final LinearLayout popMain = (LinearLayout) view.findViewById(R.id.pop_main);

        final PopupWindow popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
        PopupWindowUtil.fixPopupWindow(popWindow);
        if (!TextUtils.isEmpty(title)) {
            desc.setVisibility(View.VISIBLE);
            seperator1.setVisibility(View.VISIBLE);
            desc.setText(title);
        } else {
            desc.setVisibility(View.GONE);
            seperator1.setVisibility(View.GONE);
        }

        if (list != null && list.size() > 0) {
            if (list.get(0).imageId > 0) {
                // 有图标的部分
                GridView gridView = (GridView) view.findViewById(R.id.gridViewShare);
                gridView.setVisibility(View.VISIBLE);
                int size = list.size();
                if (size > 5) {
                    size = 5;
                }
                gridView.setNumColumns(size);
                gridView.setAdapter(new ShareAdapter(list, context));
                gridView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position < 0 || position > list.size() - 1) {
                            return;
                        }
                        list.get(position).onClickListener.onClick(view);
                        if (popWindow != null && popWindow.isShowing()) {
                            popWindow.dismiss();
                        }
                    }
                });
            } else {
                // 无图标的部分
                desc.setBackgroundResource(R.color.white);
                for (int i = 0; i < list.size(); i++) {
                    final TextSet textSet = list.get(i);
                    View itemView = LayoutInflater.from(context).inflate(R.layout.popwindow_bottom_item, null);

                    TextView popItem = (TextView) itemView.findViewById(R.id.pop_item);
                    View popSeperator = itemView.findViewById(R.id.pop_seperator);

                    if (i == 0) {
                        popSeperator.setVisibility(View.GONE);
                    }

                    if (TextUtils.isEmpty(textSet.text)) {
                        popItem.setText(textSet.textResId);
                    } else {
                        popItem.setText(textSet.text);
                    }
                    popItem.setTextColor(context.getResources().getColor(textSet.important ? R.color.color_main
                        : R.color.color_text_h));

                    popItem.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (popWindow != null && popWindow.isShowing() && textSet.bClickClose) {
                                popWindow.dismiss();
                            }
                            textSet.onClickListener.onClick(v);
                        }
                    });

                    popMain.addView(itemView);
                }
            }
        }

        if (outsideTouchable) {
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (popWindow != null && popWindow.isShowing()) {
                        popWindow.dismiss();
                    }
                }
            });
        }
        if (!TextUtils.isEmpty(cancelText)) {
            cancelView.setText(cancelText);
        }
        cancelView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popWindow != null && popWindow.isShowing()) {
                    popWindow.dismiss();
                }
            }
        });
        // 设置setFocusable(true)和backgroundDrawable才能使点击返回键popupwindow消失，否则，点击返回键Popupwindow为null
        popWindow.setFocusable(true);
        popWindow.setBackgroundDrawable(new PaintDrawable(0x88000000));
        popWindow.setOutsideTouchable(outsideTouchable);
        if (dismissListener != null && dismissListener.length > 0) {
            popWindow.setOnDismissListener(dismissListener[0]);
        }

        popWindow.showAtLocation(viewParent, Gravity.CENTER, 0, 0);
    }

    private static void setTextViewStyle(Context context, TextView sourceView, TextView targetView) {
        if (sourceView == targetView) {
            return;
        }

        if (sourceView != null) {
            sourceView.setBackgroundDrawable(
                context.getResources().getDrawable(R.drawable.textview_round_border_normal));
            sourceView.setTextColor(context.getResources().getColor(R.color.black));
        }

        if (targetView != null) {
            targetView.setBackgroundDrawable(
                context.getResources().getDrawable(R.drawable.textview_round_border_selected));
            targetView.setTextColor(context.getResources().getColor(R.color.white));
        }
    }

    static TextView currentTextView = null;

    public static void showPopWindowForLocation(Context context, View viewParent, int descTextResId,
        final ArrayList<TextSet> list, boolean outsideTouchable, StringBuffer timeLong, final StringBuffer duration) {

        View view = LayoutInflater.from(context).inflate(R.layout.popwindow_bottom_share, null);
        TextView desc = (TextView) view.findViewById(R.id.pop_desc);
        TextView threeHoursTv = (TextView) view.findViewById(R.id.threeHours);
        TextView sixHoursTv = (TextView) view.findViewById(R.id.sixHours);
        TextView twelveHoursTv = (TextView) view.findViewById(R.id.twelveHours);
        threeHoursTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewStyle(context, currentTextView, threeHoursTv);
                currentTextView = threeHoursTv;
                timeLong.setLength(0);
                timeLong.append(threeHoursTv.getText());
                duration.setLength(0);
                duration.append(String.valueOf(3 * 60 * 60));
            }
        });
        sixHoursTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewStyle(context, currentTextView, sixHoursTv);
                currentTextView = sixHoursTv;
                timeLong.setLength(0);
                timeLong.append(sixHoursTv.getText());
                duration.setLength(0);
                duration.append(String.valueOf(6 * 60 * 60));
            }
        });
        twelveHoursTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewStyle(context, currentTextView, twelveHoursTv);
                currentTextView = twelveHoursTv;
                timeLong.setLength(0);
                timeLong.append(twelveHoursTv.getText());
                duration.setLength(0);
                duration.append(String.valueOf(12 * 60 * 60));
            }
        });
        threeHoursTv.performClick();

        TextView cancelView = (TextView) view.findViewById(R.id.pop_cancel);
        final LinearLayout popMain = (LinearLayout) view.findViewById(R.id.pop_main);

        final PopupWindow popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
        PopupWindowUtil.fixPopupWindow(popWindow);

        if (list != null && list.size() > 0) {
            if (list.get(0).imageId > 0) {
                // 有图标的部分
                GridView gridView = (GridView) view.findViewById(R.id.gridViewShare);
                gridView.setVisibility(View.VISIBLE);
                int size = list.size();
                if (size > 5) {
                    size = 5;
                }
                gridView.setNumColumns(size);
                gridView.setAdapter(new ShareForLocationAdapter(list, context));
                gridView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position < 0 || position > list.size() - 1) {
                            return;
                        }
                        list.get(position).onClickListener.onClick(view);
                        if (popWindow != null && popWindow.isShowing()) {
                            popWindow.dismiss();
                        }
                    }
                });
            } else {
                // 无图标的部分
                desc.setBackgroundResource(R.color.white);
                for (int i = 0; i < list.size(); i++) {
                    final TextSet textSet = list.get(i);
                    View itemView = LayoutInflater.from(context).inflate(R.layout.popwindow_bottom_item, null);

                    TextView popItem = (TextView) itemView.findViewById(R.id.pop_item);
                    View popSeperator = itemView.findViewById(R.id.pop_seperator);

                    if (i == 0) {
                        popSeperator.setVisibility(View.GONE);
                    }

                    popItem.setText(textSet.textResId);
                    popItem.setTextColor(context.getResources()
                        .getColor(textSet.important ? R.color.color_main : R.color.color_text_h));

                    popItem.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (popWindow != null && popWindow.isShowing() && textSet.bClickClose) {
                                popWindow.dismiss();
                            }
                            textSet.onClickListener.onClick(v);
                        }
                    });

                    popMain.addView(itemView);
                }
            }
        }

        if (outsideTouchable) {
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (popWindow != null && popWindow.isShowing()) {
                        popWindow.dismiss();
                    }
                }
            });
        }
        cancelView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popWindow != null && popWindow.isShowing()) {
                    popWindow.dismiss();
                }
            }
        });
        // 设置setFocusable(true)和backgroundDrawable才能使点击返回键popupwindow消失，否则，点击返回键Popupwindow为null
        popWindow.setFocusable(true);
        popWindow.setBackgroundDrawable(new PaintDrawable(0x88000000));
        popWindow.setOutsideTouchable(outsideTouchable);

        popWindow.showAtLocation(viewParent, Gravity.CENTER, 0, 0);
    }

    static class ShareAdapter extends BaseAdapter {

        private ArrayList<TextSet> list;
        private Context context;

        public ShareAdapter(ArrayList<TextSet> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.share_adpater_item, null);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageViewIcon);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.textViewTo);
                convertView.setTag(R.layout.share_adpater_item, viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag(R.layout.share_adpater_item);
            }

            if (position >= 0 && position < list.size()) {
                viewHolder.imageView.setImageResource(list.get(position).imageId);
                viewHolder.textView.setText(list.get(position).textResId);
            }

            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
            TextView textView;
        }
    }

    static class ShareForLocationAdapter extends BaseAdapter {

        private ArrayList<TextSet> list;
        private Context context;

        public ShareForLocationAdapter(ArrayList<TextSet> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.share_adpater_location_item, null);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageViewIcon);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.textViewTo);
                convertView.setTag(R.layout.share_adpater_item, viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag(R.layout.share_adpater_item);
            }

            if (position >= 0 && position < list.size()) {
                viewHolder.imageView.setImageResource(list.get(position).imageId);
                viewHolder.textView.setText(list.get(position).textResId);
            }

            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
            TextView textView;
        }
    }
}
