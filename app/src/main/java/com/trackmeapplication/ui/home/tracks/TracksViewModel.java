package com.trackmeapplication.ui.home.tracks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TracksViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TracksViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Tracks");
    }

    public LiveData<String> getText() {
        return mText;
    }
}