package com.coomix.app.all.service;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MyOrientationListener implements SensorEventListener{

    private Context context;
    private SensorManager sensorManager;
    private Sensor sensor;

    private float lastX;

    private OnOrientationListener onOrientationListener;

    public MyOrientationListener(Context context)
    {
        this.context = context;
    }

    //onStart
    public void start()
    {
        //获取方向传感器
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null)
        {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        if(sensor != null)
        {
            sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_UI);
        }
    }

    //onStop
    public void stop()
    {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION)
        {
            float x = event.values[SensorManager.DATA_X];

            if(Math.abs(x-lastX)>1.0){
                onOrientationListener.onOrientationChanged(x);
            }

            lastX = x;
        }
    }

    public void setOnOrientationListener(OnOrientationListener onOrientationListener)
    {
        this.onOrientationListener = onOrientationListener;
    }

    public interface OnOrientationListener
    {
        void onOrientationChanged(float x);
    }
}
