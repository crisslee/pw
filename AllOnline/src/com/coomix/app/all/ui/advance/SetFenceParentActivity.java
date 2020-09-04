package com.coomix.app.all.ui.advance;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.Fence;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.manager.ThemeManager;
import com.coomix.app.all.model.response.RespBase;
import com.coomix.app.all.model.response.RespQueryFence;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.manager.SettingDataManager;
import com.coomix.app.all.util.ViewUtil;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

import io.reactivex.disposables.Disposable;

public abstract class SetFenceParentActivity extends BaseActivity implements OnClickListener
{
    public static final String FENCE_DATA = "FENCE";
    private TextView mTextViewTitle;
    private ImageButton mImageButtonSwitch;
    private ImageView leftBtn;
    // private ImageButton leftBtn, rightBtn, mImageButtonSwitch,
    // mImageButtonMyLoc;
    protected ImageButton mImageButtonZoomIn, mImageButtonZoomOut, mIncreaseBtn, mReduceBtn;
    private ImageView mImageViewRange;
    private Button mButtonSet;
    // private Button mButtonSwitch;
    protected SeekBar mSeekBar;
    private TextView mTextViewRange;
    protected Fence mFence;
    protected Fence mNewFence;
    protected DeviceInfo mDevice;
    protected float mPreviousZoomLevel = 15f;
    protected float mMaxZoomLevel;
    protected float mMinZoomlevel;

    private Bitmap bitmapRadius;
    private Canvas canvas;
    private Paint mPaint;
    private Paint mPaint2;

    private String cachedPhone;

    protected int mRange = 200;

    private RelativeLayout mRelativeLayout;
    private ImageView mImageViewPin;
    private View mViewCenterAnchor;
    private Button mButtonFenceType;
    protected boolean isCircle = true;
    private BitmapDrawable markerOnline;
    private String indexDrag = "";
    private int indexPolyline1 = -1;
    private int indexPolyline2 = -1;
    private TextView mCircle;
    private TextView mPolygon;
    private RelativeLayout mCircleRelativeLayout;
    private LinearLayout mPolygonRelativeLayout;
    private Button resetPolygonFence;
    private Button setPolygonFence;
    private TextView fenceTip;
    private View popView;
    protected RelativeLayout layoutMapView;

    private void initNetData()
    {
        if(mDevice == null){
            return;
        }
        if (mFence == null)
        {
            Disposable d = DataEngine.getAllMainApi().queryFence(
                    GlobalParam.getInstance().getCommonParas(),
                    GlobalParam.getInstance().getAccessToken(),
                    AllOnlineApp.sAccount, mDevice.getImei(), "")
                    .compose(RxUtils.toMain())
                    .compose(RxUtils.businessTransformer())
                    .subscribeWith(new BaseSubscriber<RespQueryFence>() {
                        @Override
                        public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                            showErr(e.getErrCodeMessage());
                            hideLoading();
                        }

                        @Override
                        public void onNext(RespQueryFence respQueryFence) {
                            mFence = respQueryFence.getData();
                            initFence();
                            hideLoading();
                        }
                    });
            subscribeRx(d);
        }
    }

    protected void initFence()
    {
        if (mFence != null)
        {
            if (mFence.getShape_type() == Fence.SHAPE_CIRCLE)
            {
                isCircle = true;
                mRelativeLayout.setVisibility(View.VISIBLE);
                mImageViewPin.setVisibility(View.VISIBLE);
                mViewCenterAnchor.setVisibility(View.VISIBLE);
                mImageViewRange.setVisibility(View.VISIBLE);
                mTextViewRange.setVisibility(View.VISIBLE);
                mCircleRelativeLayout.setVisibility(View.VISIBLE);
                mPolygonRelativeLayout.setVisibility(View.GONE);
                fenceTip.setText(getResources().getString(R.string.set_fence_mess));
                titleCircleSelected();
                if (mFence.getRadius() > 0)
                {
                    mRange = mFence.getRadius();
                    mSeekBar.setProgress(mFence.getRadius() / 100 - 2);
                }
                setSwitchIcon(mFence.getValidate_flag());
            }
            else if (mFence.getShape_type() == Fence.SHAPE_POLYGON)
            {
                isCircle = false;
                mRelativeLayout.setVisibility(View.GONE);
                mImageViewPin.setVisibility(View.GONE);
                mViewCenterAnchor.setVisibility(View.GONE);
                mImageViewRange.setVisibility(View.GONE);
                mTextViewRange.setVisibility(View.GONE);
                mCircleRelativeLayout.setVisibility(View.GONE);
                mPolygonRelativeLayout.setVisibility(View.VISIBLE);
                fenceTip.setText(getResources().getString(R.string.set_polygon_fence_tip));
                titlePolygonSelected();
            }
        }
        else
        {
            mRelativeLayout.setVisibility(View.VISIBLE);
            mImageViewPin.setVisibility(View.VISIBLE);
            mViewCenterAnchor.setVisibility(View.VISIBLE);
            mImageViewRange.setVisibility(View.VISIBLE);
            mTextViewRange.setVisibility(View.VISIBLE);
            mCircleRelativeLayout.setVisibility(View.VISIBLE);
            mPolygonRelativeLayout.setVisibility(View.GONE);
            fenceTip.setText(getResources().getString(R.string.set_fence_mess));
            titleCircleSelected();
            mTextViewRange.setText(getString(R.string.fence_range, 200));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_set_fence);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ev_function", SettingDataManager.getInstance(this).getMapNameByType() + "地图围栏设置");
        MobclickAgent.onEvent(this, "ev_function", map);

        if(getIntent() != null && getIntent().hasExtra(FENCE_DATA)) {
            mFence = (Fence) getIntent().getSerializableExtra(FENCE_DATA);
        }
        if(getIntent() != null && getIntent().hasExtra(Constant.KEY_DEVICE)) {
            mDevice = (DeviceInfo) getIntent().getSerializableExtra(Constant.KEY_DEVICE);
        }else{
            mDevice = DeviceSettingActivity.device;
            CrashReport.postCatchedException(new Exception("高级设置中的围栏设置的intent device为null，account: " + AllOnlineApp.sAccount + ",Intent: " + getIntent()));
        }
        if(mDevice == null){
            finish();
            CrashReport.postCatchedException(new Exception("高级设置中的围栏设置的device为null，" + mDevice + ",Intent: " + getIntent()));
            return;
        }

        findViewById();

        initPins();

        setListener();

        initNetData();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    private void findViewById()
    {
        layoutMapView = (RelativeLayout) findViewById(R.id.layoutMapViewSetFence);
        mTextViewTitle = (TextView) findViewById(R.id.textViewTitle);
        leftBtn = (ImageView) findViewById(R.id.imageViewBack);
        // rightBtn = (ImageButton) findViewById(R.id.right_button);
        mImageViewRange = (ImageView) findViewById(R.id.img_range);
        mSeekBar = (SeekBar) findViewById(R.id.sbar_range);
        mTextViewRange = (TextView) findViewById(R.id.tv_range);
        // mCheckBoxSwitch = (CheckBox) findViewById(R.id.cbox_switch);
        mImageButtonSwitch = (ImageButton) findViewById(R.id.ibtn_switch);
        mImageButtonZoomIn = (ImageButton) findViewById(R.id.ibtn_zoom_in);
        mImageButtonZoomOut = (ImageButton) findViewById(R.id.ibtn_zoom_out);
        mButtonSet = (Button) findViewById(R.id.btn_set_fence);
        // mButtonSwitch = (Button) findViewById(R.id.btn_switch_fence);
        mIncreaseBtn = (ImageButton) findViewById(R.id.increaseBtn);
        mReduceBtn = (ImageButton) findViewById(R.id.reduceBtn);

        mRelativeLayout = (RelativeLayout) findViewById(R.id.seekbar_rl);
        mImageViewPin = (ImageView) findViewById(R.id.pin);
        mViewCenterAnchor = (View) findViewById(R.id.center_anchor);
        mCircle = (TextView) findViewById(R.id.headerRight1);
        mPolygon = (TextView) findViewById(R.id.headerRight2);
        mCircleRelativeLayout = (RelativeLayout) findViewById(R.id.choose_circle_fence);
        mPolygonRelativeLayout = (LinearLayout) findViewById(R.id.choose_polygon_fence);
        resetPolygonFence = (Button) findViewById(R.id.reset_polygon_fence);
        setPolygonFence = (Button) findViewById(R.id.set_polygon_fence);
        fenceTip = (TextView) findViewById(R.id.fence_tip);
        popView = LayoutInflater.from(this).inflate(R.layout.popup_view_layout, null);

        mTextViewTitle.setText(R.string.fence);
        findViewById(R.id.myActionbar).setBackgroundColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorWhole());
        mTextViewTitle.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
        mButtonSet.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
    }

    private void setListener()
    {
        leftBtn.setOnClickListener(this);
        // rightBtn.setOnClickListener(this);
        mImageButtonSwitch.setOnClickListener(this);
        mImageButtonZoomIn.setOnClickListener(this);
        mImageButtonZoomOut.setOnClickListener(this);
        mButtonSet.setOnClickListener(this);
        // mButtonSwitch.setOnClickListener(this);

        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRange = (mSeekBar.getProgress() + 2) * 100;
                zoomToSpan(false);
                drawPin(false);
            }
        });
        mIncreaseBtn.setOnClickListener(this);
        mReduceBtn.setOnClickListener(this);

        mCircle.setOnClickListener(this);
        mPolygon.setOnClickListener(this);
        resetPolygonFence.setOnClickListener(this);
        setPolygonFence.setOnClickListener(this);
    }

    protected View initPopView()
    {
        popView = LayoutInflater.from(SetFenceParentActivity.this).inflate(R.layout.popup_view_layout, null);
        return popView;
    }

    private void setSwitchIcon(int switchType) {
        if (switchType == Fence.SWITCH_ON) {
            // mImageButtonSwitch.setImageResource(R.drawable.icon_fence_off);
            // mButtonSwitch.setText(R.string.switch_fence_off);
            mButtonSet.setEnabled(true);
            ViewUtil.setBg(mButtonSet, ThemeManager.getInstance().getBGColorDrawable(this));
        } else {
            // mImageButtonSwitch.setImageResource(R.drawable.icon_fence_on);
            mButtonSet.setEnabled(false);
            mButtonSet.setBackgroundResource(R.drawable.login_btn_disable);
            ViewUtil.setBg(mButtonSet, ThemeManager.getInstance().getDefaultDrawable(this));
            // mButtonSwitch.setText(R.string.switch_fence_on);
        }
    }

    /**
     * function to load map. If map is not created it will create it for you
     */
    //    private void initilizeMap()
    //    {
    //        if (aMap == null)
    //        {
    //            aMap = mapView.getMap();
    //            mUiSettings = aMap.getUiSettings();
    //            mUiSettings.setZoomControlsEnabled(false);
    //        }
    //        mMaxZoomLevel = aMap.getMaxZoomLevel();
    //        mMinZoomlevel = aMap.getMinZoomLevel();
    //        LatLng locationLatLng = new LatLng(mDevice.state.lat, mDevice.state.lng);
    //        Marker marker = aMap.addMarker(new MarkerOptions().position(locationLatLng).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fence_location_marker))).perspective(true).draggable(false));
    //    }
    private void initPins()
    {
        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.FILL_AND_STROKE);
        mPaint.setColor(Fence.FILL_COLOR);
        mPaint2 = new Paint();
        mPaint2.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setStyle(Style.STROKE);
        mPaint2.setColor(Fence.STROKE_COLOR);
        mPaint2.setStrokeWidth(3);
    }

    protected void drawPin(boolean bDeviceLoc)
    {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        if (bitmapRadius != null)
        {
            bitmapRadius.recycle();
            bitmapRadius = null;
        }
        bitmapRadius = Bitmap.createBitmap(width, height, conf);
        canvas = new Canvas(bitmapRadius);
        mTextViewRange.setText(getString(R.string.fence_range, mRange));

        canvas.drawCircle(bitmapRadius.getWidth() / 2, bitmapRadius.getHeight() / 2, pixel, mPaint);
        canvas.drawCircle(bitmapRadius.getWidth() / 2, bitmapRadius.getHeight() / 2, pixel, mPaint2);
        mImageViewRange.setBackgroundDrawable(new BitmapDrawable(bitmapRadius));
    }

    protected int width;
    protected int height;
    protected float pixel;

    protected int calculateDistance(Point p1, Point p2)
    {
        int dx = p1.x - p2.x;
        int dy = p1.y - p2.y;

        return (int) Math.sqrt((dx * dx) + (dy * dy));
    }

    /**
     * Generate LatLng of radius marker
     */
    //    private static LatLng toRadiusLatLng(LatLng center, double radius)
    //    {
    //        double radiusAngle = Math.toDegrees(radius / RADIUS_OF_EARTH_METERS) / Math.cos(Math.toRadians(center.latitude));
    //        return new LatLng(center.latitude, center.longitude + radiusAngle);
    //    }
    protected void addFence()
    {
        //子类还有实现部分
        showProgressDialog(getString(R.string.loading_set_fence));
        mNewFence = new Fence();
    }

    protected void sendAddFenceRequest(String shapeParam,int shape)
    {
        Disposable disposable = DataEngine.getAllMainApi().addFence(
                GlobalParam.getInstance().getCommonParas(),
                GlobalParam.getInstance().getAccessToken(),
                mDevice.getImei(),
                shape,
                shapeParam,
                Fence.SWITCH_ON,
                AllOnlineApp.sAccount,
                Fence.ALARM_TYPE_NORMAL)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBase>() {
                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showErr(e.getErrCodeMessage());
                        hideLoading();
                    }

                    @Override
                    public void onNext(RespBase respBase) {
                        hideLoading();
                       showToast( getString(R.string.add_fence_success));
                        if (mNewFence != null && mFence != null)
                        {
                            mFence.setShape_type(mNewFence.getShape_type());
                            mFence.setShape_param(mNewFence.getShape_param());
                            mFence.setLat(mNewFence.getLat());
                            mFence.setLng(mNewFence.getLng());
                            mFence.setRadius(mNewFence.getRadius());
                        }
                        Intent intent = new Intent();
                        if (mFence != null)
                        {
                            intent.putExtra("newFence", mFence);
                        }
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                });
        subscribeRx(disposable);
//        mSetFenceTaskID = mServiceAdapter.addFence(this.hashCode(), AllOnlineApp.sToken.access_token,
//                shape, shape_param, Fence.SWITCH_ON, AllOnlineApp.sAccount, SettingDataManager.strMapType, mDevice.imei);
    }

    private void switchFence(int switchType)
    {
        // mIsSwitchOn = switchType;

        showProgressDialog(getString(R.string.loading_set_fence));
        Disposable disposable = DataEngine.getAllMainApi()
                .switchFence(GlobalParam.getInstance().getCommonParas(), AllOnlineApp.sAccount,
                GlobalParam.getInstance().getAccessToken(),
                mDevice.getImei(),
                mFence.getId(),
                switchType,
                Fence.ALARM_TYPE_NORMAL)
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespBase>(){

                    @Override
                    public void onNext(RespBase respBase) {
                        mFence.setValidate_flag(mFence.getValidate_flag() == Fence.SWITCH_ON ? Fence.SWITCH_OFF : Fence.SWITCH_ON);
                        setSwitchIcon(mFence.getValidate_flag());
                        if (mFence.getValidate_flag() == Fence.SWITCH_OFF)
                        {
                            Toast.makeText(SetFenceParentActivity.this, getString(R.string.switch_fence_off_toast), Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(SetFenceParentActivity.this, getString(R.string.switch_fence_on_toast), Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent();
                        intent.putExtra("switch", mFence.getValidate_flag());
                        setResult(Activity.RESULT_OK, intent);
                    }

                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                        showToast(e.getErrCodeMessage());
                        hideLoading();
                    }
                });
        subscribeRx(disposable);
//        mSwitchFenceTaskID = mServiceAdapter.switchFence(this.hashCode(), AllOnlineApp.sToken.access_token,
//                mFence.id, switchType, AllOnlineApp.sAccount, SettingDataManager.strMapType, mDevice.imei);

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.imageViewBack:
                finish();
                break;

            case R.id.ibtn_switch:
                mImageButtonSwitch.setEnabled(false);
                if (mFence.getValidate_flag() == Fence.SWITCH_ON)
                {
                    switchFence(Fence.SWITCH_OFF);
                }
                else
                {
                    switchFence(Fence.SWITCH_ON);
                }
                break;

            case R.id.ibtn_zoom_in:
                zoomIn();
                break;

            case R.id.ibtn_zoom_out:
                zoomOut();
                break;

            case R.id.btn_set_fence:
                addFence();
                break;

            case R.id.increaseBtn:
                int progress = mSeekBar.getProgress();
                if (progress < mSeekBar.getMax())
                {
                    mSeekBar.setProgress(progress + 1);
                }
                break;

            case R.id.reduceBtn:
                int progress1 = mSeekBar.getProgress();
                if (progress1 > 0)
                {
                    mSeekBar.setProgress(progress1 - 1);
                }
                break;

            case R.id.headerRight1:
                circleClicked();
                break;

            case R.id.headerRight2:
                polygonClicked();
                break;

            case R.id.reset_polygon_fence:
                polygonFenceClicked();
                break;

            case R.id.set_polygon_fence:
                addFence();
                break;

                default:
                    break;
        }
    }

    protected void circleClicked()
    {
        mRelativeLayout.setVisibility(View.VISIBLE);
        mImageViewPin.setVisibility(View.VISIBLE);
        mViewCenterAnchor.setVisibility(View.VISIBLE);
        mImageViewRange.setVisibility(View.VISIBLE);
        mTextViewRange.setVisibility(View.VISIBLE);
        mCircleRelativeLayout.setVisibility(View.VISIBLE);
        mPolygonRelativeLayout.setVisibility(View.GONE);
        fenceTip.setText(getResources().getString(R.string.set_fence_mess));
        titleCircleSelected();
        isCircle = true;
        drawPin(true);
    }

    protected void polygonClicked()
    {
        mRelativeLayout.setVisibility(View.GONE);
        mImageViewPin.setVisibility(View.GONE);
        mViewCenterAnchor.setVisibility(View.GONE);
        mImageViewRange.setVisibility(View.GONE);
        mTextViewRange.setVisibility(View.GONE);
        mCircleRelativeLayout.setVisibility(View.GONE);
        mPolygonRelativeLayout.setVisibility(View.VISIBLE);
        fenceTip.setText(getResources().getString(R.string.set_polygon_fence_tip));
        titlePolygonSelected();
        isCircle = false;
    }

    private void titleCircleSelected(){
        mCircle.setSelected(true);
        mPolygon.setSelected(false);
        mCircle.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
        mPolygon.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorUntouchableBtn());
    }

    private void titlePolygonSelected(){
        mCircle.setSelected(false);
        mPolygon.setSelected(true);
        mCircle.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorUntouchableBtn());
        mPolygon.setTextColor(ThemeManager.getInstance().getThemeAll().getThemeColor().getColorNavibarText());
    }

    @Override
    protected void onDestroy()
    {
        if (bitmapRadius != null)
        {
            bitmapRadius.recycle();
            bitmapRadius = null;
        }

        super.onDestroy();
    }

    protected abstract void zoomToSpan(boolean bDeviceLoc);
    protected abstract void animateCamera(double lat, double lng);
    protected abstract void zoomIn();
    protected abstract void zoomOut();
    protected abstract void polygonFenceClicked();
}
