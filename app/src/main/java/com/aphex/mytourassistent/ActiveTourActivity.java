package com.aphex.mytourassistent;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;

/**
 * OPEN STREET MAPS:
 * Se https://github.com/osmdroid/osmdroid
 * OG=> https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Java)
 * og https://github.com/osmdroid/osmdroid/wiki
 * og https://medium.com/mindorks/have-you-heard-about-open-street-map-d6c51dc00bea
 *
 * NB! FÅ MED i onCreate():
 *   Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
 */
public class ActiveTourActivity extends AppCompatActivity {

    private static final int CALLBACK_ALL_PERMISSIONS = 1;
    private static final int REQUEST_CHECK_SETTINGS = 10;

    private static String[] requiredPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private MapView map_view;
    private CompassOverlay mCompassOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private MinimapOverlay mMinimapOverlay;

    // Indikerer om servicen er startet eller stoppet:
    private boolean requestingLocationUpdates = false;

    private FusedLocationProviderClient fusedLocationClient;
    private Location previousLocation=null;
    private LocationCallback locationCallback;

    private TextView tvInfo;
    private TextView tvTrackedLocation;
    private Polyline mPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_main);

        tvInfo = findViewById(R.id.tvInfo);
        tvTrackedLocation = findViewById(R.id.tvTrackedLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Callback for å fange opp LOKASJONsendringer:
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                StringBuffer locationBuffer = new StringBuffer();
                for (Location location : locationResult.getLocations()) {
                    // Beregner avstand fra forrisge veipunkt:
                    if (previousLocation==null)
                        previousLocation = location;
                    float distance = previousLocation.distanceTo(location);
                    Log.d("MY-LOCATION-DISTANCE", String.valueOf(distance));
                    Log.d("MY-LOCATION", location.toString());
                    if (distance>50) {
                        Log.d("MY-LOCATION", "MER ENN 50 METER!!");
                    }
                    previousLocation = location;

                    locationBuffer.append(location.getLatitude() + ", " + location.getLongitude() + "\n");

                    // Polyline: tegner stien.
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude() , location.getLongitude());
                    mPolyline.addPoint(geoPoint);
                    map_view.getController().setCenter(geoPoint);
                    map_view.invalidate();  //tegner kartet på nytt.
                }
                tvTrackedLocation.setText(locationBuffer);
            }
        };

        Button btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPolyline.setPoints(new ArrayList<>());
                map_view.invalidate();
            }
        });

        Button btnStartTracking = findViewById(R.id.btnStartTracking);
        btnStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLocationUpdates();
                tvInfo.setText("Tracking pågår.");
            }
        });

        Button btnStopTracking = findViewById(R.id.btnStopTracking);
        btnStopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTracking();
                tvInfo.setText("Tracking avsluttet.");
            }
        });

        verifyPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            this.verifyPermissions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.stopTracking();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("requestingLocationUpdates", requestingLocationUpdates);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.keySet().contains("requestingLocationUpdates")) {
            this.requestingLocationUpdates = savedInstanceState.getBoolean("requestingLocationUpdates");
        } else {
            this.requestingLocationUpdates = false;
        }
    }

    private void stopTracking() {
        if (fusedLocationClient != null && locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // Verifiserer kravene satt i locationRequest-objektet.
    //   Dersom OK verifiseres fine-location-tillatelse start av lokasjonsforespørsler.
    private void initLocationUpdates() {
        final LocationRequest locationRequest = this.createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        // NB! Sjekker om kravene satt i locationRequest kan oppfylles:
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // Alle lokasjopnsinnstillinger er OK, klienten kan nå initiere lokasjonsforespørsler her:
                startLocationUpdates();
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Lokasjopnsinnstillinger er IKKE OK, men det kan fikses ved å vise brukeren en dialog!!
                    try {
                        // Viser dialogen ved å kalle startResolutionForResult() OG SJEKKE resultatet i onActivityResult()
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(ActiveTourActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    // LocationRequest: Setter krav til posisjoneringa:
    public LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        // Hvor ofte ønskes lokasjonsoppdateringer (her: hvert 10.sekund)
        locationRequest.setInterval(5000);
        // Her settes intervallet for hvor raskt appen kan håndtere oppdateringer.
        locationRequest.setFastestInterval(3000);
        // Ulike verderi; Her: høyest mulig nøyaktighet som også normalt betyr bruk av GPS.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * SE: http://developer.android.com/training/permissions/requesting.html#handle-response
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    public void verifyPermissions() {
        // Kontrollerer om vi har tilgang til eksternt område:
        if (!hasPermissions(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, CALLBACK_ALL_PERMISSIONS);
        } else {
            initMap();
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = this.createLocationRequest();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */);
        requestingLocationUpdates = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //Kalles når bruker har akseptert og gitt tillatelse til bruk av posisjon:
            case REQUEST_CHECK_SETTINGS:
                initLocationUpdates();
                return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CALLBACK_ALL_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED ) {
                    this.initMap();
                }
                return;
            default:
                Toast.makeText(this, "Feil ...! Ingen tilgang!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initMap() {

        map_view = findViewById(R.id.map_view);

        this.mPolyline = new Polyline(map_view);
        final Paint paintBorder = new Paint();
        paintBorder.setStrokeWidth(20);
        paintBorder.setStyle(Paint.Style.FILL_AND_STROKE);
        paintBorder.setColor(Color.BLACK);
        paintBorder.setStrokeCap(Paint.Cap.ROUND);
        paintBorder.setAntiAlias(true);

        final Paint paintInside = new Paint();
        paintInside.setStrokeWidth(10);
        paintInside.setStyle(Paint.Style.FILL);
        paintInside.setColor(Color.WHITE);
        paintInside.setStrokeCap(Paint.Cap.ROUND);
        paintInside.setAntiAlias(true);

        mPolyline.getOutlinePaintLists().add(new MonochromaticPaintList(paintBorder));
        mPolyline.getOutlinePaintLists().add(new MonochromaticPaintList(paintInside));

        map_view.getOverlays().add(mPolyline);

        //map_view.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        map_view.setTileSource(TileSourceFactory.MAPNIK);
        map_view.setBuiltInZoomControls(true);
        map_view.setMultiTouchControls(true);
        map_view.getController().setZoom(14.0);

        // Punkter:
        GeoPoint geoPointStart = new GeoPoint(68.439198,17.445000);
        GeoPoint geoPointEnd = new GeoPoint(68.432000,17.435700);

        // Markers:
        Marker startMarker = new Marker(map_view);
        startMarker.setPosition(geoPointStart);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        map_view.getOverlays().add(startMarker);
        startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_my_location_24, null));
        startMarker.setTitle("Start point");
        //startMarker.setTextIcon("Startpunkt!");
        startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                Toast.makeText(getBaseContext(),"Klikk på ikon...", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        map_view.getOverlays().add(startMarker);

        map_view.getController().setCenter(geoPointStart);

        // Compass overlay;
        this.mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), map_view);
        this.mCompassOverlay.enableCompass();
        map_view.getOverlays().add(this.mCompassOverlay);

        // Multi touch:
        mRotationGestureOverlay = new RotationGestureOverlay(this, map_view);
        mRotationGestureOverlay.setEnabled(true);
        map_view.setMultiTouchControls(true);
        map_view.getOverlays().add(this.mRotationGestureOverlay);

        // Zoom-knapper;
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(map_view);
        mScaleBarOverlay.setCentred(true);
        //play around with these values to get the location on screen in the right place for your application
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        map_view.getOverlays().add(this.mScaleBarOverlay);

        // Fange opp posisjon i klikkpunkt på kartet:
        final MapEventsReceiver mReceive = new MapEventsReceiver(){
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                //Toast.makeText(getBaseContext(),p.getLatitude() + " - "+p.getLongitude(), Toast.LENGTH_LONG).show();
                Marker marker = new Marker(map_view);
                marker.setPosition(geoPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                map_view.getOverlays().add(marker);
                marker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_my_location_24, null));
                marker.setTitle("Klikkpunkt");
                map_view.getOverlays().add(marker);
                return false;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        map_view.getOverlays().add(new MapEventsOverlay(mReceive));
    }

}