package com.coomix.app.all.widget.wheel.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.coomix.app.all.R;

import java.util.ArrayList;


public class WheelSelectDialog extends Dialog {

    public Context mContext;

    public View mDialogView;

    public Button mLeftBtn;

    public Button mRightBtn;

    public WheelView mWheelView;


    private ArrayList<String> mArrayListData;

    private WheelViewIndexAdapter wvApadter;

    protected WheelSelectDialog(Context context) {
        super(context);

        this.mContext = context;
        initView();
    }

    protected WheelSelectDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);

        this.mContext = context;
        initView();
    }

    public WheelSelectDialog(Context context, int theme) {
        super(context, theme);

        this.mContext = context;
        initView();
    }

    public void initView() {
        mDialogView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.dialog_wheel_select, null);
        mLeftBtn = (Button) mDialogView.findViewById(R.id.btn_cancel);
        mRightBtn = (Button) mDialogView.findViewById(R.id.btn_confirm);
        mWheelView = (WheelView) mDialogView.findViewById(R.id.wv_city);
        mWheelView.setVisibleItems(5);
        mWheelView.setCyclic(false);
        mWheelView.addClickingListener(new OnWheelClickedListener() {

            @Override
            public void onItemClicked(WheelView wheel, int itemIndex) {
                wheel.setCurrentItem(itemIndex,true);
            }
        });
    }

   

    public void setData(ArrayList<String> mArrayListData) {
        this.mArrayListData = mArrayListData;
        wvApadter = new WheelViewIndexAdapter(mContext, this.mArrayListData);
        mWheelView.setViewAdapter(wvApadter);
    }

    public void setPositiveButton(android.view.View.OnClickListener listener) {
        mRightBtn.setVisibility(View.VISIBLE);
        mRightBtn.setOnClickListener(listener);
    }

    public void setClickListener() {
        mWheelView.addClickingListener(new OnWheelClickedListener() {

            @Override
            public void onItemClicked(WheelView wheel, int itemIndex) {
                wheel.setCurrentItem(itemIndex);
            }
        });
    }

    /**
     * set text and listener for positive button
     * 
     * @param resId
     *            text displayed
     * @param listener
     *            onclickListener
     */
    public void setPositiveButton(int resId, android.view.View.OnClickListener listener) {
        mRightBtn.setVisibility(View.VISIBLE);
        mRightBtn.setText(mContext.getString(resId));
        mRightBtn.setOnClickListener(listener);
    }

    public void setNegativeButton(android.view.View.OnClickListener listener) {
        mLeftBtn.setVisibility(View.VISIBLE);
        mLeftBtn.setOnClickListener(listener);
    }

    /**
     * set text and listener for negative button
     * 
     * @param resId
     * @param listener
     */
    public void setNegativeButton(int resId, android.view.View.OnClickListener listener) {
        mLeftBtn.setVisibility(View.VISIBLE);
        mLeftBtn.setText(mContext.getString(resId));
        mLeftBtn.setOnClickListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mDialogView);

        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // getWindow().clearFlags(ViewGroup.LayoutParams.FLAG_DIM_BEHIND);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    public String getSelectedItemIndex() {
        if (mArrayListData != null) {
            return mArrayListData.get(mWheelView.getCurrentItem());
        }
        
        return null;
    }

    public void setItemSelected(int pos) {
        mWheelView.setCurrentItem(pos);
    }

    private class WheelViewIndexAdapter extends AbstractWheelTextAdapter {

        private ArrayList<String> mCityList;

        /**
         * Constructor
         */
        protected WheelViewIndexAdapter(Context context, ArrayList<String> mGameCardsList) {
            super(context, R.layout.wheelview_item_layout, NO_RESOURCE);

            setItemTextResource(R.id.item_name);
            this.mCityList = mGameCardsList;
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            View view = super.getItem(index, cachedView, parent);
            return view;
        }

        @Override
        public int getItemsCount() {
            return mCityList.size();
        }

        @Override
        protected CharSequence getItemText(int index) {
            return mCityList.get(index);
        }
    }
}
