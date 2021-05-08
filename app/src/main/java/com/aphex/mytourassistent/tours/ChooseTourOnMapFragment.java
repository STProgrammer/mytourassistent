package com.aphex.mytourassistent.tours;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.databinding.FragmentChooseTourOnMapBinding;
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

import org.jetbrains.annotations.NotNull;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
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
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ChooseTourOnMapFragment extends Fragment {

private View view;


    private FragmentChooseTourOnMapBinding binding;

    private ToursViewModel toursViewModel;

    ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    private static final int CALLBACK_ALL_PERMISSIONS = 1;
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

    // Indikerer om servicen er startet eller stoppet:
    private boolean requestingLocationUpdates = false;

    private FusedLocationProviderClient fusedLocationClient;
    private Location previousLocation=null;
    private LocationCallback locationCallback;

    private TextView tvInfo;
    private TextView tvTrackedLocation;
    private Polyline mPolyline;

    private boolean gotInitialLocation = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentChooseTourOnMapBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Configuration.getInstance().load(requireActivity().getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext()));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        toursViewModel = new ViewModelProvider(requireActivity()).get(ToursViewModel.class);
//sharedViewModels
        verifyPermissions();
        initLocationUpdates();

        binding.mapView.getController().setZoom(7.0);



        binding.btnClearPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.mapView.invalidate();
                binding.mapView.getOverlays().clear();
                toursViewModel.getGeoPoints().clear();
                initMap();
            }
        });

        binding.btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastKnownLocation();
            }
        });

        binding.btnFinishPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).popBackStack();
            }
        });

    }

    // DEL 1: Finner siste kjente posisjon.
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener((Activity) requireContext(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            binding.mapView.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
                            binding.mapView.getController().setZoom(10.0);
                            Log.d("MY-LOCATION", "SIST KJENTE POSISJON: " + location.toString());
                        }
                    }
                });

    }


    @Override
    public void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            this.verifyPermissions();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.stopTracking();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("requestingLocationUpdates", requestingLocationUpdates);
        super.onSaveInstanceState(outState);
    }

/*
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.keySet().contains("requestingLocationUpdates")) {
            this.requestingLocationUpdates = savedInstanceState.getBoolean("requestingLocationUpdates");
        } else {
            this.requestingLocationUpdates = false;
        }
    }*/

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
        SettingsClient client = LocationServices.getSettingsClient(requireContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(requireActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // Alle lokasjopnsinnstillinger er OK, klienten kan nå initiere lokasjonsforespørsler her:
            }
        });
        task.addOnFailureListener(requireActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Lokasjopnsinnstillinger er IKKE OK, men det kan fikses ved å vise brukeren en dialog!!
                    try {
                        // Viser dialogen ved å kalle startResolutionForResult() OG SJEKKE resultatet i onActivityResult()
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

    public boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
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
            ActivityCompat.requestPermissions(requireActivity(), requiredPermissions, CALLBACK_ALL_PERMISSIONS);
        } else {
            initMap();
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CALLBACK_ALL_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED ) {
                    this.initMap();
                }
                return;
            default:
                Toast.makeText(requireContext(), "Feil ...! Ingen tilgang!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initMap() {






        //map_view.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setBuiltInZoomControls(true);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getController().setZoom(8.0);

        // Punkter:
        //GeoPoint geoPointStart = new GeoPoint(68.439198,17.445000);

        // Markers:
        /*Marker startMarker = new Marker(binding.mapView);
        startMarker.setPosition(geoPointStart);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        binding.mapView.getOverlays().add(startMarker);
        startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_my_location_24, null));
        startMarker.setTitle("Start point");
        //startMarker.setTextIcon("Startpunkt!");
        startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                Toast.makeText(requireContext(),"Klikk på ikon...", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        binding.mapView.getOverlays().add(startMarker);*/

        //binding.mapView.getController().setCenter(geoPointStart);

        // Compass overlay;
        this.mCompassOverlay = new CompassOverlay(requireContext(), new InternalCompassOrientationProvider(requireContext()), binding.mapView);
        this.mCompassOverlay.enableCompass();
        binding.mapView.getOverlays().add(this.mCompassOverlay);

        // Multi touch:
        mRotationGestureOverlay = new RotationGestureOverlay(requireActivity(), binding.mapView);
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
        final MapEventsReceiver mReceive = new MapEventsReceiver(){
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                return false;
            }
            @Override
            public boolean longPressHelper(GeoPoint geoPoint) {
                Toast.makeText(requireContext(),geoPoint.getLatitude() + " - "+geoPoint.getLongitude(), Toast.LENGTH_LONG).show();
                Marker marker = new Marker(binding.mapView);
                marker.setPosition(geoPoint);
                toursViewModel.addToGeoPoints(geoPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

                databaseWriteExecutor.execute(() -> {
                    RoadManager roadManager = new OSRMRoadManager(requireContext(), "Aaa");
                    Road road = roadManager.getRoad(toursViewModel.getGeoPoints());
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                    binding.mapView.getOverlays().add(roadOverlay);
                    marker.setIcon(requireActivity().getResources().getDrawable(R.drawable.ic_baseline_my_location_24, null));
                    marker.setTitle("Klikkpunkt");
                    binding.mapView.getOverlays().add(marker);
                });

                return false;
            }
        };
        binding.mapView.getOverlays().add(new MapEventsOverlay(mReceive));

        if (!toursViewModel.getGeoPoints().isEmpty()) {
            //this means user already have selected geopoints
            //in this case, we will connect all waypoints when user come to this screen
            //wen need to iterate over all the waypoints and connect them together
            //waypint1
            //waypint 2
            //waypint 3
            //iteration 1


            databaseWriteExecutor.execute(() -> {

                for (GeoPoint gp: toursViewModel.getGeoPoints()) {
                    Marker marker = new Marker(binding.mapView);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    marker.setIcon(requireActivity().getResources().getDrawable(R.drawable.ic_baseline_my_location_24, null));
                    marker.setTitle("Klikkpunkt");
                    marker.setPosition(gp);
                    binding.mapView.getOverlays().add(marker);
                }


                RoadManager roadManager = new OSRMRoadManager(requireContext(), "Aaa");
                Road road = roadManager.getRoad(toursViewModel.getGeoPoints());
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                binding.mapView.getOverlays().add(roadOverlay);

            });
        }


    }

}