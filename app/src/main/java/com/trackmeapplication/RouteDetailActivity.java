package com.trackmeapplication;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

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
    private View fragmentRecord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String json = (String)intent.getSerializableExtra("RouteRecord");
        record = new Gson().fromJson(json, RouteRecord.class);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_google_map);
        mapFragment.getMapAsync(this);

        fragmentRecord = (View)findViewById(R.id.fragment_record_detail);
        fragmentRecord.setVisibility(View.VISIBLE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView txtAvgStrSpeed = (TextView) findViewById(R.id.txt_str_current_speed);
        txtAvgStrSpeed.setText(getString(R.string.avg_speed));
        TextView txtAvgSpeed = (TextView) findViewById(R.id.txt_current_speed);
        txtAvgSpeed.setText(("%1 m/s").replace("%1",String.format("%.02f",record.getAvgSpeed())));
        TextView txtDistance = (TextView) findViewById(R.id.txt_distance);
        txtDistance.setText(("%1 m").replace("%1",String.format("%.02f",record.getDistance())));
        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer_duration);
        chronometer.setFormat("%s s");
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.setBase(SystemClock.elapsedRealtime() - record.getDuration()*1000);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
