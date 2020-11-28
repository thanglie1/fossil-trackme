package com.trackmeapplication.ui.home.map;

import android.content.Context;

import com.trackmeapplication.database.DatabaseHandler;
import com.trackmeapplication.database.RouteRecord;
import com.trackmeapplication.ui.home.tracks.ITracksView;

public class MapPresenterImpl implements IMapPresenter {
    private IMapView mapView;
    private Context context;

    @Override
    public void onAttach(IMapView mvpFragmentView) {
        mapView = mvpFragmentView;
        context = mvpFragmentView.getContext();
    }

    @Override
    public void updateDatabase() {
    }

    @Override
    public void saveDatabase(RouteRecord record) {
        DatabaseHandler db = new DatabaseHandler(context, null, null, 1);
        db.add(record);
    }
}
