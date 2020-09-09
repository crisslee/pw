package com.coomix.app.all.manager;

import android.app.Activity;
import android.util.SparseArray;

import com.coomix.app.all.manager.ActivityStateManager.ActivityState.State;

public final class ActivityStateManager {

    private final static SparseArray<ActivityState> mManagerActivity = new SparseArray<ActivityStateManager.ActivityState>();

    public synchronized static void onCreate(Activity activity) {
        ActivityState state = mManagerActivity.get(activity.hashCode());

        if (state == null) {
            state = new ActivityState();
            state.mActivity = activity;
            state.mHashCode = activity.hashCode();
            state.mStatus = State.ACTIVE;
            mManagerActivity.put(activity.hashCode(), state);
        } else {
            state.mStatus = State.ACTIVE;
        }
    }

    public synchronized static void onResume(Activity activity) {
        ActivityState state = mManagerActivity.get(activity.hashCode());
        if (state != null) {
            state.mStatus = State.ACTIVE;
        }
    }

    public synchronized static void onStop(Activity activity) {
        ActivityState state = mManagerActivity.get(activity.hashCode());
        if (state != null) {
            state.mStatus = ActivityState.State.STOP;
        }
    }

    public synchronized static void onDestroy(Activity activity) {
        mManagerActivity.remove(activity.hashCode());
    }

    public synchronized static State getActivityState(Activity activity) {
        return getActivityState(activity.hashCode());
    }

    /**
     * @param hashcode the hashcode of the queried activity
     * @return
     */
    public synchronized static State getActivityState(int hashcode) {
        ActivityState state = mManagerActivity.get(hashcode);
        if (state != null) {
            return state.mStatus;
        } else {
            return State.REMOVE;
        }
    }

    public synchronized static void finishAll() {
        int size = mManagerActivity.size();
        for (int i = 0; i < size; i++) {
            mManagerActivity.valueAt(i).mActivity.finish();
        }
        DeviceManager.getInstance().clearData();

        mManagerActivity.clear();
    }

    public synchronized static boolean isHomeActivityExist() {
        if (mManagerActivity == null) {
            return false;
        }
        int size = mManagerActivity.size();
        if (size <= 0) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            ActivityState state = mManagerActivity.valueAt(i);
            if (state.mActivity instanceof MainActivityParent) {
                return true;
            }
        }
        return false;
    }

    public static class ActivityState {

        public enum State {
            ACTIVE, STOP, DESTROY, REMOVE;
        }

        public int mHashCode;
        public State mStatus;
        public Activity mActivity;

        @Override
        public String toString() {
            return "ActivityState{" +
                    "mHashCode=" + mHashCode +
                    ", mStatus=" + mStatus +
                    ", mActivity=" + mActivity +
                    '}';
        }
    }
}
