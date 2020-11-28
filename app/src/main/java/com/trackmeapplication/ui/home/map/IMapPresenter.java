package com.trackmeapplication.ui.home.map;

import com.trackmeapplication.database.RouteRecord;
import com.trackmeapplication.mvpFragment.MvpFragmentPresenter;

public interface IMapPresenter extends MvpFragmentPresenter<IMapView> {
    void updateDatabase();
    void saveDatabase(RouteRecord record);
}
