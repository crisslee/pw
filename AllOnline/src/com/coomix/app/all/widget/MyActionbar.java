package com.coomix.app.all.widget;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coomix.app.all.R;
import com.coomix.app.all.manager.ThemeManager;


public class MyActionbar extends RelativeLayout{
    private ImageView imageLeft, imageRight, imageClose;
    private TextView textLeft, textTitle, textRight;
    private RadioGroup mRadioGroup;

    public MyActionbar(Context context) {
        this(context, null, 0);
    }

    public MyActionbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyActionbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initViews();
    }

    private void initViews() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.my_actionbar_layout, this);
        imageLeft = (ImageView) view.findViewById(R.id.iamgeViewBack);
        textLeft = (TextView) view.findViewById(R.id.textViewLeft);
        imageClose = (ImageView) view.findViewById(R.id.imageViewClose);
        imageRight = (ImageView) view.findViewById(R.id.imageViewRight);
        textTitle = (TextView) view.findViewById(R.id.textViewTitle);
        textRight = (TextView) view.findViewById(R.id.textViewRight);
        mRadioGroup = (RadioGroup) findViewById(R.id.rGroup_update);

        imageLeft.setOnClickListener(view1 -> {
            Context context = getContext();
            if(context != null && context instanceof Activity){
                ((Activity)context).finish();
            }
        });

        setBackgroundColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorWhole());
        textTitle.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
        textRight.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
    }

    public void initActionbar(boolean bShowBack, int titleStrId, int rightStrId, int rightImageId){
        if(bShowBack){
            setLeftImageResource(R.drawable.actionbar_back);
        }else{
            imageLeft.setVisibility(GONE);
        }

        setTitle(titleStrId);

        setRightText(rightStrId);

        setRightImageResource(rightImageId);
    }

    public void initActionbar(boolean bShowBack, String title, int rightStrId, int rightImageId){
        if(bShowBack){
            setLeftImageResource(R.drawable.actionbar_back);
        }else{
            imageLeft.setVisibility(GONE);
        }

        setTitle(title);

        setRightText(rightStrId);

        setRightImageResource(rightImageId);
    }

    public void setLeftImageVisibility(int visibility){
        imageLeft.setVisibility(visibility);
    }

    public void setLeftImageResource(int leftImageId){
        if(leftImageId > 0){
            imageLeft.setVisibility(VISIBLE);
            imageLeft.setImageResource(leftImageId);
        }else{
            imageLeft.setVisibility(GONE);
        }
    }

    public void setLeftImageClickListener(OnClickListener clickListener){
        imageLeft.setVisibility(VISIBLE);
        imageLeft.setOnClickListener(clickListener);
    }

    public void setLeftText(int strId){
        if(strId > 0){
            textLeft.setVisibility(VISIBLE);
            textLeft.setText(strId);
        }else{
            textLeft.setVisibility(GONE);
        }
    }

    public void setLeftTextCLickListener(OnClickListener leftTextCLickListener){
        textLeft.setOnClickListener(leftTextCLickListener);
    }

    public void setTitle(int titleStrId){
        if(titleStrId > 0){
            textTitle.setVisibility(VISIBLE);
            textTitle.setText(titleStrId);
        }else{
            textTitle.setVisibility(GONE);
        }
    }

    public void setTitle(String titleStr){
        if(!TextUtils.isEmpty(titleStr)){
            textTitle.setVisibility(VISIBLE);
            textTitle.setText(titleStr);
        }else{
            textTitle.setVisibility(GONE);
        }
    }

    public String getTitle(){
        return textTitle.getText().toString();
    }

    public void setRightTextVisibility(int visibility){
        textRight.setVisibility(visibility);
    }

    public void setRightText(int rightStrId){
        if(rightStrId > 0){
            textRight.setVisibility(VISIBLE);
            textRight.setText(rightStrId);
        }else{
            textRight.setVisibility(GONE);
        }
    }

    public void setRightText(String text){
        if(!TextUtils.isEmpty(text)){
            textRight.setVisibility(VISIBLE);
            textRight.setText(text);
        }else{
            textRight.setVisibility(GONE);
        }
    }

    public void setRightTextColor(int color)
    {
        textRight.setTextColor(color);
    }

    public void setRightTextClickListener(OnClickListener clickListener){
        textRight.setVisibility(VISIBLE);
        textRight.setOnClickListener(clickListener);
    }

    public void setRightImageResource(int rightImageId){
        if(rightImageId > 0){
            imageRight.setVisibility(VISIBLE);
            imageRight.setImageResource(rightImageId);
        }else{
            imageRight.setVisibility(GONE);
        }
    }

    public void setRightImageClickListener(OnClickListener clickListener){
        imageRight.setVisibility(VISIBLE);
        imageRight.setOnClickListener(clickListener);
    }

    public ImageView getImageRight() {
        return imageRight;
    }

    public void setCloseImageResource(int closeId){
        if(closeId > 0){
            imageClose.setVisibility(VISIBLE);
            imageClose.setImageResource(closeId);
        }else{
            imageClose.setVisibility(GONE);
        }
    }

    public void setCloseImageVisibility(int visibility){
        imageClose.setVisibility(visibility);
    }

    public void setCloseImageClickListener(OnClickListener clickListener){
        imageClose.setVisibility(VISIBLE);
        imageClose.setOnClickListener(clickListener);
    }

    public RadioGroup getRadioGroup(){
        return mRadioGroup;
    }

    public void setCheckLeft(){
        mRadioGroup.check(R.id.titlebar_rbtn_left);
    }

    public void setCheckRight(){
        mRadioGroup.check(R.id.titlebar_rbtn_right);
    }
}
