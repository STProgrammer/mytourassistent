package com.aphex.mytourassistent.activetour;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aphex.mytourassistent.entities.GeoPointPlanned;
import com.aphex.mytourassistent.entities.Tour;
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

    private Repository repository;


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


    public void addNewTour(String tourName, long startTime, long endTime, String tourType, String tourStatus) {
        repository.addTour(tourName, startTime, endTime, tourType, tourStatus, geoPointsPlanned.getValue());
    }

    public LiveData<List<Tour>> getAllTours() {
        return repository.getAllTours();
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
}
