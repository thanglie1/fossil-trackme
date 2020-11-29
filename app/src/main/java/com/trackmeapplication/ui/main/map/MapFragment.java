package com.trackmeapplication.ui.main.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import com.trackmeapplication.service.LocationService;
import com.trackmeapplication.ui.main.shared.SharedViewModel;
import com.trackmeapplication.utils.SharedPreferencesData;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapFragment extends Fragment implements OnMapReadyCallback, LocationListener {
    private SharedViewModel sharedViewModel;
    private GoogleMap mMap;
    private MapViewModel mapViewModel;
    private LocationManager locationManager;
    private Location lastLocation;
    private Location startLocation;
    private ArrayList<Polyline> polylineList = new ArrayList<Polyline>();

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; //10*1 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second


    private TextView txtCurrentSpeed;
    private TextView txtDistance;
    private Timer reloadLocation;
    private View fragmentRecord;
    private View fragmentPauseStop;

    private Chronometer chronometer;
    private long pauseOffSet;

    public MapFragment() {
    }

    public void loadInternalData() {
        SharedPreferencesData.openPref(getActivity());
        SharedPreferences pref = SharedPreferencesData.getSharedPreferences();
        mapViewModel.setDistance(pref.getFloat("distance", 0));
        mapViewModel.setCurrentSpeed(pref.getFloat("currentSpeed", 0));
        int duration = pref.getInt("duration", 0);
        chronometer.setBase(SystemClock.elapsedRealtime() - duration* 1000);
        chronometer.start();
        if (duration > 0){
            sharedViewModel.setIsRecording(true);
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
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
                sharedViewModel.setIsRecording(true);
                btnRecord.setVisibility(View.GONE);
                fragmentRecord.setVisibility(View.VISIBLE);
                fragmentPauseStop.setVisibility(View.VISIBLE);
                btnResume.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
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
                sharedViewModel.setIsRecording(false);
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

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        mapViewModel.getCurrentSpeed().observe(getViewLifecycleOwner(), new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                txtCurrentSpeed.setText(("%1 m/s").replace("%1", String.format("%.02f", aFloat)));
            }
        });

        mapViewModel.getDistance().observe(getViewLifecycleOwner(), new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                txtDistance.setText(("%1 m").replace("%1", String.format("%.02f", aFloat)));
            }
        });

        sharedViewModel.isRecording().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    btnRecord.setVisibility(View.GONE);
                    fragmentRecord.setVisibility(View.VISIBLE);
                    fragmentPauseStop.setVisibility(View.VISIBLE);
                    btnResume.setVisibility(View.GONE);
                    btnPause.setVisibility(View.VISIBLE);
                }
                else {
                    btnRecord.setVisibility(View.VISIBLE);
                    fragmentRecord.setVisibility(View.GONE);
                    fragmentPauseStop.setVisibility(View.GONE);
                }
            }
        });
        loadInternalData();
        return root;
    }

    @Override
    public void onStop() {
        if (sharedViewModel.isRecording().getValue()) {
            SharedPreferences.Editor pref = SharedPreferencesData.getSharedPreferences().edit();
            pref.putFloat("distance", mapViewModel.getDistance().getValue());
            pref.putFloat("currentSpeed", mapViewModel.getCurrentSpeed().getValue());
            int elapsedSeconds = sharedViewModel.isRunning().getValue() ? (int) (SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 : (int) pauseOffSet / 1000;
            pref.putInt("duration", elapsedSeconds);
            pref.commit();
        }
        else {
            SharedPreferences.Editor pref = SharedPreferencesData.getSharedPreferences().edit();
            pref.clear();
            pref.commit();
        }
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        requestLatestLocation();
    }

    private void handleStartAction() {
        startChronometer();
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

        if (gps_enabled && network_enabled) {
            registerRequestLocationUpdates();
        }
    }

    private void handleStopAction() {
        saveDatabase();
        //Reset chronometer
        resetChronometer();

        //Remove polyline
        for (Polyline polyline : polylineList) {
            polyline.remove();
        }
        mapViewModel.setCurrentSpeed(0);
        mapViewModel.setDistance(0);
    }

    private void saveDatabase() {
        DatabaseHandler handler = new DatabaseHandler(getActivity(), null, null, 1);
        if (startLocation == null)
            return;
        ArrayList<List<LatLng>> routeList = new ArrayList<List<LatLng>>();
        for (Polyline polyline : polylineList) {
            routeList.add(polyline.getPoints());
        }
        int elapsedSeconds = sharedViewModel.isRunning().getValue() ? (int) (SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 : (int) pauseOffSet / 1000;
        RouteRecord record = new RouteRecord(System.currentTimeMillis(), mapViewModel.getDistance().getValue(), elapsedSeconds, elapsedSeconds == 0 ? 0 : (mapViewModel.getDistance().getValue() / elapsedSeconds), new LatLng(startLocation.getLatitude(), startLocation.getLongitude()), routeList);
        handler.add(record);

        //Update data
        ArrayList<RouteRecord> data = handler.getAll();
        sharedViewModel.setRecords(data);
    }

    private void startChronometer() {
        if (!sharedViewModel.isRunning().getValue()) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffSet);
            chronometer.start();
            sharedViewModel.setIsRunning(true);
        }
    }

    private void pauseChronometer() {
        if (sharedViewModel.isRunning().getValue()) {
            chronometer.stop();
            pauseOffSet = SystemClock.elapsedRealtime() - chronometer.getBase();
            sharedViewModel.setIsRunning(false);
        }
    }

    private void resetChronometer() {
        sharedViewModel.setIsRunning(false);
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffSet = 0;
    }

    @Override
    public void onLocationChanged(Location location) {
        mapViewModel.setCurrentSpeed(location.getSpeed());

        if (sharedViewModel.isRunning().getValue()) {
            moveCamera(location, -1);
            mapViewModel.setDistance(mapViewModel.getDistance().getValue() + location.distanceTo(lastLocation));
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

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                updateCurrentLocation();
            }
        };

        reloadLocation = new Timer();
        reloadLocation.schedule(timerTask, 2000, 2000);
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

                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
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