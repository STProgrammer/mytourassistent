package com.aphex.mytourassistent.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.repository.db.entities.GeoPointActual;
import com.aphex.mytourassistent.repository.db.entities.Tour;
import com.aphex.mytourassistent.repository.db.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.enums.TourStatus;
import com.aphex.mytourassistent.repository.Repository;
import com.aphex.mytourassistent.views.activities.ActiveTourActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class TourTrackingService extends LifecycleService {
    private final String ACTION_STOP_RECORDING = "ACTION_STOP_RECORDING";
    Repository repository;
    private final String ACTION_PAUSE_RECORDING = "ACTION_PAUSE_RECORDING";
    private SharedPreferences prefs;

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
        //get thevalues from intent

        repository = Repository.getInstance(getApplication());

        prefs =  PreferenceManager.getDefaultSharedPreferences(this);  //Denne bruker getSharedPreferences(... , ...). Tilgjengelig fra alle aktiviteter.

    }

    private Notification createNotification(String notificationText){
        Intent notificationIntent = new Intent(this, ActiveTourActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        // The PendingIntent to launch our activity if the user selects
        // this notification

        Intent pauseIntent = new Intent(this, PauseRecordingBroadcastReceiver.class);
        pauseIntent.setAction(ACTION_PAUSE_RECORDING);
        pauseIntent.putExtra(EXTRA_NOTIFICATION_ID, LOCATION_NOTIFICATION_ID);
        pauseIntent.putExtra("TOUR_ID", tourId);
        pauseIntent.putExtra("TOUR_STATUS", tourStatus);
        PendingIntent pausePendingIntent =
                PendingIntent.getBroadcast(this, 0, pauseIntent, 0);

        Intent stopIntent = new Intent(this, StopRecordingBroadcastReceiver.class);
        stopIntent.setAction(ACTION_STOP_RECORDING);
        stopIntent.putExtra(EXTRA_NOTIFICATION_ID, LOCATION_NOTIFICATION_ID);
        stopIntent.putExtra("TOUR_ID", tourId);
        stopIntent.putExtra("TOUR_STATUS", tourStatus);
        PendingIntent stopPendingIntent =
                PendingIntent.getBroadcast(this, 0, stopIntent, 0);
        Log.d("Debug",tourId+"");

        NotificationCompat.Action actionPause = new NotificationCompat.Action.Builder(R.drawable.ic_baseline_pause_24, getString(R.string.notification_pause), pausePendingIntent).build();
        NotificationCompat.Action actionStop = new NotificationCompat.Action.Builder(R.drawable.ic_baseline_stop_24, getString(R.string.notification_stop), stopPendingIntent).build();


        Notification notification =  new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Lokasjon")
                .setOnlyAlertOnce(false)
                .setContentText("Din posisjon: " + notificationText)
                .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                .setContentIntent(pendingIntent)
                .setTicker("Tracker din lokasjon ...")
                .addAction(actionPause)
                .addAction(actionStop)
                .build();

        
        //TODO play pause and stop button


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

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        Notification notification = createNotification("0.0 0.0");
        startForeground(LOCATION_NOTIFICATION_ID, notification);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                //lets check our tour settings-> check and then value
                if(prefs.getBoolean("auto_cancellation_checkbox", false)){
                    //now check nr of hours
                    Log.d("DebugTime","checking time");
                    int nrOfHoursLimit = Integer.valueOf(prefs.getString("nr_of_hours", "1"));
                    TourWithAllGeoPoints twagp = repository.getTourWithAllGeoPoints(tourId, false).getValue();

                    long difference = (new Date().getTime() - twagp.tour.startTimeOfTour)/(1000*60*60);
                    //get the number of hours from this difference
                    if (nrOfHoursLimit < difference) {
                        Log.d("DebugTime","auto pause");
                        Intent intent = new Intent(context,PauseRecordingBroadcastReceiver.class);
                        intent.setAction(ACTION_PAUSE_RECORDING);
                        sendBroadcast(intent);
                        return;
                    }



                }else{
                    Log.d("DebugTime","not checking time");
                }

                //do nothing
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
                        GeoPointActual gpa = new GeoPointActual(location.getLatitude(), location.getLongitude(), tourId, travelOrder++);
                        Log.d("MY-LOCATION", "MER ENN 10 METER!!" + distance);
                        repository.addGeoPointsActual(gpa);
                    }

                }

                // Viser/oppdaterer varsel:
                //TODO: update notification instead of new notification every time
                Notification notification = createNotification(locationBuffer.toString());
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(LOCATION_NOTIFICATION_ID, notification);
            }
        };

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        final LocationRequest locationRequest = createLocationRequest();

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

    // LocationRequest: Setter krav til posisjoneringa:
    public static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        // Hvor ofte ønskes lokasjonsoppdateringer (her: hvert 10.sekund)
        locationRequest.setInterval(5000);
        // Her settes intervallet for hvor raskt appen kan håndtere oppdateringer.
        locationRequest.setFastestInterval(3000);
        // Ulike verderi; Her: høyest mulig nøyaktighet som også normalt betyr bruk av GPS.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
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

    public static class PauseRecordingBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            context.stopService(new Intent(context, TourTrackingService.class));
            Repository repository = Repository.getInstance(context);
            long tourId = intent.getLongExtra("TOUR_ID", 0L);
            TourWithAllGeoPoints twagp = repository.getTourWithAllGeoPoints(tourId, false).getValue();
            Tour tour = twagp.tour;
            tour.tourStatus = TourStatus.PAUSED.getValue();
            repository.updateTour(tour);
            Toast.makeText(context, R.string.status_paused, Toast.LENGTH_SHORT).show();
        }
    }

    public static class StopRecordingBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            //get the current tour
            Repository repository = Repository.getInstance(context);
            long tourId = intent.getLongExtra("TOUR_ID", 0L);
            TourWithAllGeoPoints twagp = repository.getTourWithAllGeoPoints(tourId, false).getValue();
            int status = twagp.tour.tourStatus;
            Tour tour = twagp.tour;
            Log.d("Debug","On Stop "+tourId);
            if (status == TourStatus.ACTIVE.getValue() ||
                    status == TourStatus.PAUSED.getValue()) {
                tour.finishTimeActual = new Date().getTime();
                tour.tourStatus = TourStatus.COMPLETED.getValue();
                repository.updateTour(tour);
                context.stopService(new Intent(context, TourTrackingService.class));
                Toast.makeText(context, R.string.status_completed, Toast.LENGTH_SHORT).show();
            }
        }
    }



}


