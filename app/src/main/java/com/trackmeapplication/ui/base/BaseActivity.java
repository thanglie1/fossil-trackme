package com.trackmeapplication.ui.base;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import com.trackmeapplication.TrackMeApplication;
import com.trackmeapplication.mvp.MvpActivity;
import com.trackmeapplication.mvp.MvpPresenter;

public abstract class BaseActivity extends MvpActivity {
    @LayoutRes
    protected abstract Integer getLayoutId();

    @IdRes
    protected abstract Integer getSlideMenuId();

    @IdRes
    protected abstract Integer getBtnMenuId();

    abstract protected void initialized();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        TrackMeApplication.getInstance().updateCurrentActivity(this);
        initialized();
    }
}
