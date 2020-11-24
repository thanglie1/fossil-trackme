package com.trackmeapplication.ui.home;

import android.content.Context;

public class MainPresenterImpl implements IMainPresenter {
    private IMainView mainView;
    private Context context;
    @Override
    public void onAttach(IMainView mvpView) {
        this.mainView = mvpView;
        this.context = mvpView.getContext();
    }

    @Override
    public void onDetach() {

    }
}
