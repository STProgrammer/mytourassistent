package com.aphex.mytourassistent.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aphex.mytourassistent.repository.Repository;
import com.aphex.mytourassistent.repository.db.entities.GeoPointActual;
import com.aphex.mytourassistent.repository.db.entities.GeoPointPlanned;
import com.aphex.mytourassistent.repository.db.entities.Tour;
import com.aphex.mytourassistent.repository.db.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.repository.network.models.Data;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActiveTourViewModel extends AndroidViewModel {


    private final MutableLiveData<ArrayList<GeoPoint>> geoPointsPlanned;
    private final MutableLiveData<ArrayList<GeoPoint>> geoPointsActual;

    private final Repository repository;
    private GeoPoint currentLocation;
    private long currentGeoPointActualId = -1;


    private double currentZoomLevel;


    public ActiveTourViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        geoPointsPlanned = new MutableLiveData<>();
        geoPointsPlanned.setValue(new ArrayList<GeoPoint>());
        geoPointsActual = new MutableLiveData<>();
        geoPointsActual.setValue(new ArrayList<GeoPoint>());
    }

    public void addToGeoPointsPlanned(GeoPoint gp) {
        geoPointsPlanned.getValue().add(gp);
    }

    public LiveData<ArrayList<GeoPoint>> getGeoPointsPlanned() {
        return geoPointsPlanned;
    }

    public LiveData<ArrayList<GeoPoint>> getGeoPointsActual() {
        return geoPointsActual;
    }

    public LiveData<TourWithAllGeoPoints> getTourWithAllGeoPoints(long tourId, boolean mIsFirstTime) {
        return repository.getTourWithAllGeoPoints(tourId, mIsFirstTime);
    }

    public void clearGeoPoints(long tourId) {
        repository.clearGeoPoints(tourId);
    }

    public void updateTour(Tour tour) {
        repository.updateTour(tour);
    }

    public GeoPoint getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(GeoPoint gp) {
        currentLocation = gp;
    }

    public long getCurrentGeoPointActualId() {
        return currentGeoPointActualId;
    }

    public void setCurrentGeoPointActualId(long gpaId) {
        currentGeoPointActualId = gpaId;
    }

    public void setCurrentZoom(double zoomLevelDouble) {
        currentZoomLevel = zoomLevelDouble;
    }

    public double getCurrentZoomLevel() {
        return currentZoomLevel;
    }


    public LiveData<GeoPointActual> getLastGeoPointRecorded() {
        return repository.getLastGeoPointRecorded();
    }

    public MutableLiveData<Integer> getTourStatus() {
        return repository.getTourStatus();
    }

    public void updateWeatherData(double lat, double lng, Date date) {
        repository.getWeatherData(lat, lng, date, true);
    }

    public LiveData<Data> getWeatherData() {
        return repository.getFirstWeatherLiveData();
    }

    public void savePhoto(String uri) {
        repository.savePhoto(uri, currentGeoPointActualId);
    }


    public long getActivityTourId() {
        return repository.getActivityTourId();
    }
}
