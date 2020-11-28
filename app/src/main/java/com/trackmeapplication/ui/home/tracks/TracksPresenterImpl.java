package com.trackmeapplication.ui.home.tracks;

import android.content.Context;

import com.trackmeapplication.ui.home.IMainView;

public class TracksPresenterImpl implements ITracksPresenter{
    private ITracksView tracksView;
    private Context context;

    @Override
    public void onAttach(ITracksView mvpFragmentView) {
        tracksView = mvpFragmentView;
        context = mvpFragmentView.getContext();
    }
}