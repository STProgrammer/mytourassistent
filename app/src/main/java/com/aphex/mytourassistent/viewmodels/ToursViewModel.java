package com.aphex.mytourassistent.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.aphex.mytourassistent.repository.db.entities.Tour;
import com.aphex.mytourassistent.repository.db.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.repository.db.entities.TourWithGeoPointsActual;
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

    private long currentActiveTour = -1;

    private MutableLiveData<ArrayList<GeoPoint>> geoPointsPlanning;
    private MutableLiveData<ArrayList<GeoPoint>> geoPointsOnActive;
    private MutableLiveData<ArrayList<GeoPoint>> geoPointsOnCompleted;

    private Repository repository;


    public ToursViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        geoPointsPlanning = new MutableLiveData<>();
        geoPointsPlanning.setValue(new ArrayList<>());
        geoPointsOnActive = new MutableLiveData<>();
        geoPointsOnActive.setValue(new ArrayList<>());
        geoPointsOnCompleted = new MutableLiveData<>();
        geoPointsOnCompleted.setValue(new ArrayList<>());
    }


    public MutableLiveData<ArrayList<GeoPoint>> getGeoPointsOnCompleted() {
        return geoPointsOnCompleted;
    }

    public void addToGeoPointsOnCompleted(GeoPoint gp) {
        geoPointsOnCompleted.getValue().add(gp);
    }

    public LiveData<List<Tour>> getAllUncompletedTours(boolean mIsFirstTime) {
        return repository.getAllUncompletedTours(mIsFirstTime);
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

    public LiveData<TourWithGeoPointsActual> getTourWithGeoPointsActual(long tourId, boolean mIsFirstTime) {
        return repository.getTourWithGeoPointsActual(tourId, mIsFirstTime);
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

    public void addComment(String comment, Tour tour) {
        tour.comment = comment;
        repository.updateTour(tour);
    }

    public void setActivityTourId(long tourId) {
        repository.setActivityTourId(tourId);
    }
}
