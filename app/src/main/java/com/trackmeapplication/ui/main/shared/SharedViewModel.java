package com.trackmeapplication.ui.main.shared;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trackmeapplication.database.RouteRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<Boolean> isRunning = new MutableLiveData<>();
    private MutableLiveData<Boolean> isRecording = new MutableLiveData<>();
    private MutableLiveData<ArrayList<RouteRecord>> records = new MutableLiveData<>();

    public SharedViewModel() {
        isRunning.setValue(false);
        isRecording.setValue(false);
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

    public LiveData<Boolean> isRunning() { return isRunning;}

    public void setIsRunning(boolean value) { this.isRunning.setValue(value);}

    public LiveData<Boolean> isRecording() { return isRecording;}

    public void setIsRecording(boolean value) { this.isRecording.setValue(value);}
}
