package com.aphex.mytourassistent.tours;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aphex.mytourassistent.entities.Tour;
import com.aphex.mytourassistent.entities.TourWithGeoPointsPlanned;
import com.aphex.mytourassistent.repository.Repository;
import com.aphex.mytourassistent.entities.GeoPointPlanned;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ToursViewModel extends AndroidViewModel {

    private Calendar mCalendarStart;
    private Calendar mCalendarFinish;
    private String tourName;
    private String tourType;

    private MutableLiveData<ArrayList<GeoPoint>> geoPoints;
    private MutableLiveData<ArrayList<GeoPointPlanned>> plannedGeoPoints;

    private Repository repository;


    public ToursViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        geoPoints = new MutableLiveData<>();
        geoPoints.setValue(new ArrayList<>());
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

    public LiveData<List<Tour>> getAllTours(boolean mIsFirstTime) {
        return repository.getAllTours(mIsFirstTime);
    }

    public void deleteTour(long tourId) {
        repository.deleteTour(tourId);
    }
}
