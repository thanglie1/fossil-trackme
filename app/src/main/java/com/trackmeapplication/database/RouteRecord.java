package com.trackmeapplication.database;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RouteRecord implements Serializable {
    private long ID;
    private float distance;
    private int duration;
    private float avgSpeed;
    private LatLng startLocation;
    private ArrayList<List<LatLng>> route;

    public RouteRecord() {
        route= new ArrayList<>();
    }

    public RouteRecord(long ID, float distance, int duration, float avgSpeed, LatLng startLocation, ArrayList<List<LatLng>> route) {
        this.ID = ID;
        this.distance = distance;
        this.duration = duration;
        this.avgSpeed = avgSpeed;
        this.startLocation = startLocation;
        this.route = route;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(float avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public ArrayList<List<LatLng>> getRoute() {
        return route;
    }

    public void setRoute(ArrayList<List<LatLng>> route) {
        this.route = route;
    }
}
