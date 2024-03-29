package com.aphex.mytourassistent.views.activities;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.enums.TourType;
import com.aphex.mytourassistent.repository.db.entities.GeoPointActualWithPhotos;
import com.aphex.mytourassistent.viewmodels.ActiveTourViewModel;
import com.aphex.mytourassistent.services.TourTrackingService;
import com.aphex.mytourassistent.databinding.ActivityActiveTourBinding;
import com.aphex.mytourassistent.repository.db.entities.GeoPointPlanned;
import com.aphex.mytourassistent.repository.db.entities.Tour;
import com.aphex.mytourassistent.repository.db.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.enums.TourStatus;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.File;
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

    public static final String STRING_ACTION = "END_SERVICE";
    private ActivityActiveTourBinding binding;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    private long tourId;
    private boolean mIsFirstTime;
    private boolean firstTimeLocation;

    private TourWithAllGeoPoints tourWithAllGeoPoints;
    public static long travelOrder;


    private static String[] requiredPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private CompassOverlay mCompassOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private ScaleBarOverlay mScaleBarOverlay;


    private Polyline mPolyline;
    private ActiveTourViewModel activeTourViewModel;
    private SharedPreferences prefs;
    private boolean showWeatherIsSet;


    //START BUILDING ACTIVITY
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mIsFirstTime = true;
        }
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));


        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivityActiveTourBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());

        prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);

        showWeatherIsSet = prefs.getBoolean("show_weather_checkbox", true);


        activeTourViewModel = new ViewModelProvider(this).get(ActiveTourViewModel.class);

        tourId = activeTourViewModel.getActivityTourId();
        Log.d("Debug", "onCreate: "+tourId);


        binding.btnReset.setOnClickListener(v -> {
            int status = tourWithAllGeoPoints.tour.tourStatus;
            if (status == TourStatus.ACTIVE.getValue() ||
                    status == TourStatus.PAUSED.getValue()) {
                mPolyline.setPoints(new ArrayList<>());
                Objects.requireNonNull(activeTourViewModel.getGeoPointsActual().getValue()).clear();
                binding.mapView.invalidate();
                activeTourViewModel.clearGeoPoints(tourId);
                binding.tvInfo.setText(getString(R.string.tv_tracking_reset));
            }
        });

        binding.ivPlay.setOnClickListener(v -> {
            if (!hasPermissions(requiredPermissions)) {
                verifyPermissions();
                return;
            }
            int status = tourWithAllGeoPoints.tour.tourStatus;
            Tour tour = tourWithAllGeoPoints.tour;
            if (status == TourStatus.NOT_STARTED.getValue()) {
                tour.startTimeActual = new Date().getTime();
                tour.startTimeOfTour = new Date().getTime();
                tour.tourStatus = TourStatus.ACTIVE.getValue();
                activeTourViewModel.updateTour(tour);
                startRecording();
            } else if (status == TourStatus.ACTIVE.getValue()) {
                stopRecording();
                tour.tourStatus = TourStatus.PAUSED.getValue();
                activeTourViewModel.updateTour(tour);
            } else if (status == TourStatus.PAUSED.getValue()) {
                startRecording();
                tour.startTimeOfTour = new Date().getTime();
                tour.tourStatus = TourStatus.ACTIVE.getValue();
                activeTourViewModel.updateTour(tour);
            }

        });

        binding.ivStop.setOnClickListener(v -> {

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ActiveTourActivity.this);
            builder.setTitle(R.string.btn_stop_tracking);
            builder.setMessage(R.string.are_you_sure_to_complete_tour);
            builder.setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                int status = tourWithAllGeoPoints.tour.tourStatus;
                Tour tour = tourWithAllGeoPoints.tour;
                if (status == TourStatus.ACTIVE.getValue() ||
                        status == TourStatus.PAUSED.getValue()) {
                    tour.finishTimeActual = new Date().getTime();
                    tour.tourStatus = TourStatus.COMPLETED.getValue();
                    activeTourViewModel.updateTour(tour);
                    stopRecording();
                }
            });
            builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
            builder.show();

        });

        binding.ivPhoto.setOnClickListener(v -> {
            //ask permissions
            //first check if we have permissions already if not then trigger

            //Taken and partly edited from:
            //https://www.androidhive.info/2017/12/android-easy-runtime-permissions-with-dexter/
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                Dexter.withContext(ActiveTourActivity.this)
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                // permission is granted, open the camera
                                dispatchTakePictureIntent();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                // check for permanent denial of permission
                                if (response.isPermanentlyDenied()) {
                                    showSettingsDialog();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();

            }


        });

        binding.btnFindMyLocation.setOnClickListener(v -> {
            if (!hasPermissions(requiredPermissions)) {
                verifyPermissions();
                return;
            }
            if (activeTourViewModel.getCurrentLocation() != null) {
                updateCurrentLocationIcon(activeTourViewModel.getCurrentLocation());
            } else {
                Toast.makeText(getApplicationContext(), R.string.toast_cant_find_my_location, Toast.LENGTH_SHORT).show();
            }

        });

        if (mIsFirstTime) {
            verifyPermissions();
        }

        initMap();

    }


    private void updateCurrentLocationIcon(GeoPoint gp) {
        if (currentMarker != null) {
            binding.mapView.getOverlays().remove(currentMarker);
        }
        currentMarker = new Marker(binding.mapView);
        currentMarker.setPosition(gp);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        currentMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_location_on_24, null));
        binding.mapView.getOverlays().add(currentMarker);
        binding.mapView.getController().setCenter(gp);
        binding.mapView.getController().setZoom(17.0);
        Log.d("MY-LOCATION", "SIST KJENTE POSISJON: " + gp.getLatitude());
    }

    private void stopRecording() {
        stopService(new Intent(this, TourTrackingService.class));
    }

    private void startRecording() {
        Intent intent = new Intent(this, TourTrackingService.class);
        Log.d("Debug", "startRecording:" +tourId);
        intent.putExtra("TOUR_ID", tourId);
        intent.putExtra("TRAVEL_ORDER", travelOrder);
        startForegroundService(intent);
    }
    //------------------------
    // END BUILDING ACTIVITY
    //------------------------


    private void drawTheRoute(List<GeoPointActualWithPhotos> geoPointsActual) {
        //fetch records / geopoints from db
        // draw white lines on the map
        for (GeoPointActualWithPhotos gpa : geoPointsActual) {
            mPolyline.addPoint(new GeoPoint(gpa.geoPointActual.lat, gpa.geoPointActual.lng));
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        activeTourViewModel.setCurrentLocation((GeoPoint) binding.mapView.getMapCenter());
        activeTourViewModel.setCurrentZoom(binding.mapView.getZoomLevelDouble());
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
     * SEE: http://developer.android.com/training/permissions/requesting.html#handle-response
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    public void verifyPermissions() {
        //This code is taken and partly edited from:
        //https://www.androidhive.info/2017/12/android-easy-runtime-permissions-with-dexter/
        Dexter.withContext(this)
                .withPermissions(
                        requiredPermissions
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                Log.d("TAG", "onPermissionsChecked: ");
                // check if all permissions are granted
                if (report.areAllPermissionsGranted()) {
                    //initMap();
                }

                // check for permanent denial of any permission
                if (report.isAnyPermissionPermanentlyDenied()) {
                    // show alert dialog navigating to Settings
                    showSettingsDialog();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        })
                .check();
    }


    //This code is taken and partly edited from:
    //https://www.androidhive.info/2017/12/android-easy-runtime-permissions-with-dexter/
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.need_permissions);
        builder.setMessage(R.string.need_permissions_message);
        builder.setPositiveButton(getString(R.string.dialog_go_to_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    //This code is taken and partly edited from:
    //https://www.androidhive.info/2017/12/android-easy-runtime-permissions-with-dexter/
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK

            Uri uri = data.getData();
            Uri uriForFile = FileProvider.getUriForFile(this, this.getPackageName() + ".provider", new File(uri.getPath()));

            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //imageView.setImageBitmap(imageBitmap);

            if (activeTourViewModel.getCurrentGeoPointActualId() < 0) {
                Toast.makeText(this, R.string.toast_please_start_tour, Toast.LENGTH_SHORT).show();
            } else {
                activeTourViewModel.savePhoto(uriForFile.toString());
                Toast.makeText(this, R.string.toast_photo_saved, Toast.LENGTH_SHORT).show();
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.task_cancelled), Toast.LENGTH_SHORT).show();
        }
    }

    Marker currentMarker;


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

        initRouteTracking();

    }

    public void initRouteTracking() {
        Log.d("Debug", "initRouteTracking: "+tourId);
        //FETCHING DATA FROM DATABASE TOUR AND LOCATIONS
        activeTourViewModel.getTourWithAllGeoPoints(tourId, mIsFirstTime).observe(this, tourWithAllGeoPoints -> {

            if (tourWithAllGeoPoints != null) {
                this.tourWithAllGeoPoints = tourWithAllGeoPoints;

                Log.d("ActiveDebug", "Here tour is not null");

                activeTourViewModel.getTourStatus().postValue(tourWithAllGeoPoints.tour.tourStatus);

                binding.tvActiveTourTitle.setText(getString(R.string.tours_list_title) + " " + tourWithAllGeoPoints.tour.title);

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
                    GeoPoint geoPt = new GeoPoint(gp.lat, gp.lng);
                    marker.setPosition(geoPt);
                    activeTourViewModel.addToGeoPointsPlanned(geoPt);
                    binding.mapView.getOverlays().add(marker);
                }

                databaseWriteExecutor.execute(() -> {
                    RoadManager roadManager = new GraphHopperRoadManager(getString(R.string.graphhopper_api_key), true);
                    if (tourWithAllGeoPoints.tour.tourType == TourType.WALKING.getValue()) {
                        roadManager.addRequestOption(getString(R.string.api_param_vehicle_foot));
                        roadManager.addRequestOption(getString(R.string.api_param_optimize_true));
                    } else if (tourWithAllGeoPoints.tour.tourType == TourType.BIKING.getValue()) {
                        roadManager.addRequestOption(getString(R.string.api_param_vehicle_bike));
                        roadManager.addRequestOption(getString(R.string.api_param_optimize_true));
                    } else if (tourWithAllGeoPoints.tour.tourType == TourType.SKIING.getValue()) {
                        roadManager.addRequestOption(getString(R.string.api_param_vehicle_hike));
                        roadManager.addRequestOption(getString(R.string.api_param_optimize_true));
                    }
                    Road road = roadManager.getRoad(activeTourViewModel.getGeoPointsPlanned().getValue());
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                    binding.mapView.getOverlays().add(roadOverlay);
                });


                // Points:
                if (mIsFirstTime && tourWithAllGeoPoints.tour.tourStatus == 2) {

                    GeoPoint geoPointStart = Objects.requireNonNull(activeTourViewModel.getGeoPointsPlanned().getValue()).get(0);

                    // Markers:
                    Marker startMarker = new Marker(binding.mapView);
                    startMarker.setPosition(geoPointStart);
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    binding.mapView.getOverlays().add(startMarker);
                    startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_circle_24, null));

                    binding.mapView.getOverlays().add(startMarker);

                }
            }
        });

        //listen to tour completion
        activeTourViewModel.getTourStatus().observe(this, status -> {
            switch (status) {
                case 1: //Not started
                    binding.ivPlay.setVisibility(View.VISIBLE);
                    binding.btnReset.setVisibility(View.VISIBLE);
                    binding.ivPhoto.setVisibility(View.VISIBLE);
                    binding.ivPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_arrow_24));
                    break;
                case 2: //Active
                    binding.tvInfo.setText(getString(R.string.tv_tracking_started));
                    binding.ivPlay.setVisibility(View.VISIBLE);
                    binding.btnReset.setVisibility(View.VISIBLE);
                    binding.ivPhoto.setVisibility(View.VISIBLE);
                    binding.ivPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_pause_24));
                    break;
                case 3: //Paused
                    binding.ivPlay.setVisibility(View.VISIBLE);
                    binding.btnReset.setVisibility(View.VISIBLE);
                    binding.ivPhoto.setVisibility(View.VISIBLE);
                    binding.tvInfo.setText(getString(R.string.tv_tracking_paused));
                    binding.ivPlay.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_arrow_24));
                    break;
                case 4: //Completed
                    binding.tvInfo.setText(getString(R.string.tv_tracking_completed));
                    binding.ivPlay.setVisibility(View.INVISIBLE);
                    binding.btnReset.setVisibility(View.INVISIBLE);
                    binding.ivPhoto.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        });

        if (!mIsFirstTime) {
            binding.mapView.getController().setCenter(activeTourViewModel.getCurrentLocation());
            binding.mapView.getController().setZoom(activeTourViewModel.getCurrentZoomLevel());
        }

        if (!showWeatherIsSet) {
            binding.tvWeather.setVisibility(View.GONE);
        }

        activeTourViewModel.getWeatherData().observe(this, data -> {
            Log.d("DebugDate", "inside getWeatherDataObserver");
            if (data == null) {
                binding.tvWeather.setText(getString(R.string.no_weather_information));
            } else {
                Log.d("DebugDate", "inside getWeatherDataObserver data not null");
                String valueToShow = getString(R.string.temperature_label);
                valueToShow += data.getInstant().getDetails().getAirTemperature();
                valueToShow += getString(R.string.relative_humidity_label);
                valueToShow += data.getInstant().getDetails().getRelativeHumidity();
                if (data.getNext1Hours() != null) {
                    int resId = mappingWeatherSymbolAndCode(data.getNext1Hours().getSummary().getSymbolCode());
                    binding.ivWeatherIcon.setImageDrawable(getDrawable(resId));
                }
                binding.tvWeather.setText(valueToShow);
            }
        });

        activeTourViewModel.getLastGeoPointRecorded().observe(ActiveTourActivity.this, geoPointActual -> {

            Log.d("MY-LOCATION-DRAWING", geoPointActual.lat + "latitude");

            // Polyline: drwaing route.
            GeoPoint gp = new GeoPoint(geoPointActual.lat, geoPointActual.lng);
            activeTourViewModel.setCurrentLocation(gp);

            activeTourViewModel.setCurrentGeoPointActualId(geoPointActual.geoPointActualId);

            mPolyline.addPoint(gp);
            binding.mapView.getController().setCenter(gp);
            binding.mapView.invalidate();  //drawing map again

            updateCurrentLocationIcon(activeTourViewModel.getCurrentLocation());

            //get weather data
            if (showWeatherIsSet) {
                activeTourViewModel.updateWeatherData(geoPointActual.lat, geoPointActual.lng, new Date());
            }
        });


    }

    private int mappingWeatherSymbolAndCode(String symbolCode) {
        int resID = getResources().getIdentifier(symbolCode, "drawable", getPackageName());
        return resID;
    }

    private void dispatchTakePictureIntent() {
        ImagePicker.with(this)
                .cameraOnly()
                .saveDir(getExternalFilesDir(Environment.DIRECTORY_PICTURES))
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

}