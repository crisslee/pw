package com.coomix.app.all.ui.history;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.coomix.app.all.AllOnlineApp;
import com.coomix.app.all.Constant;
import com.coomix.app.all.manager.DeviceManager;
import com.coomix.app.all.GlobalParam;
import com.coomix.app.all.R;
import com.coomix.app.all.model.bean.DeviceInfo;
import com.coomix.app.all.model.bean.TrackPoint;
import com.coomix.app.all.model.bean.TrackPointWithSpeed;
import com.coomix.app.all.model.bean.TrackPoints;
import com.coomix.app.all.map.baidu.OverlayType;
import com.coomix.app.framework.util.CommonUtil;
import com.coomix.app.framework.util.PreferenceUtil;
import com.coomix.app.framework.util.TimeUtil;
import com.coomix.app.all.data.BaseSubscriber;
import com.coomix.app.all.data.DataEngine;
import com.coomix.app.all.data.ExceptionHandle;
import com.coomix.app.all.data.RxUtils;
import com.coomix.app.all.model.response.RespAddress;
import com.coomix.app.all.model.response.RespTrackPoints;
import com.coomix.app.all.ui.base.BaseActivity;
import com.coomix.app.all.manager.SettingDataManager;
import com.umeng.analytics.MobclickAgent;
import io.reactivex.disposables.Disposable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.microedition.khronos.opengles.GL10;

public abstract class HistoryParentActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = HistoryParentActivity.class.getSimpleName();

    public static final String START_TIME = "START_TIME";
    public static final String END_TIME = "END_TIME";
    protected boolean isFilterLbs = false;

    @BindView(R.id.back)
    TextView back;
    protected SeekBar mSeekBar;
    protected int mProgress;
    protected boolean mReplay;
    protected DeviceInfo mDevice;
    protected long lastPointTimeLong;
    protected int mCount;
    private final static int NUM_OF_POINTS = 1000;
    /**
     * 上一次网络请求的mTraceEndTime值
     */
    protected static long LAST_REQUEST_mTraceEndTime;
    //动画线程控制
    private volatile boolean mIsInterrupt;
    private volatile boolean mIsPause;
    protected long respEndTime;
    protected CopyOnWriteArrayList<TrackPoint> mArrayListTrackPoints = new CopyOnWriteArrayList<TrackPoint>();
    protected final static Object mLocker = new Object();
    protected int  mCurrentIndex;
    private Thread thread;
    private final static int UPDATE_MAP_VIEW = 1;
    protected TrackPoint mLastTrackPoint;
    protected int mTotalDistance;
    protected long mTotalStayTime;
    protected long currentRunTime;
    protected long totalRunTime;
    protected ArrayList<TrackPoint> mArrayListStayPoints;
    private final int STAY_POINT_INTERVAL = 60;

    protected TrackPoint curShowStayPoint;
    private PowerManager.WakeLock mWakeLock;
    private boolean isFirstInto;

    // stay point interval
    private int historyInterval = 0;

    protected SettingDataManager setManager;
    protected RelativeLayout layoutMapView;
    protected ImageView imagePlay;
    protected TextView textDateTime, textSpeed, textDistance, textStartTime;
    @BindView(R.id.txtTotalTime)
    TextView txtTotalTime;

    protected View mPopupView;
    private TextView textViewStart;
    private TextView textViewEnd;
    private TextView textViewTotal;
    private TextView textViewAddress;
    private TextView pop_name_position;

    protected int stayNum;//停留点数目

    private long traceStartTime;
    protected long traceEndTime;

    @SuppressWarnings("HandlerLeak")
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_MAP_VIEW:
                    updateView((Integer) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_map);
        ButterKnife.bind(this);

        setManager = SettingDataManager.getInstance(this);
        MobclickAgent.onEvent(this, "ev_history");
        MobclickAgent.onEvent(this, "ev_function",
            new HashMap<String, String>().put("ev_function", setManager.getMapNameByType() + "地图历史回放"));
        historyInterval = 600;

        PreferenceUtil.init(this);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "all:MyTag");

        initIntentData();

        isFirstInto = true;
        mProgress = 0;
        mReplay = false;

        initViews();
        initData();
        setListener();

        thread = new Thread(animate);
        thread.start();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mWakeLock != null && !mWakeLock.isHeld()) {
            this.mWakeLock.acquire();
        }
    }

    @Override
    public void onResume() {
        mIsPause = false;// 此时 是选中，可以画出轨迹的时候
        updatePlayBtn();
        if (isFirstInto) {
            isFirstInto = false;
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        mIsPause = true;// 此时 是暂停的状态
        updatePlayBtn();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mIsInterrupt = true;
        synchronized (mLocker) {
            mLocker.notifyAll();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (thread != null) {
            thread.interrupt();
        }
        super.onDestroy();
    }

    private void initIntentData() {
        Intent i = getIntent();
        if (i == null) {
            finish();
            return;
        }
        if (i.hasExtra(Constant.KEY_DEVICE)) {
            mDevice = (DeviceInfo) i.getSerializableExtra(Constant.KEY_DEVICE);
            if (mDevice == null) {
                finish();
                return;
            }
        }
        if (i.hasExtra(START_TIME)) {
            traceStartTime = i.getLongExtra(START_TIME, 0);
        }
        if (i.hasExtra(END_TIME)) {
            traceEndTime = i.getLongExtra(END_TIME, 0);
            long curr = System.currentTimeMillis() / 1000;
            if (traceEndTime > curr) {
                traceEndTime = curr;
            }
        }
    }

    private void initViews() {
        back.setOnClickListener(this);
        layoutMapView = (RelativeLayout) findViewById(R.id.map_relative_layout);
        mSeekBar = (SeekBar) findViewById(R.id.sbar_history);
        textDateTime = (TextView) findViewById(R.id.textViewDateTime);
        textSpeed = (TextView) findViewById(R.id.textViewSpeed);
        textDistance = (TextView) findViewById(R.id.textViewDistance);
        textStartTime = (TextView) findViewById(R.id.textViewPlayTime);
        imagePlay = (ImageView) findViewById(R.id.iamgeViewPlay);
        imagePlay.setOnClickListener(this);
    }

    private void initData() {
        textDistance.setText(getString(R.string.label_mileage) + mTotalDistance);
        textSpeed.setText(getString(R.string.label_speed) + getHistorySpeed(mDevice.getSpeed()));
        textDateTime.setText(TimeUtil.long2DateTimeString(mDevice.getGps_time() * 1000));

        mIsInterrupt = false;
        mIsPause = false;
        mCount = 0;
        stayNum = 0;
        mCurrentIndex = 1;
        LAST_REQUEST_mTraceEndTime = 0;

        mArrayListStayPoints = new ArrayList<TrackPoint>();

        initHistory();
    }

    private void setListener() {
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgress = progress;
                if (progress == 100) {
                    mIsPause = true;
                    updatePlayBtn();
                }
                if (fromUser || mReplay) {
                    mReplay = false;
                    if (progress < 100) {

                    }
                    progressChange(progress);
                }
            }
        });
    }

    protected boolean isShiftPoint(TrackPoint lastTrackPoint, TrackPoint currentTrackPoint) {
        double distance = getDistance(lastTrackPoint, currentTrackPoint);

        long diffInSec = currentTrackPoint.gps_time - lastTrackPoint.gps_time;
        int speed = 0;
        if (diffInSec != 0) {
            speed = (int) (distance / diffInSec * 60 * 60);
        }
        if (speed > 200000) {
            return true;
        } else {
            mTotalDistance += distance;
            return false;
        }
    }

    // 不计算距离
    protected boolean isShiftPointNoDistance(TrackPoint lastTrackPoint, TrackPoint currentTrackPoint) {
        if (lastTrackPoint == null || currentTrackPoint == null) {
            return false;
        }
        double distance = getDistance(lastTrackPoint, currentTrackPoint);

        long diffInSec = currentTrackPoint.gps_time - lastTrackPoint.gps_time;
        int speed = 0;
        if (diffInSec != 0) {
            speed = (int) (distance / diffInSec * 60 * 60);
        }
        if (speed > 200000) {
            return true;
        } else {
            return false;
        }
    }

    Runnable animate = new Runnable() {
        @Override
        public void run() {
            while (!mIsInterrupt) {
                try {
                    synchronized (mLocker) {
                        if (mCurrentIndex > mArrayListTrackPoints.size() || mIsPause) {
                            if (mProgress == 100) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        updatePlayBtn();
                                    }
                                });
                            }
                            mLocker.wait();
                        }
                    }

                    if (!mIsInterrupt && mCurrentIndex > 0) {
                        Message msg = mHandler.obtainMessage(UPDATE_MAP_VIEW, mCurrentIndex);
                        mHandler.sendMessage(msg);
                        if (mArrayListTrackPoints != null && mCurrentIndex <= mArrayListTrackPoints.size()) {
                            int iIndex = mCurrentIndex - 1;
                            if (iIndex < 0) {
                                iIndex = 0;
                            }
                            TrackPoint tPoint = mArrayListTrackPoints.get(iIndex);
                            if ((mLastTrackPoint == null || !isShiftPointNoDistance(mLastTrackPoint, tPoint))
                                && (!isFilterLbs || !isLbsPoint(tPoint))) {
                                //如果已经有计算得到的速度，则直接使用
                                if(tPoint instanceof TrackPointWithSpeed){
                                    Thread.sleep(((TrackPointWithSpeed) tPoint).historySpeed);
                                }else {//否则使用默认速度
                                    Thread.sleep(Constant.HISTORY_PLAY_SPEED);
                                }

                            }
                        }
                    }

                    //这里多线程同时++有可能导致+两次，所以synchronized一下
                    synchronized (mLocker){
                        if (mCurrentIndex < mArrayListTrackPoints.size()) {
                            mCurrentIndex++;
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    protected void displayStatistic(TrackPoint point) {
        mSeekBar.setVisibility(View.GONE);
        addEndPoint(point);
        String summary = getString(R.string.total_distance) + CommonUtil.formatRangeSize(this, mTotalDistance)
            + "  " + getString(R.string.total_stay_time) + TimeUtil.getTimeDHMS(this, mTotalStayTime, 2);
        textStartTime.setText(summary);
    }

    protected boolean isLbsPoint(TrackPoint point) {
        if (point != null && point.speed == -1) {
            return true;
        }
        return false;
    }

    public void checkIsStayPoint(TrackPoint lastPoint, TrackPoint curPoint) {
        if (lastPoint != null) {
            // stay point interval
            if (curPoint.gps_time - lastPoint.gps_time > historyInterval) {
                TrackPoint stayPoint = new TrackPoint();
                stayPoint.startTime = lastPoint.gps_time;
                stayPoint.endTime = curPoint.gps_time;
                stayPoint.stayTime = (stayPoint.endTime - stayPoint.startTime);
                stayPoint.lat = lastPoint.lat;
                stayPoint.lng = lastPoint.lng;

                mArrayListStayPoints.add(stayPoint);// 停留轨迹
                addStayPoint(stayPoint);// 停留點 显示A的位置点
            }
        }
    }

    public void addStayTime(TrackPoint lastPoint, TrackPoint curPoint) {
        if (lastPoint != null && curPoint != null) {
            long time = curPoint.gps_time - lastPoint.gps_time;
            if (time > historyInterval) {
                mTotalStayTime += time;
            } else {
                currentRunTime += time;
            }
        }
    }

    protected void resetData() {
        mLastTrackPoint = null;
        mArrayListTrackPoints.clear();
        mArrayListStayPoints.clear();
        mCount = 0;
        stayNum = 0;
        mTotalDistance = 0;
        mTotalStayTime = 0;
        currentRunTime = 0;
        totalRunTime = 0;
        mCurrentIndex = 1;
        mSeekBar.setProgress(0);
        LAST_REQUEST_mTraceEndTime = 0;

        if (thread == null || !thread.isAlive()) {
            thread = new Thread(animate);
            thread.start();
        }
    }

    protected void initHistory() {
        resetData();
        showLoading(getString(R.string.loading));
        requestHistoryData(traceStartTime);
    }

    protected void initNewHistory() {
        resetData();
        requestHistoryData(respEndTime);
    }

    protected void requestHistoryData(long startTime) {
        long curr = System.currentTimeMillis() / 1000;
        Disposable d = DataEngine.getAllMainApi()
            .history(AllOnlineApp.sToken.access_token, mDevice.getImei(), AllOnlineApp.sAccount, startTime,
                traceEndTime, curr, NUM_OF_POINTS, GlobalParam.getInstance().getCommonParas())
            .compose(RxUtils.toIO())
            .compose(RxUtils.businessTransformer())
            .subscribeWith(new BaseSubscriber<RespTrackPoints>() {
                @Override
                public void onHttpError(ExceptionHandle.ResponeThrowable e) {
                    hideLoading();
                    showToast(e.getErrCode() + "," + e.getErrMessage());
                    LAST_REQUEST_mTraceEndTime = 0;
                }

                @Override
                public void onNext(RespTrackPoints respTrackPoints) {
                    processData(respTrackPoints.getData());
                }
            });
        subscribeRx(d);
    }

    private void processData(TrackPoints data) {
        if (data == null || data.getTrackPoints() == null || data.getTrackPoints().size() == 0) {
            hideLoading();
            if (mArrayListTrackPoints.size() == 0) {
                showToast(getString(R.string.no_history));
                mIsPause = true;
            }
            updateTotalTime();
            return;
        }

        //计算每个点的回放速度，并加到mArrayListTrackPoints中
        handleHistorySpeed(data);

        if (data.getResEndTime() > 0) {
            respEndTime = data.getResEndTime();
            if (Constant.IS_DEBUG_MODE) {
                int size = mArrayListTrackPoints.size();
                Log.d(TAG, "respEndTime=" + respEndTime + ", lastPoint="
                    + mArrayListTrackPoints.get(size - 1).toString());
            }
        }
        if (respEndTime < traceEndTime) {
            requestHistoryData(respEndTime);
        } else {
            hideLoading();
            updateTotalTime();
        }
        if (!mIsPause) {
            synchronized (mLocker) {
                mLocker.notifyAll();
            }
        }
    }


    /**
     * 计算每个点的回放速度
     * 现在由于10秒内设备可能上报多个点，所以需要动态计算每两个点之间的播放速度
     * @param data
     */
    private void handleHistorySpeed(TrackPoints data){

        List<TrackPoint> preTrackPoints = data.getTrackPoints();

        if(preTrackPoints == null || preTrackPoints.isEmpty()){
            return;
        }


        int startIndex = 0;
        long startTime = preTrackPoints.get(0).gps_time;
        for(int i = 0; i < preTrackPoints.size(); i++){

            TrackPoint point = preTrackPoints.get(i);

            //以10秒为一个区间，用HISTORY_PLAY_SPEED/区间内点数，取平均值
            if(i > startIndex && (point.gps_time - startTime > 10)){

                int historySpeed = (Constant.HISTORY_PLAY_SPEED/(i - startIndex));
                if(historySpeed <= 0){
                    historySpeed = Constant.HISTORY_PLAY_SPEED;
                }

                for(int j = startIndex; j < i; j++){
                    TrackPointWithSpeed trackPointWithSpeed = new TrackPointWithSpeed(preTrackPoints.get(j));
                    trackPointWithSpeed.historySpeed = historySpeed;
                    mArrayListTrackPoints.add(trackPointWithSpeed);
                }

                startIndex = i;
                startTime = preTrackPoints.get(i).gps_time;
            }


            if(i == preTrackPoints.size() - 1){

                int historySpeed = Constant.HISTORY_PLAY_SPEED;
                if(i > startIndex){
                    historySpeed = (Constant.HISTORY_PLAY_SPEED/(i - startIndex));
                }

                if(historySpeed <= 0){
                    historySpeed = Constant.HISTORY_PLAY_SPEED;
                }

                for(int j = startIndex; j < preTrackPoints.size(); j++){
                    TrackPointWithSpeed trackPointWithSpeed = new TrackPointWithSpeed(preTrackPoints.get(j));
                    trackPointWithSpeed.historySpeed = historySpeed;
                    mArrayListTrackPoints.add(trackPointWithSpeed);
                }
            }
        }
    }

    private void updateTotalTime() {
        if (mArrayListTrackPoints == null) {
            return;
        }
        int size = mArrayListTrackPoints.size();
        for (int i = 1; i < size; i++) {
            TrackPoint prePoint = mArrayListTrackPoints.get(i - 1);
            TrackPoint point = mArrayListTrackPoints.get(i);
            if (prePoint == null || point == null) {
                continue;
            }
            if (!isShiftPoint(prePoint, point)) {
                long time = point.gps_time - prePoint.gps_time;
                if (time <= historyInterval) {
                    totalRunTime += time;
                }
            }
        }
        mTotalDistance = 0;
        runOnUiThread(() -> {
            txtTotalTime.setText(TimeUtil.sec2Time(totalRunTime));
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back:
                //toSelectDate();
                finish();
                break;
            case R.id.iamgeViewPlay:
                if (mIsPause) {
                    if (mProgress == 100) {
                        mReplay = true;
                        mSeekBar.setProgress(0);
                    }
                    mIsPause = false;
                    synchronized (mLocker) {
                        mLocker.notifyAll();
                    }
                } else {
                    mIsPause = true;
                }
                updatePlayBtn();
                break;
            default:
                break;
        }
    }

    private void toSelectDate() {
        Intent i = new Intent(this, SelectDateHistoryActivity.class);
        i.putExtra(Constant.KEY_DEVICE, mDevice);
        startActivity(i);
    }

    private void updatePlayBtn() {
        if (mIsPause) {
            imagePlay.setImageResource(R.drawable.icon_play);
        } else {
            imagePlay.setImageResource(R.drawable.icon_pause);
        }
    }

    private SparseArray<TrackPoint> listClickedStayPoint = new SparseArray<TrackPoint>();

    public void reverseGeoAddpointList(TrackPoint trackPoint) {
        if (trackPoint != null && trackPoint.lat != 0 && trackPoint.lng != 0) {
            Disposable d = DataEngine.getAllMainApi()
                .reverseGeo(AllOnlineApp.sToken.access_token, AllOnlineApp.sAccount, trackPoint.lat, trackPoint.lng,
                    GlobalParam.getInstance().getCommonParas())
                .compose(RxUtils.toMain())
                .compose(RxUtils.businessTransformer())
                .subscribeWith(new BaseSubscriber<RespAddress>() {
                    @Override
                    public void onHttpError(ExceptionHandle.ResponeThrowable e) {

                    }

                    @Override
                    public void onNext(RespAddress respAddress) {
                        RespAddress.Address address = respAddress.getData();
                        String addr = address.address;
                        if (!TextUtils.isEmpty(addr)) {
                            DeviceManager.getInstance().setCachedAddress(trackPoint.lng, trackPoint.lat, addr);
                            listClickedStayPoint.remove(trackPoint.hashCode());
                        }
                        //当前停留点返回数据了，更新地址
                        showStayPointInfo(curShowStayPoint);
                    }
                });
            subscribeRx(d);
            listClickedStayPoint.put(trackPoint.hashCode(), trackPoint);
        }
    }

    protected void showStayPointInfo(TrackPoint trackPoint) {
        if (trackPoint != null) {
            LayoutInflater infalInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mPopupView = infalInflater.inflate(R.layout.popup_layout_history, null);
            textViewStart = (TextView) mPopupView.findViewById(R.id.pop_start_time);
            textViewEnd = (TextView) mPopupView.findViewById(R.id.pop_end_time);
            textViewTotal = (TextView) mPopupView.findViewById(R.id.pop_total_time);
            textViewAddress = (TextView) mPopupView.findViewById(R.id.pop_address);
            pop_name_position = (TextView) mPopupView.findViewById(R.id.pop_name_position);

            textViewStart.setText(CommonUtil.getTimeString(trackPoint.startTime * 1000));
            textViewEnd.setText(CommonUtil.getTimeString(trackPoint.endTime * 1000));
            textViewTotal.setText(TimeUtil.getTimeDHMS(this, trackPoint.stayTime, 2));

            String cacheAddress = DeviceManager.getInstance().getCachedAddress(trackPoint.lng, trackPoint.lat);
            if (TextUtils.isEmpty(cacheAddress)) {
                textViewAddress.setText(R.string.reverse);
                reverseGeoAddpointList(trackPoint);
            } else {
                textViewAddress.setText(cacheAddress);
            }

            for (int i = 0; i < mArrayListStayPoints.size(); i++) {
                TrackPoint trPoint = mArrayListStayPoints.get(i);
                if (trPoint != null && trPoint.equals(trackPoint)) {
                    pop_name_position.setText((i + 1) + "");
                    break;
                }
            }
            Bundle extra = new Bundle();
            extra.putString("type", OverlayType.OVERLAY_STAYPOINT);
            showInfoWindow(trackPoint, extra);
        }
    }

    protected void drawCircle(GL10 gl, int color, PointF point, float radius) {
        if (radius == 0 || point == null) {
            return;
        }

        final int circleVertexSize = 10; // 因为需要画的圆半径较小，取10份即可，要提高精度，增大份数
        final int dimension = 2; // 使用2维平面

        // 圆
        float[] circleVertexs = new float[dimension * circleVertexSize];
        double per = 2 * Math.PI / circleVertexSize;
        for (int i = 0; i < circleVertexSize; i++) {
            circleVertexs[i * dimension] = ((float) Math.cos(i * per)) * radius + point.x;
            circleVertexs[i * dimension + 1] = ((float) Math.sin(i * per)) * radius + point.y;
            // circleVertexs[i * dimension + 2] = 0.0f;
        }
        FloatBuffer circleBuffer = makeFloatBuffer(circleVertexs);

        gl.glPushMatrix();
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        float colorA = Color.alpha(color) / 255f;
        float colorR = Color.red(color) / 255f;
        float colorG = Color.green(color) / 255f;
        float colorB = Color.blue(color) / 255f;

        // 绘线
        gl.glEnable(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(dimension, GL10.GL_FLOAT, 0, circleBuffer);
        gl.glColor4f(colorR, colorG, colorB, colorA);
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, circleVertexSize);
        gl.glDisable(GL10.GL_VERTEX_ARRAY);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glPopMatrix();
        gl.glFlush();
    }

    protected View getStayPointView() {
        View view = View.inflate(this, R.layout.history_stay_marker_layout, null);
        TextView numTv = (TextView) view.findViewById(R.id.history_stay_num);

        stayNum = stayNum + 1;
        if (stayNum <= 9) {
            numTv.setTextSize(14);
            //numTv.setTextSize(getResources().getDimensionPixelSize(R.dimen.textsize_xsmall));
        } else if (stayNum <= 99) {
            numTv.setTextSize(13);
            //numTv.setTextSize(getResources().getDimensionPixelSize(R.dimen.textsize_xxsmall));
        } else {
            numTv.setTextSize(12);
            //numTv.setTextSize(getResources().getDimensionPixelSize(R.dimen.textsize_xxxsmall));
        }
        numTv.setText(String.valueOf(stayNum));

        return view;
    }

    private FloatBuffer makeFloatBuffer(float[] fs) {
        ByteBuffer bb = ByteBuffer.allocateDirect(fs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(fs);
        fb.position(0);
        return fb;
    }

    protected static String getHistorySpeed(int speed) {
        if(speed == -1) {
            // 基站定位可能返回速度为-1，此时不显示速度
            return "";
        } else {
            return speed + " km/h";
        }
    }

    protected abstract void addStayPoint(TrackPoint trackPoint);

    protected abstract void showInfoWindow(TrackPoint trackPoint, Bundle extra);

    protected abstract double getDistance(TrackPoint center, TrackPoint radius);

    protected abstract void progressChange(int progress);

    protected abstract void updateView(int index);

    protected abstract void addStartPoint(TrackPoint tPoint);

    protected abstract void addEndPoint(TrackPoint tPoint);
}
