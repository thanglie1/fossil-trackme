package com.trackmeapplication.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trackmeapplication.R;
import com.trackmeapplication.mvp.MvpPresenter;
import com.trackmeapplication.ui.base.BaseActivity;
import com.trackmeapplication.ui.home.map.MapFragment;
import com.trackmeapplication.ui.home.tracks.TracksFragment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity implements IMainView {
    private IMainPresenter presenter = new MainPresenterImpl();

    private FragmentManager fm = getSupportFragmentManager();
    final Fragment mapFragment = new MapFragment();
    final Fragment tracksFragment = new TracksFragment();
    Fragment active = mapFragment;

    static final int PERMISSION_REQUEST_CODE = 202;
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public MainActivity() {
    }

    @Override
    protected Integer getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initialized() {
        //creat
        checkPermission(PERMISSIONS);
        BottomNavigationView bottomNav = findViewById(R.id.nav_view);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        fm.beginTransaction().add(R.id.fragment_container, tracksFragment, "1").hide(tracksFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, mapFragment, "0").commit();
    }

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
                            return true;
                    }
                    return true;
                }
            };

    @Override
    protected MvpPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}