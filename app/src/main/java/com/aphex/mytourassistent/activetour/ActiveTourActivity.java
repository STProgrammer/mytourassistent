package com.aphex.mytourassistent.activetour;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.aphex.mytourassistent.BuildConfig;
import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.databinding.ActivityActiveTourBinding;
import com.aphex.mytourassistent.entities.GeoPointActual;
import com.aphex.mytourassistent.entities.GeoPointPlanned;
import com.aphex.mytourassistent.entities.Tour;
import com.aphex.mytourassistent.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.enums.TourStatus;
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

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * OPEN STREET MAPS:
 * Se https://github.com/osmdroid/osmdroid
 * OG=> https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Java)
 * og https://github.com/osmdroid/osmdroid/wiki
 * og https://medium.com/mindorks/have-you-heard-about-open-street-map-d6c51dc00bea
 * <p>
 * NB! FÅ MED i onCreate():
 * Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
 */
public class ActiveTourActivity extends AppCompatActivity {

    private ActivityActiveTourBinding binding;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    private long tourId;
    private boolean mIsFirstTime;
    private boolean firstTimeLocation;

    private TourWithAllGeoPoints tourWithAllGeoPoints;
    private boolean trackingActive;
    private boolean trackingFinished;
    private long travelOrder;

    private final int ACTIVE = TourStatus.ACTIVE.getValue();

    private static final int CALLBACK_ALL_PERMISSIONS = 1;
    private static final int REQUEST_CHECK_SETTINGS = 10;

    private static String[] requiredPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private MapView mapView;
    private CompassOverlay mCompassOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private MinimapOverlay mMinimapOverlay;

    // Indikerer om servicen er startet eller stoppet:
    private boolean requestingLocationUpdates = false;

    private FusedLocationProviderClient fusedLocationClient;
    private Location previousLocation = null;
    private LocationCallback locationCallback;

    private Polyline mPolyline;
    private ActiveTourViewModel activeTourViewModel;
    private int tourStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mIsFirstTime = true;
        }
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        tourId = getIntent().getLongExtra("TOUR_ID", 0L);
        tourStatus = getIntent().getIntExtra("TOUR_STATUS", 1);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivityActiveTourBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());


        activeTourViewModel = new ViewModelProvider(this).get(ActiveTourViewModel.class);


        //FETCHING DATA FROM DATABASE TOUR AND LOCATIONS
        activeTourViewModel.getTourWithAllGeoPoints(tourId, mIsFirstTime).observe(this, tourWithAllGeoPoints -> {
            if (tourWithAllGeoPoints != null) {
                StringBuilder sb = new StringBuilder();
                String startDatePlanned = new SimpleDateFormat("yyyy-MM-dd HH")
                        .format(new Date(tourWithAllGeoPoints.tour.startTimePlanned));
                String finishDatePlanned = new SimpleDateFormat("yyyy-MM-dd HH")
                        .format(new Date(tourWithAllGeoPoints.tour.finishTimePlanned));
                String tourType = "";
                String tourStatus = "";

                this.tourWithAllGeoPoints = tourWithAllGeoPoints;
                this.tourStatus = tourWithAllGeoPoints.tour.tourStatus;

                switch (tourWithAllGeoPoints.tour.tourType) {
                    case 1:
                        tourType = getString(R.string.tour_type_walking);
                        break;
                    case 2:
                        tourType = getString(R.string.tour_type_bicycling);
                        break;
                    case 3:
                        tourType = getString(R.string.tour_type_skiing);
                        break;
                }

                if (!firstTimeLocation) {
                    //Start drawing based on tour status:
                    switch (tourWithAllGeoPoints.tour.tourStatus) {
                        case 1:
                            //we will make ui according o this state
                            //set the button as play
                            binding.ivPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_arrow_24));
                            break;
                        case 2:
                            //lets change the play button to pause button
                            binding.ivPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_pause_24));
                            initLocationUpdates();
                            drawTheRoute(tourWithAllGeoPoints.geoPointsActual);
                            break;
                        case 3:
                            binding.ivPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_arrow_24));
                            drawTheRoute(tourWithAllGeoPoints.geoPointsActual);
                            break;
                    }
                    firstTimeLocation = true;
                }

                for (GeoPointPlanned gp : tourWithAllGeoPoints.geoPointsPlanned) {
                    Marker marker = new Marker(binding.mapView);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    marker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_my_location_24, null));
                    marker.setTitle("Klikkpunkt");
                    GeoPoint geoPt = new GeoPoint(gp.lat, gp.lng);
                    marker.setPosition(geoPt);
                    activeTourViewModel.addToGeoPointsPlanned(geoPt);
                    binding.mapView.getOverlays().add(marker);
                }

                databaseWriteExecutor.execute(() -> {
                    RoadManager roadManager = new OSRMRoadManager(this, "Aaa");
                    Road road = roadManager.getRoad(activeTourViewModel.getGeoPointsPlanned().getValue());
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                    binding.mapView.getOverlays().add(roadOverlay);
                });

                // Punkter:
                if (mIsFirstTime && tourWithAllGeoPoints.tour.tourStatus == 2) {

                    GeoPoint geoPointStart = Objects.requireNonNull(activeTourViewModel.getGeoPointsPlanned().getValue()).get(0);

                    // Markers:
                    Marker startMarker = new Marker(binding.mapView);
                    startMarker.setPosition(geoPointStart);
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    binding.mapView.getOverlays().add(startMarker);
                    startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_circle_24, null));
                    startMarker.setTitle("Start point");
                    //startMarker.setTextIcon("Startpunkt!");

                    binding.mapView.getOverlays().add(startMarker);
                    binding.mapView.getController().setCenter(geoPointStart);
                    // firstTimeLocation = true;
                }
            }
        });

        if (!mIsFirstTime) {
            binding.mapView.getController().setCenter(activeTourViewModel.getCurrentLocation());
            binding.mapView.getController().setZoom(activeTourViewModel.getCurrentZoomLevel());
        }


        //  activeTourViewModel.getTourWithGeoPointsPlanned()


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
                    if (previousLocation == null)
                        previousLocation = location;
                    float distance = previousLocation.distanceTo(location);
                    Log.d("MY-LOCATION-DISTANCE", String.valueOf(distance));
                    Log.d("MY-LOCATION", location.toString());

                    previousLocation = location;

                    locationBuffer.append(location.getLatitude() + ", " + location.getLongitude() + "\n");

                    // Polyline: tegner stien.
                    GeoPoint gp = new GeoPoint(location.getLatitude(), location.getLongitude());
                    activeTourViewModel.updateCurrentLocation(gp);
                    //we keep on saving the updated location in our main memory
                    //activeTourViewModel.addToGeoPointsActual(gp);

                    mPolyline.addPoint(gp);
                    binding.mapView.getController().setCenter(gp);

                    //before storing it to db
                    //check with the last location object
                    //add the method here to check if distance is more than 5 meters then save it in
                    //db
                    //activeTourViewModel.addGeoPointsActual(gpa);
                    if (distance > 10) {
                        GeoPointActual gpa = new GeoPointActual(gp.getLatitude(), gp.getLongitude(), tourId, travelOrder++, null);
                        Log.d("MY-LOCATION", "MER ENN 50 METER!!" + distance);
                        activeTourViewModel.addGeoPointsActual(gpa);
                    }

                    binding.mapView.invalidate();  //tegner kartet på nytt.

                }
            }
        };


        binding.btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = tourWithAllGeoPoints.tour.tourStatus;
                if (status == TourStatus.ACTIVE.getValue() ||
                        status == TourStatus.PAUSED.getValue()) {
                    mPolyline.setPoints(new ArrayList<>());
                    Objects.requireNonNull(activeTourViewModel.getGeoPointsActual().getValue()).clear();
                    binding.mapView.invalidate();
                    activeTourViewModel.clearGeoPoints(tourId);
                    binding.tvInfo.setText(getString(R.string.tv_tracking_reset));
                }
            }
        });

        binding.ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = tourWithAllGeoPoints.tour.tourStatus;
                Tour tour = tourWithAllGeoPoints.tour;
                if (status == TourStatus.NOT_STARTED.getValue()) {
                    tour.startTimeActual = new Date().getTime();
                    tour.tourStatus = TourStatus.ACTIVE.getValue();
                    activeTourViewModel.updateTour(tour);

                    initLocationUpdates();
                    binding.tvInfo.setText(getString(R.string.tv_tracking_started));
                    binding.ivPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_pause_24));
                } else if (status == TourStatus.ACTIVE.getValue()) {
                    stopTracking();
                    tour.tourStatus = TourStatus.PAUSED.getValue();
                    activeTourViewModel.updateTour(tour);
                    binding.tvInfo.setText(getString(R.string.tv_tracking_paused));
                    binding.ivPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_arrow_24));
                } else if (status == TourStatus.PAUSED.getValue()) {
                    initLocationUpdates();
                    tour.tourStatus = TourStatus.ACTIVE.getValue();
                    activeTourViewModel.updateTour(tour);
                    binding.tvInfo.setText(getString(R.string.tv_tracking_started));
                    binding.ivPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_pause_24));
                }

            }
        });

        binding.ivStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = tourWithAllGeoPoints.tour.tourStatus;
                Tour tour = tourWithAllGeoPoints.tour;
                if (status == TourStatus.ACTIVE.getValue() ||
                        status == TourStatus.PAUSED.getValue()) {
                    tour.finishTimeActual = new Date().getTime();
                    tour.tourStatus = TourStatus.COMPLETED.getValue();
                    activeTourViewModel.updateTour(tour);
                    stopTracking();
                    //activeTourViewModel.completeTour()
                    binding.tvInfo.setText(getString(R.string.tv_tracking_completed));
                    binding.ivPlay.setVisibility(View.INVISIBLE);
                    binding.btnReset.setVisibility(View.INVISIBLE);
                }
            }
        });

        binding.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    dispatchTakePictureIntent();
                }


            }
        });

        binding.btnFindMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastKnownLocation();
            }
        });

        verifyPermissions();
    }

    private void drawTheRoute(List<GeoPointActual> geoPointsActual) {
        //fetch records / geopoints from db
        for (GeoPointActual gpa : geoPointsActual) {
            mPolyline.addPoint(new GeoPoint(gpa.lat, gpa.lng));
        }

        // draw white lines on the map
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
        activeTourViewModel.updateCurrentLocation((GeoPoint) binding.mapView.getMapCenter());
        activeTourViewModel.setCurrentZoom(binding.mapView.getZoomLevelDouble());
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
     * <p>
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
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
//                    Bundle extras = data.getExtras();
                    //Bitmap imageBitmap = (Bitmap) extras.get("data");
                    //imageView.setImageBitmap(imageBitmap);
                    //TODO save to database
                    //activeTourViewModel.saveImage();
                }
                return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CALLBACK_ALL_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    this.initMap();
                }
                return;
            default:
                Toast.makeText(this, "Feil ...! Ingen tilgang!!", Toast.LENGTH_SHORT).show();
        }
    }

    Marker currentMarker;

    // DEL 1: Finner siste kjente posisjon.
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener((Activity) this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            if (currentMarker != null) {
                                binding.mapView.getOverlays().remove(currentMarker);
                            }
                            currentMarker = new Marker(binding.mapView);
                            GeoPoint gp = new GeoPoint(location.getLatitude(), location.getLongitude());
                            activeTourViewModel.updateCurrentLocation(gp);
                            currentMarker.setPosition(gp);
                            currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                            currentMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_location_on_24, null));
                            binding.mapView.getOverlays().add(currentMarker);
                            binding.mapView.getController().setCenter(gp);
                            binding.mapView.getController().setZoom(17.0);
                            Log.d("MY-LOCATION", "SIST KJENTE POSISJON: " + location.toString());
                        }
                    }
                });

    }


    private void initMap() {

        this.mPolyline = new Polyline(binding.mapView);
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

        binding.mapView.getOverlays().add(mPolyline);

        //map_view.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setBuiltInZoomControls(true);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getController().setZoom(10.0);


        // Compass overlay;
        this.mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), binding.mapView);
        this.mCompassOverlay.enableCompass();
        binding.mapView.getOverlays().add(this.mCompassOverlay);

        // Multi touch:
        mRotationGestureOverlay = new RotationGestureOverlay(this, binding.mapView);
        mRotationGestureOverlay.setEnabled(true);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getOverlays().add(this.mRotationGestureOverlay);

        // Zoom-knapper;
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(binding.mapView);
        mScaleBarOverlay.setCentred(true);
        //play around with these values to get the location on screen in the right place for your application
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        binding.mapView.getOverlays().add(this.mScaleBarOverlay);

        // Fange opp posisjon i klikkpunkt på kartet:
        final MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                //Toast.makeText(getBaseContext(),p.getLatitude() + " - "+p.getLongitude(), Toast.LENGTH_LONG).show();
                Marker marker = new Marker(binding.mapView);
                marker.setPosition(geoPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                binding.mapView.getOverlays().add(marker);
                marker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_my_location_24, null));
                marker.setTitle("Klikkpunkt");
                binding.mapView.getOverlays().add(marker);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        binding.mapView.getOverlays().add(new MapEventsOverlay(mReceive));


       /* if (!activeTourViewModel.getGeoPointsPlanned(tourId).isEmpty()) {
            //this means user already have selected geopoints
            //in this case, we will connect all waypoints when user come to this screen
            //wen need to iterate over all the waypoints and connect them together
            //waypint1
            //waypint 2
            //waypint 3
            //iteration 1


            databaseWriteExecutor.execute(() -> {

                for (GeoPoint gp: activeTourViewModel.getGeoPoints()) {
                    Marker marker = new Marker(binding.mapView);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    marker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_my_location_24, null));
                    marker.setTitle("Klikkpunkt");
                    marker.setPosition(gp);
                    binding.mapView.getOverlays().add(marker);
                }

                RoadManager roadManager = new OSRMRoadManager(this, "Aaa");
                Road road = roadManager.getRoad(activeTourViewModel.getGeoPoints());
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                binding.mapView.getOverlays().add(roadOverlay);
            });
        }*/

    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        long lastGpId = activeTourViewModel.getLastInsertedGeoPointActualId(tourId);
        String imageFileName = lastGpId + "_" + tourId;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir +"/"+
                imageFileName + ".jpeg");

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    
}