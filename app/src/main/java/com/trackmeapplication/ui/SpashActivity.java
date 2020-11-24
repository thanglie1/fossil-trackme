package com.trackmeapplication.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.trackmeapplication.ui.home.MainActivity;

public class SpashActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "ui.base.SpashActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "SpashActivity");
        startActivity(intent, savedInstanceState);
    }
}
