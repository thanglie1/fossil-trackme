package com.trackmeapplication.ui.sharedData;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.GoogleMap;
import com.trackmeapplication.database.RouteRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<ArrayList<RouteRecord>> records = new MutableLiveData<>();

    public SharedViewModel() {
    }

    public LiveData<ArrayList<RouteRecord>> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<RouteRecord> records) {
        Collections.sort(records, new Comparator<RouteRecord>() {
            @Override
            public int compare(RouteRecord o1, RouteRecord o2) {
                if (o1.getID() < o2.getID())
                    return 1;
                if (o1.getID() > o2.getID())
                    return -1;
                return  0;
            }
        });
        this.records.setValue(records);
    }
}
