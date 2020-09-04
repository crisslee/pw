package com.coomix.app.all.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.bumptech.GlideApp;
import com.coomix.app.all.R;

public class VRoundImageView extends LinearLayout {
    private Context mContext;
    private ImageView imageIcon;
    private ImageView imageV;

    public VRoundImageView(Context context) {
        this(context, null, 0);
    }

    public VRoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VRoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context.getApplicationContext();
        initViews();
    }

    private void initViews() {
        View mainView = LayoutInflater.from(mContext).inflate(R.layout.v_user_icon_view, this);
        imageIcon = (ImageView) mainView.findViewById(R.id.imageViewIcon);
        imageV = (ImageView) mainView.findViewById(R.id.imageViewV);
        imageV.setVisibility(GONE);
    }

    public void setAvatorSize(int size) {
        imageIcon.setLayoutParams(new RelativeLayout.LayoutParams(size, size));
    }

    public void setImageResource(int resID) {
        imageIcon.setImageResource(resID);
    }

    public ImageView getIconView() {
        return imageIcon;
    }

    public ImageView getVView() {
        return imageV;
    }

    public void setVImageResource(int resID) {
        imageV.setImageResource(resID);
    }

    public void setAvatarImage(String iconUrl, int size) {
        imageIcon.setLayoutParams(new RelativeLayout.LayoutParams(size, size));
        GlideApp.with(mContext)
            .load(iconUrl)
            .override(size, size)
            .placeholder(R.drawable.login_icon_large)
            .error(R.drawable.login_icon_large)
            .into(imageIcon);
    }

    public void setVVisibility(int visibility) {
        imageV.setVisibility(visibility);
    }

    public void setVImage(int vType, int vSize) {
        int resID = 0;
        switch (vType) {
            case 1://01
            case 3://11
                //官方账号--蓝色
                resID = R.drawable.v_official;
                break;

            case 2://10
                //官方认证的账号--金黄色
                resID = R.drawable.v_renzheng;
                break;

            default:
                imageV.setVisibility(GONE);
                return;
        }

        imageV.setVisibility(VISIBLE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(vSize, vSize);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        imageV.setLayoutParams(params);
        imageV.setImageResource(resID);
    }

    public void setUserData(String avatarUrl, int avatarSize, int vType, int vSize) {
        setAvatarImage(avatarUrl, avatarSize);
        setVImage(vType, vSize);
    }

    //    public void setRadiusOffset(float radiusOffset)
    //    {
    //        imageIcon.setRadiusOffset(radiusOffset);
    //    }
}
