package com.trackmeapplication.ui.home.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

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
import com.trackmeapplication.R;
import com.trackmeapplication.database.DatabaseHandler;
import com.trackmeapplication.database.RouteRecord;
import com.trackmeapplication.ui.base.BaseFragment;
import com.trackmeapplication.mvpFragment.MvpFragmentPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapFragment extends BaseFragment implements IMapView, OnMapReadyCallback, LocationListener {
    private IMapPresenter presenter = new MapPresenterImpl();
    private GoogleMap mMap;
    LocationManager locationManager;
    Location lastLocation;
    Location startLocation;
    ArrayList<Polyline> polylineList = new ArrayList<Polyline>();

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; //10*1 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second

    float distance;
    float currentSpeed;
    int duration;

    TextView txtCurrentSpeed;
    TextView txtDistance;
    Timer reloadLocation;
    View fragmentRecord;
    View fragmentPauseStop;

    Chronometer chronometer;
    boolean isRunning;
    long pauseOffSet;

    @Override
    protected MvpFragmentPresenter getPresenter() {
        return presenter;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate();
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_google_map);
        mapFragment.getMapAsync(this);

        txtCurrentSpeed = root.findViewById(R.id.txt_current_speed);
        txtDistance = root.findViewById(R.id.txt_distance);
        fragmentRecord = root.findViewById(R.id.fragment_record);
        fragmentPauseStop = root.findViewById(R.id.view_pause_stop);

        ImageButton btnRecord = (ImageButton) root.findViewById(R.id.img_button_record);
        ImageButton btnStop = (ImageButton) root.findViewById(R.id.img_button_stop);
        ImageButton btnPause = (ImageButton) root.findViewById(R.id.img_button_pause);
        ImageButton btnResume = (ImageButton) root.findViewById(R.id.img_button_resume);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRecord.setVisibility(View.GONE);
                fragmentRecord.setVisibility(View.VISIBLE);
                fragmentPauseStop.setVisibility(View.VISIBLE);
                handleStartAction();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRecord.setVisibility(View.VISIBLE);
                fragmentRecord.setVisibility(View.GONE);
                fragmentPauseStop.setVisibility(View.GONE);
                handleStopAction();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.VISIBLE);
                pauseChronometer();
            }
        });

        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnResume.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                startChronometer();
            }
        });

        chronometer = root.findViewById(R.id.chronometer_duration);
        chronometer.setFormat("%s s");
        chronometer.setBase(SystemClock.elapsedRealtime());

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        requestLatestLocation();

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

    private void handleStartAction() {
        //Register location Update
        registerRequestLocationUpdates();

        //Start chronometer
        startChronometer();
    }

    private void handleStopAction() {
        saveDatabase();
        //Reset chronometer
        resetChronometer();

        //Remove polyline
        for (Polyline polyline : polylineList) {
            polyline.remove();
        }
    }

    private void saveDatabase() {
        DatabaseHandler handler = new DatabaseHandler(getActivity(), null, null, 1);
        if (startLocation == null)
            return;
        ArrayList<List<LatLng>> routeList = new ArrayList<List<LatLng>>();
        for (Polyline polyline: polylineList) {
            routeList.add(polyline.getPoints());
        }
        RouteRecord record = new RouteRecord(System.currentTimeMillis()/1000, distance, duration, distance /duration, new LatLng(startLocation.getLatitude(), startLocation.getLongitude()), routeList);
        presenter.saveDatabase(record);
    }

    private void startChronometer() {
        if (!isRunning) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffSet);
            chronometer.start();
            isRunning = true;
        }
    }

    private void pauseChronometer() {
        if (isRunning) {
            chronometer.stop();
            pauseOffSet = SystemClock.elapsedRealtime() - chronometer.getBase();
            isRunning = false;
        }
    }

    private void resetChronometer() {
        isRunning = false;
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffSet = 0;
    }

    @Override
    public void onLocationChanged(Location location) {
        currentSpeed = location.getSpeed();
        txtCurrentSpeed.setText(("%1 m/s").replace("%1" ,String.format("%.02f", currentSpeed)));

        if (isRunning) {
            moveCamera(location,-1);
            distance += location.distanceTo(lastLocation);
            txtDistance.setText(("%1 m").replace("%1" ,String.format("%.02f", distance)));
            Polyline polyline = mMap.addPolyline(new PolylineOptions().clickable(true)
                    .color(getResources().getColor(R.color.red_50))
                    .add(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                            new LatLng(location.getLatitude(), location.getLongitude())));
            polylineList.add(polyline);
        }
        lastLocation = location;
    }

    public void onUpdateLocation() {
        reloadLocation.cancel();
        reloadLocation.purge();
    }

    private void requestLatestLocation() {
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

        this.mMap.setMyLocationEnabled(true);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                updateCurrentLocation();
            }
        };

        reloadLocation = new Timer();
        reloadLocation.schedule(timerTask,  2000, 2000);
    }

    public void updateCurrentLocation() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            moveCamera(location, 16);
                            onUpdateLocation();
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

    public void moveCamera(Location location, float zoom) {
        LatLng defaultLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));
        if (zoom >= 0)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, zoom));
    }

    public void registerRequestLocationUpdates() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            //Mark start point
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                            // Clear previously click position.
                            mMap.clear();
                            // Add Marker on Map
                            mMap.addMarker(markerOptions);
                            startLocation = location;
                            lastLocation = location;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this::onLocationChanged);
    }
}