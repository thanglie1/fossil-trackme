package com.trackmeapplication.ui.base;

import com.trackmeapplication.ui.home.HomeApplication;
import com.trackmeapplication.mvpFragment.MvpFragment;

public abstract class BaseFragment extends MvpFragment {
    public void updateDatabase() {

    }

    @Override
    protected void onCreate() {
        super.onCreate();
        HomeApplication.getInstance().updateCurrentFragment(this);
    }
}
