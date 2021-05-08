package com.aphex.mytourassistent.tours;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.aphex.mytourassistent.repository.Repository;
import com.aphex.mytourassistent.entities.GeoPointPlanned;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;

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

    public ArrayList<GeoPoint> getGeoPoints() {
        return geoPoints.getValue();
    }


    public void addNewTour(String tourName, long startTime, long endTime, String tourType) {
        repository.addTour(tourName, startTime, endTime, tourType, geoPoints.getValue());
    }
}
