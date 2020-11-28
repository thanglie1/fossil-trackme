package com.trackmeapplication.mvpFragment;

public interface MvpFragmentPresenter  <V extends MvpFragmentView> {
    void onAttach(V mvpFragmentView);
}
