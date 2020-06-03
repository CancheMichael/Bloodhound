/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import edu.dartmouth.cs65.searchandrescue.R;
import edu.dartmouth.cs65.searchandrescue.code.fragments.MapsFragment;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class TrackingService extends Service {
    private static final long UPDATE_INTERVAL = 1000;
    private static final int SERVICE_NOTIFICATION_ID = 1;
    public static final String BROADCAST_LOCATION = "new location";
    private NotificationManager notiManager;


    public TrackingService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        //Setup locationrequest
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(UPDATE_INTERVAL);
        locationRequest.setSmallestDisplacement(1);

        //See if locationrequest is reasonable
        getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest,
                mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback()  {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Intent intent = new Intent(BROADCAST_LOCATION);
            intent.putExtra("location", locationResult.getLastLocation());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        createNotification();

        return START_STICKY;

    }


    private void createNotification() {
        Intent notificationIntent = new Intent(this, MapsFragment.class);
        String channelId = "Location Tracker";
        String channelName = "Search and Rescue";
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Setup notification channel
        notiManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_HIGH);
        notiManager.createNotificationChannel(channel);

        //Create notification to return to MapsActivity
        Notification notification = new Notification.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(channelName)
                .setContentText("Currently tracking your location")
                .setOngoing(false)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(SERVICE_NOTIFICATION_ID, notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
