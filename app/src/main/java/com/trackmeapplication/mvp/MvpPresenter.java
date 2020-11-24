package com.trackmeapplication.mvp;

public interface MvpPresenter <V extends MvpView> {
    void onAttach(V mvpView);

    void onDetach();
}
