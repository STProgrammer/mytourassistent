package com.aphex.mytourassistent.views.fragments.tours.add;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.databinding.FragmentChooseTourOnMapBinding;
import com.aphex.mytourassistent.enums.TourType;
import com.aphex.mytourassistent.viewmodels.AddTourViewModel;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ChooseTourOnMapFragment extends Fragment {

    private FragmentChooseTourOnMapBinding binding;

    private AddTourViewModel addTourViewModel;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    private static final int REQUEST_CHECK_SETTINGS = 10;

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


    private FusedLocationProviderClient fusedLocationClient;

    private Marker endMarker;
    private boolean mIsFirstTime;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            mIsFirstTime = true;
        } else {
            mIsFirstTime = false;
        }

        // Inflate the layout for this fragment
        binding = FragmentChooseTourOnMapBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Configuration.getInstance().load(requireActivity().getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext()));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        addTourViewModel = new ViewModelProvider(requireActivity()).get(AddTourViewModel.class);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());

        initLocationUpdates();

        binding.mapView.getController().setZoom(7.0);

        //listen to weather api responses
        addTourViewModel.getFirstWeatherGeoPointResponse()
                .observe(requireActivity(), data -> {
                    if (isAdded()) {
                        Log.d("DebugCrash", "onChanged: ");
                        if (isAdded()) {
                            binding.tvWeatherPlanningStart.setVisibility(View.VISIBLE);
                            binding.ivWeatherIconStart.setVisibility(View.VISIBLE);
                            if (data == null) {
                                Log.d("DebugCrash", "now here");
                                binding.tvWeatherPlanningStart.setText(getString(R.string.no_weather_information));
                            } else {
                                Log.d("DebugCrash", "Data not null here");
                                String valueToShow = getString(R.string.weather_start_point_label);
                                valueToShow += getString(R.string.temperature_label);
                                valueToShow += data.getInstant().getDetails().getAirTemperature();
                                valueToShow += getString(R.string.relative_humidity_label);
                                valueToShow += data.getInstant().getDetails().getRelativeHumidity();
                                if (data.getNext1Hours() != null) {
                                    int resId = mappingWeatherSymbolAndCode(data.getNext1Hours().getSummary().getSymbolCode());
                                    binding.ivWeatherIconStart.setImageResource(resId);
                                }
                                binding.tvWeatherPlanningStart.setText(valueToShow);
                            }
                        }
                    }
                });

        addTourViewModel.getLastWeatherGeoPointResponse().observe(requireActivity(), data -> {

            if (isAdded()) {
                binding.tvWeatherPlanningEnd.setVisibility(View.VISIBLE);
                binding.ivWeatherIconEnd.setVisibility(View.VISIBLE);
                if (data == null) {
                    binding.tvWeatherPlanningEnd.setText(getString(R.string.no_weather_information));
                } else {
                    String valueToShow = getString(R.string.weather_end_point_label);
                    valueToShow += getString(R.string.temperature_label);
                    valueToShow += data.getInstant().getDetails().getAirTemperature();
                    valueToShow += getString(R.string.relative_humidity_label);
                    valueToShow += data.getInstant().getDetails().getRelativeHumidity();
                    if (data.getNext1Hours() != null) {
                        int resId = mappingWeatherSymbolAndCode(data.getNext1Hours().getSummary().getSymbolCode());
                        binding.ivWeatherIconEnd.setImageResource(resId);
                    }
                    binding.tvWeatherPlanningEnd.setText(valueToShow);
                }
            }
        });


        addTourViewModel.getFirstGeoPoint().observe(requireActivity(), geoPoint -> {
            if (isAdded()) {
                if (geoPoint != null) {
                    getWeatherData(geoPoint, true);
                }
            }
        });

        addTourViewModel.getLastGeoPoint().observe(requireActivity(), geoPoint -> {
            if (isAdded()) {
                if (geoPoint != null) {
                    getWeatherData(geoPoint, false);
                }

            }
        });


        binding.btnClearPlan.setOnClickListener(v -> {
            binding.mapView.invalidate();
            binding.mapView.getOverlays().clear();
            addTourViewModel.getGeoPointsPlanning().getValue().clear();
            addTourViewModel.getFirstGeoPoint().setValue(null);
            initMap();

        });

        binding.btnMyLocation.setOnClickListener(v -> {
            if (!hasPermissions(requiredPermissions)) {
                verifyPermissions();
            } else {
                getLastKnownLocation();
            }
        });

        binding.btnFinishPlan.setOnClickListener(v -> Navigation.findNavController(getView()).popBackStack());

        initMap();

        if (mIsFirstTime) {
            verifyPermissions();
        }

    }

    private void getWeatherData(GeoPoint geoPoint, boolean isFirstGp) {
        if (prefs.getBoolean("show_weather_checkbox", true)) {
            addTourViewModel.getWeatherData(geoPoint.getLatitude(), geoPoint.getLongitude(), isFirstGp);
        }
    }

    private int mappingWeatherSymbolAndCode(String symbolCode) {
        int resID = getResources().getIdentifier(symbolCode, "drawable", requireActivity().getPackageName());
        return resID;
    }

    // Find last known position
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener((Activity) requireContext(), location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        binding.mapView.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
                        binding.mapView.getController().setZoom(10.0);
                        Log.d("MY-LOCATION", "SIST KJENTE POSISJON: " + location.toString());
                    }
                });
    }


    // Verifying requirements
    //   If OK fine-location-acceptenca is verified, start of location queries
    private void initLocationUpdates() {
        final LocationRequest locationRequest = this.createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        // Checking if settings can be fulfilled
        SettingsClient client = LocationServices.getSettingsClient(requireContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(requireActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All settings OK, cliend can initialize locations from here
            }
        });
        task.addOnFailureListener(requireActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Settings are not OK, but it can be fixed by showing user a dialog
                    try {
                        // Shwoing the dialog by calling startResolutionForResult() and checking results in onActivityResult()
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS);
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

    /**
     * SE: http://developer.android.com/training/permissions/requesting.html#handle-response
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    public void verifyPermissions() {

        //This code is taken and partly edited from:
        //https://www.androidhive.info/2017/12/android-easy-runtime-permissions-with-dexter/
        Dexter.withContext(requireActivity())
                .withPermissions(
                        requiredPermissions
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                Log.d("TAG", "onPermissionsChecked: ");
                // check if all permissions are granted
                if (report.areAllPermissionsGranted()) {

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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.need_permissions);
        builder.setMessage(R.string.need_permissions_message);
        builder.setPositiveButton(getString(R.string.dialog_go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(R.string.btn_cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }

    //This code is taken and partly edited from:
    //https://www.androidhive.info/2017/12/android-easy-runtime-permissions-with-dexter/
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }


    public boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //Kalles når bruker har akseptert og gitt tillatelse til bruk av posisjon:
            case REQUEST_CHECK_SETTINGS:
                initLocationUpdates();
                return;
        }
    }


    private void initMap() {

        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setBuiltInZoomControls(true);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getController().setZoom(8.0);

        binding.mapView.getController().setCenter(new GeoPoint(59.74, 10.21));

        // Compass overlay;
        this.mCompassOverlay = new CompassOverlay(requireContext(), new InternalCompassOrientationProvider(requireContext()), binding.mapView);
        this.mCompassOverlay.enableCompass();
        binding.mapView.getOverlays().add(this.mCompassOverlay);

        // Multi touch:
        mRotationGestureOverlay = new RotationGestureOverlay(requireActivity(), binding.mapView);
        mRotationGestureOverlay.setEnabled(true);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getOverlays().add(this.mRotationGestureOverlay);

        // Zoom-buttons;
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(binding.mapView);
        mScaleBarOverlay.setCentred(true);
        //play around with these values to get the location on screen in the right place for your application
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        binding.mapView.getOverlays().add(this.mScaleBarOverlay);


        // Taking positions from map
        final MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint geoPoint) {

                if (addTourViewModel.getFirstGeoPoint().getValue() == null) {
                    addTourViewModel.getFirstGeoPoint().postValue(geoPoint);
                    // Markers:
                    Marker startMarker = new Marker(binding.mapView);
                    startMarker.setPosition(geoPoint);
                    startMarker.setAnchor(Marker.ANCHOR_TOP, Marker.ANCHOR_TOP);
                    binding.mapView.getOverlays().add(startMarker);
                    startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_circle_24, null));
                } else {
                    if (endMarker != null) {
                        binding.mapView.getOverlays().remove(endMarker);
                    }
                    addTourViewModel.getLastGeoPoint().postValue(geoPoint);
                    endMarker = new Marker(binding.mapView);
                    endMarker.setPosition(geoPoint);
                    endMarker.setAnchor(Marker.ANCHOR_TOP, Marker.ANCHOR_TOP);
                    binding.mapView.getOverlays().add(endMarker);
                    endMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_flag_24, null));
                }

                Marker marker = new Marker(binding.mapView);
                marker.setPosition(geoPoint);
                addTourViewModel.addToGeoPointsPlanning(geoPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);


                databaseWriteExecutor.execute(() -> {
                    RoadManager roadManager = getRoadManager();
                    Road road = roadManager.getRoad(addTourViewModel.getGeoPointsPlanning().getValue());
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                    binding.mapView.getOverlays().add(roadOverlay);
                    marker.setIcon(requireActivity().getResources().getDrawable(R.drawable.ic_baseline_my_location_24, null));
                    binding.mapView.getOverlays().add(marker);
                });
                return false;
            }
        };
        binding.mapView.getOverlays().add(new MapEventsOverlay(mReceive));

        if (!addTourViewModel.getGeoPointsPlanning().getValue().isEmpty()) {

            databaseWriteExecutor.execute(() -> {

                for (GeoPoint gp : addTourViewModel.getGeoPointsPlanning().getValue()) {
                    Marker marker = new Marker(binding.mapView);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    marker.setIcon(requireActivity().getResources().getDrawable(R.drawable.ic_baseline_my_location_24, null));
                    marker.setPosition(gp);
                    binding.mapView.getOverlays().add(marker);
                }

                RoadManager roadManager = getRoadManager();
                Road road = roadManager.getRoad(addTourViewModel.getGeoPointsPlanning().getValue());
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                binding.mapView.getOverlays().add(roadOverlay);
            });


            if (addTourViewModel.getFirstGeoPoint().getValue() != null) {
                // Markers:
                Marker startMarker = new Marker(binding.mapView);
                startMarker.setPosition(addTourViewModel.getFirstGeoPoint().getValue());
                startMarker.setAnchor(Marker.ANCHOR_TOP, Marker.ANCHOR_TOP);
                binding.mapView.getOverlays().add(startMarker);
                startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_circle_24, null));
            }
            if (addTourViewModel.getLastGeoPoint().getValue() != null) {
                endMarker = new Marker(binding.mapView);
                endMarker.setPosition(addTourViewModel.getLastGeoPoint().getValue());
                endMarker.setAnchor(Marker.ANCHOR_TOP, Marker.ANCHOR_TOP);
                binding.mapView.getOverlays().add(endMarker);
                endMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_flag_24, null));
            }
        }
    }

    public RoadManager getRoadManager() {
        RoadManager roadManager = new GraphHopperRoadManager(getString(R.string.graphhopper_api_key), true);
        if (addTourViewModel.getTourType() == TourType.WALKING.getValue()) {
            roadManager.addRequestOption(getString(R.string.api_param_vehicle_foot));
            roadManager.addRequestOption(getString(R.string.api_param_optimize_true));
        } else if (addTourViewModel.getTourType() == TourType.BIKING.getValue()) {
            roadManager.addRequestOption(getString(R.string.api_param_vehicle_bike));
            roadManager.addRequestOption(getString(R.string.api_param_optimize_true));
        } else if (addTourViewModel.getTourType() == TourType.SKIING.getValue()) {
            roadManager.addRequestOption(getString(R.string.api_param_vehicle_hike));
            roadManager.addRequestOption(getString(R.string.api_param_optimize_true));
        }
        return roadManager;
    }

}