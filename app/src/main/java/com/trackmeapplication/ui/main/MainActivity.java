package com.trackmeapplication.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trackmeapplication.R;
import com.trackmeapplication.service.LocationService;
import com.trackmeapplication.ui.main.map.MapFragment;
import com.trackmeapplication.ui.main.tracks.TracksFragment;
import com.trackmeapplication.ui.main.shared.SharedViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SharedViewModel sharedViewModel;
    private FragmentManager fm = getSupportFragmentManager();
    private final Fragment mapFragment = new MapFragment();
    private final Fragment tracksFragment = new TracksFragment();
    Fragment active = mapFragment;

    static final int PERMISSION_REQUEST_CODE = 202;
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_map:
                            fm.beginTransaction().hide(active).show(mapFragment).commit();
                            active = mapFragment;
                            return true;

                        case R.id.nav_tracks:
                            fm.beginTransaction().hide(active).show(tracksFragment).commit();
                            active = tracksFragment;
                            break;
                    }
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission(PERMISSIONS);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        BottomNavigationView bottomNav = (BottomNavigationView) findViewById(R.id.nav_view);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        fm.beginTransaction().add(R.id.fragment_container, tracksFragment, "1").hide(tracksFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, mapFragment, "0").commit();
    }

    public void checkPermission(String... permissions) {
        // Directly ask for the permission.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            String[] requestPermissions = getRequestPermissions(permissions);
            if (requestPermissions.length > 0) {
                requestPermissions(requestPermissions, PERMISSION_REQUEST_CODE);
            }
        } else {
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

    public void startService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra("inputExtra", "Location...");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent);
        }
        else startService(serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onStop() {
        if (!sharedViewModel.isRecording().getValue()) {
            stopService();
        }
        else {
            sharedViewModel.setIsRecording(true);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        startService();
        super.onResume();
    }
}