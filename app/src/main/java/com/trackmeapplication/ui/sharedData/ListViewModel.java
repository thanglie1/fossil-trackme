package com.trackmeapplication.ui.sharedData;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trackmeapplication.database.RouteRecord;

import java.util.ArrayList;

public class ListViewModel extends ViewModel {
    private MutableLiveData<ArrayList<RouteRecord>> records = new MutableLiveData<>();

    public ListViewModel() {
    }

    public LiveData<ArrayList<RouteRecord>> getRecords() {
        return records;
    }
    public void setRecords(ArrayList<RouteRecord> records) {
        this.records.setValue(records);
    }
}
