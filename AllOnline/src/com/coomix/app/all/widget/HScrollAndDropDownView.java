package com.coomix.app.all.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.GlideApp;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.ActCategory;

import java.util.ArrayList;


public class HScrollAndDropDownView extends LinearLayout implements View.OnClickListener {
    private LinearLayout layoutMain;
    private ImageView imageFlag1, imageFlag2;
    private GridView gridView;
    private View halfView;
    private LinearLayout layoutTop;
    private HorizontalScrollView horizontalScrollView;
    private ArrayList<ActCategory> actCategoryArrayList;
    private OnMyItemClickListener myItemClickListener;
    private GridItemAdapter gridItemAdapter;
    private int iSelectPos = -1;
    private int scrollOffset = 0;

    public HScrollAndDropDownView(Context context) {
        this(context, null, 0);
    }

    public HScrollAndDropDownView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HScrollAndDropDownView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initViews();
    }

    public int getSelectPosition() {
        return iSelectPos;
    }

    private void initViews() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.hscroll_view, this);
        layoutMain = (LinearLayout) view.findViewById(R.id.layoutWrapMain);
        imageFlag1 = (ImageView) view.findViewById(R.id.imageViewFlag);
        imageFlag2 = (ImageView) view.findViewById(R.id.imageViewFlag2);
        gridView = (GridView) view.findViewById(R.id.gridViewAll);
        horizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.horizontalScrollView);
        layoutTop = (LinearLayout) view.findViewById(R.id.layoutTop);
        halfView = findViewById(R.id.halfBottom);

        imageFlag1.setVisibility(View.INVISIBLE);
        imageFlag1.setOnClickListener(this);
        imageFlag2.setOnClickListener(this);
        halfView.setOnClickListener(this);
        gridView.setOnItemClickListener(onItemClickListener);

        halfView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onClick(v);
                }
                return true;
            }
        });
    }

    public boolean dismissGridView() {
        if (gridView.getVisibility() == VISIBLE) {
            setUiStatus(false);
            return true;
        }
        return false;
    }

    private void setUiStatus(boolean bDropDown) {
        if (actCategoryArrayList == null || actCategoryArrayList.size() <= 0) {
            return;
        }
        if (bDropDown) {
            gridItemAdapter.notifyDataSetChanged();
            gridView.setVisibility(VISIBLE);
            layoutTop.setVisibility(GONE);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.push_top_in);
            gridView.setAnimation(animation);
            animation.start();
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    halfView.setVisibility(VISIBLE);
                    imageFlag1.setVisibility(INVISIBLE);
                    imageFlag2.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            halfView.setVisibility(GONE);
            imageFlag2.setVisibility(INVISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.push_top_out);
            gridView.setAnimation(animation);
            animation.start();
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    gridView.setVisibility(GONE);
                    layoutTop.setVisibility(VISIBLE);
                    imageFlag1.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
            if (gridItemAdapter != null) {
                setUiStatus(false);

                if (position != iSelectPos) {
                    reselectViewStatus(position);
                    hTextViewScrollTo(position);
                }

                toClickCallbackAction((ActCategory) gridItemAdapter.getItem(position), position);

                gridItemAdapter.notifyDataSetChanged();
            }
        }
    };

    private void hTextViewScrollTo(int position) {
        if (position < 0 || position > layoutMain.getChildCount() - 1) {
            return;
        }
        View textView = layoutMain.getChildAt(position);
        if (textView != null) {
            int scrollViewWidth = horizontalScrollView.getWidth();
            if ((scrollViewWidth + scrollOffset) < textView.getRight()) {
                //需要向右移动
                horizontalScrollView.smoothScrollBy(textView.getRight() - (scrollViewWidth + scrollOffset), 0);
                scrollOffset += textView.getRight() - (scrollViewWidth + scrollOffset);
            }
            if (scrollOffset > textView.getLeft()) {
                //需要向左移动
                horizontalScrollView.smoothScrollBy(textView.getLeft() - scrollOffset, 0);
                scrollOffset += textView.getLeft() - scrollOffset;
            }
        }
    }

    public void setMyItemClickListener(OnMyItemClickListener myItemClickListener) {
        this.myItemClickListener = myItemClickListener;
    }

    public void setCategoryData(ArrayList<ActCategory> listData, int selectPosition) {
        if (listData != null && listData.size() > 0) {
            imageFlag1.setVisibility(View.VISIBLE);
            this.actCategoryArrayList = listData;
            for (int i = 0; i < actCategoryArrayList.size(); i++) {
                layoutMain.addView(getItemLayout(actCategoryArrayList.get(i), i, selectPosition));
            }
        }

        if (gridItemAdapter == null) {
            gridItemAdapter = new GridItemAdapter();
            gridView.setAdapter(gridItemAdapter);
        } else {
            gridItemAdapter.notifyDataSetChanged();
        }
    }

    private LinearLayout getItemLayout(final ActCategory actCategory, final int index, int selectPosition) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setOrientation(VERTICAL);

        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LayoutParams(getResources()
                .getDimensionPixelOffset(R.dimen.act_category_width1),
                getResources().getDimensionPixelOffset(R.dimen.act_category_height1));
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        if (index == selectPosition) {
            GlideApp.with(getContext()).load(actCategory.getSelected_pic()).placeholder(R.drawable.act_part_select_default).error(R.drawable.act_part_select_default).into(imageView);
        } else {
            GlideApp.with(getContext()).load(actCategory.getPic()).placeholder(R.drawable.act_part_default).error(R.drawable.act_part_default).into(imageView);
        }
        layout.addView(imageView);

        TextView textView = new TextView(getContext());
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setTextColor(getResources().getColorStateList(R.color.act_category_text_selector));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_s));
        textView.setPadding(getResources().getDimensionPixelOffset(R.dimen.space_2x), 0, getResources().getDimensionPixelOffset(R.dimen.space_2x), 0);
        textView.setText(actCategory.getTitle());
        if (index == selectPosition) {
            iSelectPos = index;
            textView.setSelected(true);
        }
        layout.addView(textView);

        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                reselectViewStatus(index);
                toClickCallbackAction(actCategory, index);
            }
        });

        return layout;
    }

    private boolean toClickCallbackAction(ActCategory actCategory, int position) {
        if (actCategory != null && myItemClickListener != null && position != iSelectPos) {
            iSelectPos = position;
            myItemClickListener.onClick(actCategory);
            return true;
        }
        return false;
    }

    private void reselectViewStatus(int select) {
        //把原来选中状态的view重置
        if (iSelectPos >= 0 && iSelectPos < layoutMain.getChildCount()) {
            LinearLayout layout = (LinearLayout) layoutMain.getChildAt(iSelectPos);
            if (layout != null) {
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View view = layout.getChildAt(i);
                    if (view != null) {
                        if (view instanceof TextView) {
                            view.setSelected(false);
                        } else if (view instanceof ImageView && iSelectPos < actCategoryArrayList.size() && actCategoryArrayList.get(iSelectPos) != null) {
                            GlideApp.with(getContext()).load(actCategoryArrayList.get(iSelectPos).getPic()).placeholder(R.drawable.act_part_default).error(R.drawable.act_part_default).into((ImageView) view);
                        }
                    }
                }
            }
        }

        //把当前点击的view设置选中状态
        if (select >= 0 && select < layoutMain.getChildCount()) {
            LinearLayout layout = (LinearLayout) layoutMain.getChildAt(select);
            if (layout != null) {
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View view = layout.getChildAt(i);
                    if (view != null) {
                        if (view instanceof TextView) {
                            view.setSelected(true);
                        } else if (view instanceof ImageView && select < actCategoryArrayList.size() && actCategoryArrayList.get(select) != null) {
                            GlideApp.with(getContext()).load(actCategoryArrayList.get(select).getSelected_pic())/*.placeholder(R.drawable.act_part_select_default)*/.error(R.drawable.act_part_select_default).into((ImageView) view);
                        }
                    }
                }
            }
        }
    }

    /*******左右手势滑动后切换顶部的item*******/
    public void changeItem(boolean bAdd) {
        if (actCategoryArrayList == null || actCategoryArrayList.size() <= 0) {
            return;
        }
        int tempIndex = 0;
        if (bAdd) {
            tempIndex = iSelectPos + 1;
            if (tempIndex >= 0 && tempIndex < actCategoryArrayList.size()) {
                reselectViewStatus(tempIndex);
                if (toClickCallbackAction(actCategoryArrayList.get(tempIndex), tempIndex)) {
                    hTextViewScrollTo(iSelectPos);
                }
            }
        } else {
            tempIndex = iSelectPos - 1;
            if (tempIndex >= 0 && tempIndex < actCategoryArrayList.size()) {
                reselectViewStatus(tempIndex);
                if (toClickCallbackAction(actCategoryArrayList.get(tempIndex), tempIndex)) {
                    hTextViewScrollTo(iSelectPos);
                }
            }
        }
    }

    public void selectItem(int position) {
        if (actCategoryArrayList == null || actCategoryArrayList.size() <= 0
                || position < 0 || position >= actCategoryArrayList.size()) {
            return;
        }
        if(position == iSelectPos) {
            return;
        }

        reselectViewStatus(position);
        if (toClickCallbackAction(actCategoryArrayList.get(position), position)) {
            hTextViewScrollTo(iSelectPos);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageViewFlag:
                if (gridView.getVisibility() == View.GONE) {
                    setUiStatus(true);
                }
                break;

            case R.id.imageViewFlag2:
                setUiStatus(false);
                break;

            case R.id.halfBottom:
                setUiStatus(false);
                break;
        }
    }

    public interface OnMyItemClickListener {
        public void onClick(ActCategory actCategory);
    }

    class GridItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return actCategoryArrayList == null ? 0 : actCategoryArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            if (actCategoryArrayList != null && position >= 0 && position < actCategoryArrayList.size()) {
                return actCategoryArrayList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null || convertView.getTag(R.layout.act_category_item) == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.act_category_item, null);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageViewType);
                viewHolder.textTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
            } else {
                viewHolder = (ViewHolder) convertView.getTag(R.layout.act_category_item);
            }

            ActCategory actCategory = (ActCategory) getItem(position);
            if (actCategory == null) {
                return convertView;
            }

            viewHolder.textTitle.setText(actCategory.getTitle());

            if (iSelectPos == position) {
                viewHolder.textTitle.setSelected(true);
                GlideApp.with(getContext()).load(actCategory.getSelected_pic())/*.placeholder(R.drawable.act_part_select_default)*/.error(R.drawable.act_part_select_default).into(viewHolder.imageView);
            } else {
                viewHolder.textTitle.setSelected(false);
                GlideApp.with(getContext()).load(actCategory.getPic()).placeholder(R.drawable.act_part_default).error(R.drawable.act_part_default).into(viewHolder.imageView);
            }

            return convertView;
        }

        class ViewHolder {
            TextView textTitle;
            ImageView imageView;
        }
    }
}
