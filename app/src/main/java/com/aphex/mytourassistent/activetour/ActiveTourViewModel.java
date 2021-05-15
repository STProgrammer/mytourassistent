package com.aphex.mytourassistent.activetour;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aphex.mytourassistent.entities.GeoPointActual;
import com.aphex.mytourassistent.entities.GeoPointPlanned;
import com.aphex.mytourassistent.entities.Tour;
import com.aphex.mytourassistent.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.repository.Repository;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ActiveTourViewModel extends AndroidViewModel {

    private Calendar mCalendarStart;
    private Calendar mCalendarFinish;
    private String tourName;
    private String tourType;

    private MutableLiveData<ArrayList<GeoPoint>> geoPointsPlanned;
    private MutableLiveData<ArrayList<GeoPoint>> geoPointsActual;
    private MutableLiveData<Tour> tour;

    private Repository repository;
    private GeoPoint currentLocation;
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


    public void addNewTour(String tourName, long startTime, long endTime, int tourType, int tourStatus) {
        repository.addTour(tourName, startTime, endTime, tourType, tourStatus, geoPointsPlanned.getValue());
    }

    public LiveData<List<Tour>> getAllTours(boolean mIsFirstTime) {
        return repository.getAllUncompletedTours(mIsFirstTime);
    }

    public LiveData<List<GeoPointPlanned>> getGeoPointsPlanned(long tourId, boolean mIsFirstTime) {
            return repository.getGeoPointsPlanned(tourId, mIsFirstTime);
    }

    public void addToGeoPointsActual(GeoPoint gp) {
        geoPointsActual.getValue().add(gp);
    }

    public LiveData<ArrayList<GeoPoint>> getGeoPointsActual() {
        return geoPointsActual;
    }

    public MutableLiveData<Tour> getTour(long tourId) {
        return repository.getTour(tourId);
    }

    public LiveData<TourWithAllGeoPoints> getTourWithAllGeoPoints(long tourId, boolean mIsFirstTime) {
        return repository.getTourWithAllGeoPoints(tourId, mIsFirstTime);
    }

    public void addGeoPointsActual(GeoPointActual gpa) {
        repository.addGeoPointActual(gpa);
    }

    public void startTour(long tourId, long startTime, int status) {
        repository.startTour(tourId, startTime, status);


    }

    public void clearGeoPoints(long tourId) {
        repository.clearGeoPoints(tourId);
    }

    public void updateTour(Tour tour) {
        repository.updateTour(tour);
    }

    public void updateCurrentLocation(GeoPoint gp) {
        currentLocation = gp;
    }

    public GeoPoint getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentZoom(double zoomLevelDouble) {
        currentZoomLevel = zoomLevelDouble;
    }

    public double getCurrentZoomLevel() {
        return currentZoomLevel;
    }
}
