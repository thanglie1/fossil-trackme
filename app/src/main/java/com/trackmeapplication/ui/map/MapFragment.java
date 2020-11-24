package com.trackmeapplication.ui.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;
import com.trackmeapplication.R;
import com.trackmeapplication.service.StopWatchService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback, LocationListener{
    public static final int PERMISSION_REQUEST_CODE = 202;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; //10*1 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second

    private GoogleMap mMap;
    private MapViewModel mapViewModel;
    LocationManager locationManager;
    double speed;
    private boolean mLocationPermissionGranted;
    ArrayList<LatLng> list = new ArrayList<LatLng>();

    StopWatchService stopWatchService;
    TextView textView;
    TextView txtViewDuration;
    ImageButton recordButton;
    ImageButton stopButton;
    Intent startWatchIntent;
    Intent stopWatchIntent;
    boolean mBound = false;

    Calendar c;
    String formattedDate = "";

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public MapFragment() {
        getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap gmap) {
        this.mMap = gmap;

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        requestLastestLocation();
        textView = getActivity().findViewById(R.id.txt_current_speed);
        txtViewDuration = getActivity().findViewById(R.id.txt_duration);
        recordButton =getActivity().findViewById(R.id.img_button_record);
        stopButton =getActivity().findViewById(R.id.img_button_stop);

        startWatchIntent = new Intent(getActivity(), StopWatchService.class);
        stopWatchIntent = new Intent(getActivity(), StopWatchService.class);
        c = Calendar.getInstance();

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

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(startWatchIntent);
                getActivity().bindService(startWatchIntent, mConnection, Context.BIND_AUTO_CREATE);
                // when the walk has started, take note of the current time.
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
                formattedDate = df.format(c.getTime());

                getPermissionAndLocationChange(list);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                double computedDistance = getDistance();
                long elapsedTime = stopWatchService.getElapsedTime();

                //on the new child node, create these 4 'fields' and insert into the database
//                exampleRun.child("time").setValue(formattedDate);
//                exampleRun.child("distance").setValue(computedDistance);
//                exampleRun.child("arrOfLatLng").setValue(list);
//                exampleRun.child("duration").setValue(elapsedTime);

                getActivity().stopService(stopWatchIntent);
                getActivity().unbindService(mConnection);
                mBound = false;

//                Intent intentToFinish = new Intent(getApplicationContext(),FinishActivity.class);
//                intentToFinish.putExtra(INTENT_DISTANCEKEY,computedDistance);
//                intentToFinish.putExtra(INTENT_TIMEKEY,elapsedTime);
//                startActivity(intentToFinish);
            }
        });

//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Intent intentToList = new Intent(getApplicationContext(),ListOfWalks.class);
//                startActivity(intentToList);
//
//            }
//
//        });

        /**
         * Every one second: display the time that has passed since the walk has started.
         */
//        Thread t = new Thread() {
//
//            @Override
//            public void run() {
//                try {
//                    while (!isInterrupted()) {
//                        Thread.sleep(1000);
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (mBound) {
//                                    long elapsedTime = stopWatchService.getElapsedTime();
//                                    String formattedTime = DateUtils.formatElapsedTime(elapsedTime);
//                                    txtViewDuration.setText(formattedTime);
//                                }
//                            }
//                        });
//                    }
//                } catch (InterruptedException e) {
//                }
//            }
//        };
//
//        t.start();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * Check permission then start listening for location changes.
     * When location change, add the latlng in an arraylist.
     * @param tList an arraylist which is used to hold all the latlng of the entire walk
     */
    public void getPermissionAndLocationChange(final ArrayList<LatLng> tList){
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }

        if (mLocationPermissionGranted) {
            //minimum time interval between location updates, in milliseconds. in here, is every 10 seconds.
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    tList.add(new LatLng(location.getLatitude(), location.getLongitude()));

                    Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(tList)
                    .width(5)
                    .color(Color.RED));
                }
            });
        }

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            StopWatchService.LocalBinder binder = (StopWatchService.LocalBinder) service;
            stopWatchService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /**
     *Loop through the arrayList of latlng
     * and compute the distance between each latlng
     *
     * @return  total distance covered in meters
     */
    private double getDistance() {

        double totalDistance = 0;

        for (int i = 0; i < list.size() - 1; i++) {
            totalDistance = totalDistance + SphericalUtil.computeDistanceBetween(list.get(i), list.get(i + 1));
        }

        return totalDistance;

    }

    public void requestLastestLocation() {
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
            new AlertDialog.Builder(getActivity())
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
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (gps_enabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this::onLocationChanged);
        }
        this.mMap.setMyLocationEnabled(true);

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
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
    public void onResume() {
        super.onResume();
        Toast.makeText(getActivity(), "Reume", Toast.LENGTH_LONG);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {

            textView.setText("%@ m/s".replace("%@",String.valueOf(location.getSpeed())));
            speed = location.getSpeed();
        }
    }
}
