package com.trackmeapplication.ui.home;

import com.trackmeapplication.mvp.MvpPresenter;

public interface IMainPresenter extends MvpPresenter<IMainView> {
    void updateSuccessfullyLocation();
}
