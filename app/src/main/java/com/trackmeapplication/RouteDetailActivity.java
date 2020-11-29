package com.trackmeapplication;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.trackmeapplication.database.RouteRecord;

import java.util.ArrayList;
import java.util.List;

public class RouteDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private RouteRecord record;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String json = (String)intent.getSerializableExtra("RouteRecord");
        record = new Gson().fromJson(json, RouteRecord.class);
        Button back = findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_google_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        for (List<LatLng> aPolyline : record.getRoute()) {
            LatLng[] arr = new LatLng[aPolyline.size()];
            arr = aPolyline.toArray(arr);
            Polyline polyline = googleMap.addPolyline(new PolylineOptions().clickable(true)
                    .color(getResources().getColor(R.color.red_50))
                    .add(arr));
        }
        LatLng latLngStart = record.getStartLocation();
        moveCamera(latLngStart,20);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLngStart);
        markerOptions.title(latLngStart.latitude + " : " + latLngStart.longitude);
        googleMap.addMarker(markerOptions);
    }

    public void moveCamera(LatLng latLng, float zoom) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        if (zoom >= 0)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
}
