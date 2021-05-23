package com.aphex.mytourassistent.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aphex.mytourassistent.repository.db.entities.Tour;
import com.aphex.mytourassistent.repository.db.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.repository.network.models.Data;
import com.aphex.mytourassistent.repository.Repository;
import com.aphex.mytourassistent.repository.db.entities.GeoPointPlanned;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ToursViewModel extends AndroidViewModel {



    private Calendar mCalendarStart;
    private Calendar mCalendarFinish;
    private String tourName;
    private String tourType;



    private long currentActiveTour = -1;



    private MutableLiveData<GeoPoint> firstGeoPoint;
    private MutableLiveData<GeoPoint> lastGeoPoint;

    private MutableLiveData<ArrayList<GeoPoint>> geoPoints;
    private MutableLiveData<ArrayList<GeoPointPlanned>> plannedGeoPoints;
    private KmlDocument kmlDocument;




    private Repository repository;



    public ToursViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        geoPoints = new MutableLiveData<>();
        geoPoints.setValue(new ArrayList<>());
        kmlDocument = new KmlDocument();
        firstGeoPoint = new MutableLiveData<>();
        lastGeoPoint = new MutableLiveData<>();
    }

    public void addToGeoPoints(GeoPoint gp) {
        geoPoints.getValue().add(gp);
    }

    public LiveData<ArrayList<GeoPoint>> getGeoPoints() {
        return geoPoints;
    }


    public void addNewTour(String tourName, long startTime, long endTime, int tourType, int tourStatus) {
        repository.addTour(tourName, startTime, endTime, tourType, tourStatus, geoPoints.getValue());
    }

    public LiveData<List<Tour>> getAllUncompletedTours(boolean mIsFirstTime) {
        LiveData<List<Tour>> tours = repository.getAllUncompletedTours(mIsFirstTime);
        return tours;
    }

    public void deleteTour(long tourId) {
        repository.deleteTour(tourId);
    }

    public LiveData<List<Tour>> getAllCompletedTours(boolean mIsFirstTime) {
        return repository.getAllCompletedTours(mIsFirstTime);
    }

    public LiveData<TourWithAllGeoPoints> getTourWithAllGeoPoints(long tourId, boolean mIsFirstTime) {
        return repository.getTourWithAllGeoPoints(tourId, mIsFirstTime);
    }

    public KmlDocument getKmlDocument() {
        return kmlDocument;
    }

    public void setKmlDocument(KmlDocument kmlDocument) {
        this.kmlDocument = kmlDocument;
    }

    public LiveData<Integer> getStatusInteger() {
        return repository.getStatusInteger();
    }

    public long getCurrentActiveTour() {
        return currentActiveTour;
    }

    public void setCurrentActiveTour(List<Tour> tours) {
        for (Tour tour: tours) {
            if (tour.tourStatus == 2) {
                currentActiveTour = tour.tourId;
                break;
            }
            currentActiveTour = -1;
        }
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
        Date time = isFirstGp ? mCalendarStart.getTime(): mCalendarFinish.getTime();
         repository.getWeatherData(latitude, longitude, time, isFirstGp);
    }

    public Calendar getmCalendarStart() {
        return mCalendarStart;
    }

    public void setmCalendarStart(Calendar mCalendarStart) {
        this.mCalendarStart = mCalendarStart;
    }

    public Calendar getmCalendarFinish() {
        return mCalendarFinish;
    }

    public void setmCalendarFinish(Calendar mCalendarFinish) {
        this.mCalendarFinish = mCalendarFinish;
    }

    public LiveData<Data> getFirstWeatherGeopointResponse() {
        return repository.getFirstWeatherLiveData();
    }

    public LiveData<Data> getLastWeatherGeopointResponse() {
        return repository.getLastWeatherLiveData();
    }
}