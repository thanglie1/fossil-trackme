package com.trackmeapplication.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.trackmeapplication.ui.main.MainActivity;
import com.trackmeapplication.R;

public class LocationService extends Service implements LocationListener {
    public static final String CHANNEL_ID = "TrackMeChannel";
    public static final int ONGOING_NOTIFICATION_ID = 1111;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; //10*1 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second
    private LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = this;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TrackMe")
                .setContentText(input)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_main    )
                .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);

        registerRequestLocationUpdates();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public void registerRequestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this::onLocationChanged);
    }

//    @Override
//    protected void onPostExecute(String strJson) {
//        super.onPostExecute(strJson);
//        Log.d(LOG_TAG, strJson + " " + url);
//        exercises = ParseJSON.ChallengeParseJSON(strJson);
//        Log.d("Challenges", "challenges: " + exercises.get(0).getName() + " " + exercises.get(1).getName());
//
//        Intent intent = new Intent(FILTER); //FILTER is a string to identify this intent
//        intent.putExtra(MY_KEY, exercises);
//        sendBroadcast(intent);
//    }
}
