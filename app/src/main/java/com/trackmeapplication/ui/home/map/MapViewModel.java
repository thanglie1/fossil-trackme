package com.trackmeapplication.ui.home.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {
    private final MutableLiveData<Float> distance;
    private final MutableLiveData<Float> currentSpeed;

    public MapViewModel() {
        distance = new MutableLiveData<>();
        distance.setValue((float) 0);
        currentSpeed = new MutableLiveData<>();
        currentSpeed.setValue((float) 0);
    }

    public LiveData<Float> getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance.setValue(distance);
    }

    public LiveData<Float> getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(float currentSpeed) {
        this.currentSpeed.setValue(currentSpeed);
    }
}
