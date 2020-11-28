package com.trackmeapplication.ui.home;

import android.app.Application;

import com.trackmeapplication.ui.base.BaseFragment;

public class HomeApplication extends Application {
    private static HomeApplication instance = null;
    private BaseFragment currentFragment;

    public static HomeApplication getInstance() {
       if (instance == null) {
                instance = new HomeApplication();
            }
        return(instance);
    }

    public synchronized void updateCurrentFragment(BaseFragment fragment) {
        currentFragment = fragment;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public void updateDatabase() {
        currentFragment.updateDatabase();
    }
}