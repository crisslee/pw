package com.coomix.app.all.widget;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.coomix.app.all.R;

public class ClickableToast implements Runnable {
    static final String TAG = "ClickableToast";
    WindowManager.LayoutParams mWindowParams;
    WindowManager mWindowManager;
    Context mContext;
    View mView;

    public ClickableToast(Context mContext) {
        this.mContext = mContext;
        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        mWindowParams.x = 0;
        mWindowParams.y = 0;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = android.R.style.Animation_Toast;
        mWindowParams.verticalMargin = 0.7f;

        LayoutInflater inflate = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflate.inflate(R.layout.layout_clickable_toast, null);
        
    }
    
    public void setOnClickListener(OnClickListener onClickListener) {
        mView.setOnClickListener(onClickListener);
    }

    public void show() {
        // ImageView v = new ImageView(mContext);
        // v.setBackgroundColor(backGroundColor);
        // v.setImageBitmap(bm);
        Handler mHandler = new Handler();

        mWindowManager = (WindowManager) mContext.getSystemService("window");
        mWindowManager.addView(mView, mWindowParams);
        mHandler.postDelayed(this, 5000);
    }

    public void hide() {
        if (mView != null) {
            if (mView.getParent() != null) {
                mWindowManager.removeView(mView);
            }

        }
    }

    @Override
    public void run() {
        hide();
    }

}
