package com.trackmeapplication.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trackmeapplication.R;
import com.trackmeapplication.mvp.MvpPresenter;
import com.trackmeapplication.ui.base.BaseActivity;
import com.trackmeapplication.ui.map.MapFragment;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity implements IMainView {
    private IMainPresenter presenter = new MainPresenterImpl();

    static final int PERMISSION_REQUEST_CODE = 202;
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected Integer getLayoutId() {
        return R.layout.activity_main;
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
        //create
        checkPermission(PERMISSIONS);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder( R.id.navigation_tracks).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

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
            if (requestPermissions.length > 0){
                requestPermissions(requestPermissions, PERMISSION_REQUEST_CODE);
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