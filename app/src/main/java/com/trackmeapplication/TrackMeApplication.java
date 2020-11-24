package com.trackmeapplication;

import android.app.Application;

import com.trackmeapplication.ui.base.BaseActivity;

public class TrackMeApplication extends Application {
    private static TrackMeApplication instance = null;
    private BaseActivity currentActivity;

    public static TrackMeApplication getInstance() {
        if (instance == null) {
            instance = new TrackMeApplication();
        }
        return(instance);
    }
    public synchronized void updateCurrentActivity(BaseActivity activity) {
        currentActivity = activity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
