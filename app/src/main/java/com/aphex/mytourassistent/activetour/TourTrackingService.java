package com.aphex.mytourassistent.activetour;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.aphex.mytourassistent.MainActivity;
import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.entities.GeoPointActual;
import com.aphex.mytourassistent.repository.Repository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.util.GeoPoint;

public class TourTrackingService extends LifecycleService {
    Repository repository;

    public TourTrackingService() {
    }

    private Handler handler = new Handler(); //(Looper.getMainLooper());
    private static final int LOCATION_NOTIFICATION_ID = 1010;

    private LocationCallback locationCallback;
    private Location previousLocation=null;
    private NotificationManager notificationManager;
    private String channelId;

    private long tourId;
    private int tourStatus;
    private long travelOrder;


    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        Notification notification = createNotification("0.0 0.0");
        startForeground(LOCATION_NOTIFICATION_ID, notification);
        repository = Repository.getInstance(getApplication());

    }

    private Notification createNotification(String notificationText){
        Intent notificationIntent = new Intent(this, ActiveTourActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        // The PendingIntent to launch our activity if the user selects
        // this notification
        Notification notification =  new Notification.Builder(this, channelId)
                .setContentTitle("Lokasjon")
                .setOnlyAlertOnce(false)
                .setContentText("Din posisjon: " + notificationText)
                .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                .setContentIntent(pendingIntent)
                .setTicker("Tracker din lokasjon ...")
                .build();

        return notification;
    }

    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "my_location_channelid";
        String channelName = "MyLocationService";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Context context = this.getApplicationContext();
        Toast.makeText(context, "Starter service", Toast.LENGTH_SHORT).show();
        Log.d("MY_SERVICE", "onStartCommand(...)");
        tourId = intent.getLongExtra("TOUR_ID", 0L);
        travelOrder = intent.getLongExtra("TRAVEL_ORDER", 0L);
        tourStatus = intent.getIntExtra("TOUR_STATUS", 1);


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                StringBuffer locationBuffer = new StringBuffer();
                for (Location location : locationResult.getLocations()) {
                    // Beregner avstand fra forrisge veipunkt:
                    if (previousLocation == null)
                        previousLocation = location;
                    float distance = previousLocation.distanceTo(location);
                    Log.d("MY-LOCATION-DISTANCE", String.valueOf(distance));
                    Log.d("MY-LOCATION", location.toString());

                    previousLocation = location;

                    locationBuffer.append(location.getLatitude() + ", " + location.getLongitude() + "\n");

                    // Polyline: tegner stien.


                    //before storing it to db
                    //check with the last location object
                    //add the method here to check if distance is more than 5 meters then save it in
                    //db
                    //activeTourViewModel.addGeoPointsActual(gpa);
                    if (distance > 10) {
                        GeoPointActual gpa = new GeoPointActual(location.getLatitude(), location.getLongitude(), tourId, travelOrder++, null);
                        Log.d("MY-LOCATION", "MER ENN 50 METER!!" + distance);
                        repository.addGeoPointsActual(gpa);
                    }

                }

                // Viser/oppdaterer varsel:
                Notification notification = createNotification(locationBuffer.toString());
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(LOCATION_NOTIFICATION_ID, notification);


                //Ev. lagre i Room-database via ViewModel & Repository - klasser.
                // . . .

            }
        };

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        final LocationRequest locationRequest = ActiveTourActivity.createLocationRequest();
        startLocationUpdates(locationRequest);

        //START_STICKY sørger for at onStartCommand() kjører ved omstart (dvs. når service terminerer og starter på nytt ved ressursbehov):
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("MY_SERVICE", "onDestroy(...)");
        this.stopLocationUpdates();
        Intent intent = new Intent(ActiveTourActivity.STRING_ACTION);
        intent.putExtra("TOUR_ID", tourId);
        intent.putExtra("TRAVEL_ORDER", travelOrder);
        intent.putExtra("TOUR_STATUS", tourStatus);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Toast.makeText(this, "Stopper service", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    // Runnable som inneholder metoden som bakgrunnstråden starter:

    // Metoden som kjører i tråden.
    private void backgroundThreadProcessing() {
        //[ ... Tidskrevende kode ... ]
        int res = 0;
        for (int i = 0; i < 10; i++) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        handler.post(doShowResult);

        //Avslutt service når "oppdraget" er ferdig!
        this.stopSelf();
    }

    private Runnable doShowResult = new Runnable() {
        public void run() {
            showResult();
        }
    };

    private void showResult() {
        // Viser en Toast - kan kun fremvises i GUI-tråden:
        Context context = this.getApplicationContext();
        Toast.makeText(context, "Oppdrag fullført!!!!", Toast.LENGTH_SHORT).show();

        //Send beskjed til Activity vha. en broadcast / BroadcastReceiver.
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates(LocationRequest locationRequest) {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */);
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}