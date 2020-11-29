package com.trackmeapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.trackmeapplication.BuildConfig;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferencesData {
    private static final String PREF_FILE  = BuildConfig.APPLICATION_ID.replace(".","_");
    private static SharedPreferences sharedPreferences = null;

    public static void openPref(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_FILE,Context.MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}
