package com.trackmeapplication.ui.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.trackmeapplication.R;
import com.trackmeapplication.mvp.MvpPresenter;
import com.trackmeapplication.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, LocationListener {
    private MapsPresenter presenter = new MapsPresenterImpl();
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; //10*1 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second
    static final int PERMISSION_REQUEST_CODE = 202;
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private GoogleMap mMap;
    private MapViewModel mapViewModel;
    LocationManager locationManager;
    double speed;
    TextView textView;


    @Override
    protected Integer getLayoutId() {
        return null;
    }

    @Override
    protected Integer getSlideMenuId() {
        return null;
    }

    @Override
    protected Integer getBtnMenuId() {
        return null;
    }

    @Override
    protected void initialized() {

    }

    @Override
    protected MvpPresenter getPresenter() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.navigation_home);
//        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
       this.mMap = googleMap;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        requestLastestLocation();
        textView = findViewById(R.id.txt_current_speed);

        this.mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                // Clear previously click position.
                mMap.clear();
                // Add Marker on Map
                mMap.addMarker(markerOptions);
            }
        });
    }

    private void requestLastestLocation() {
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(this)
                    .setMessage("Please turn on GPS to continue")
                    .setPositiveButton("Turn on", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (gps_enabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this::onLocationChanged);
        }
        this.mMap.setMyLocationEnabled(true);

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            LatLng defaultLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 16));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            textView.setText("%@ m/s".replace("%@",String.valueOf(location.getSpeed())));
            speed = location.getSpeed();
        }
    }

    public void checkPermission(String... permissions) {
        // Directly ask for the permission.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            String[] requestPermissions = getRequestPermissions(permissions);
            if (requestPermissions.length > 0){
                ActivityCompat.requestPermissions(this, requestPermissions,PERMISSION_REQUEST_CODE);
            }
        }
        else {
            int[] grantResults = new int[permissions.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(PERMISSION_REQUEST_CODE, permissions, grantResults);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private String[] getRequestPermissions(String[] permissions) {
        List<String> list = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                list.add(permission);
            }
        }
        return list.toArray(new String[list.size()]);
    }
}
