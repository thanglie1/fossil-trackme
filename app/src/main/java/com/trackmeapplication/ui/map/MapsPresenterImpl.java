package com.trackmeapplication.ui.map;

import android.content.Context;

import com.trackmeapplication.ui.home.IMainView;

public class MapsPresenterImpl implements MapsPresenter{
    private IMapsView mapsView;
    private Context context;
    @Override
    public void onAttach(IMapsView mvpView) {
        mapsView = mvpView;
        context = mvpView.getContext();
    }

    @Override
    public void onDetach() {

    }
}
