package com.aphex.mytourassistent.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aphex.mytourassistent.repository.Repository;
import com.aphex.mytourassistent.repository.db.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.repository.network.models.Data;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddTourViewModel extends AndroidViewModel {


    private Calendar mCalendarStart;
    private Calendar mCalendarFinish;

    private int tourType = -1;

    private MutableLiveData<GeoPoint> firstGeoPoint;
    private MutableLiveData<GeoPoint> lastGeoPoint;
    private MutableLiveData<ArrayList<GeoPoint>> geoPointsPlanning;

    private Repository repository;


    public AddTourViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        init();
    }

    public void addToGeoPointsPlanning(GeoPoint gp) {
        geoPointsPlanning.getValue().add(gp);
    }

    public LiveData<ArrayList<GeoPoint>> getGeoPointsPlanning() {
        return geoPointsPlanning;
    }


    public void addNewTour(String tourName, long startTime, long endTime, int tourType, int tourStatus) {
        repository.addTour(tourName, startTime, endTime, tourType, tourStatus, geoPointsPlanning.getValue());
    }

    public LiveData<TourWithAllGeoPoints> getTourWithAllGeoPoints(long tourId, boolean mIsFirstTime) {
        return repository.getTourWithAllGeoPoints(tourId, mIsFirstTime);
    }


    public LiveData<Integer> getStatusOnAddTour() {
        return repository.getStatusInteger();
    }

    public MutableLiveData<GeoPoint> getFirstGeoPoint() {
        return firstGeoPoint;
    }

    public void setFirstGeoPoint(MutableLiveData<GeoPoint> firstGeoPoint) {
        this.firstGeoPoint = firstGeoPoint;
    }

    public MutableLiveData<GeoPoint> getLastGeoPoint() {
        return lastGeoPoint;
    }

    public void setLastGeoPoint(MutableLiveData<GeoPoint> lastGeoPoint) {
        this.lastGeoPoint = lastGeoPoint;
    }

    public void getWeatherData(double latitude, double longitude, boolean isFirstGp) {
        Date time = isFirstGp ? mCalendarStart.getTime() : mCalendarFinish.getTime();
        repository.getWeatherData(latitude, longitude, time, isFirstGp);
    }

    public Calendar getCalendarStart() {
        return mCalendarStart;
    }

    public void setCalendarStart(Calendar mCalendarStart) {
        this.mCalendarStart = mCalendarStart;
    }

    public Calendar getCalendarFinish() {
        return mCalendarFinish;
    }

    public void setCalendarFinish(Calendar mCalendarFinish) {
        this.mCalendarFinish = mCalendarFinish;
    }

    public LiveData<Data> getFirstWeatherGeoPointResponse() {
        return repository.getFirstWeatherLiveData();
    }

    public LiveData<Data> getLastWeatherGeoPointResponse() {
        return repository.getLastWeatherLiveData();
    }

    public int getTourType() {
        return tourType;
    }

    public void setTourType(int tourType) {
        this.tourType = tourType;
    }

    public void init() {
        geoPointsPlanning = new MutableLiveData<>();
        geoPointsPlanning.setValue(new ArrayList<>());
        firstGeoPoint = new MutableLiveData<>();
        lastGeoPoint = new MutableLiveData<>();
        mCalendarStart = null;
        mCalendarFinish = null;
        tourType = -1;
    }

}
