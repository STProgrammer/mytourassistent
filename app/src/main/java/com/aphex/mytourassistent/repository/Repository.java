package com.aphex.mytourassistent.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aphex.mytourassistent.dao.GeoPointsActualDAO;
import com.aphex.mytourassistent.dao.GeoPointsPlannedDAO;
import com.aphex.mytourassistent.dao.ToursDAO;
import com.aphex.mytourassistent.db.MyTourAssistentDatabase;
import com.aphex.mytourassistent.entities.GeoPointPlanned;
import com.aphex.mytourassistent.entities.Tour;
import com.aphex.mytourassistent.entities.TourWithGeoPointsActual;
import com.aphex.mytourassistent.entities.TourWithGeoPointsPlanned;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    private static Repository repository;

    private ToursDAO toursDAO;
    private GeoPointsPlannedDAO geoPointsPlannedDAO;
    private GeoPointsActualDAO geoPointsActualDAO;

    private MutableLiveData<List<TourWithGeoPointsPlanned>> plannedTour;


    public Repository(Application application) {
        MyTourAssistentDatabase db = MyTourAssistentDatabase.getDatabase(application);
        toursDAO = db.toursDAO();
        geoPointsPlannedDAO = db.geoPointsPlannedDAO();
        geoPointsActualDAO = db.geoPointsActualDAO();
        plannedTour = new MutableLiveData<>();

    }

    public static Repository getInstance(Application application){
        if (repository == null) {
            repository = new Repository(application);
        }
        return repository;
    }


    public void addTour(String tourName, long startTime, long endTime, String tourType, String tourStatus, ArrayList<GeoPoint> geoPoints) {

        MyTourAssistentDatabase.databaseWriteExecutor.execute(()-> {
            long tourId = toursDAO.insert(new Tour(tourName, startTime, endTime, -1,-1, "", tourStatus, tourType));
            long order = 1;
            for (GeoPoint gp: geoPoints) {
                geoPointsPlannedDAO.insert(new GeoPointPlanned(gp.getLatitude(), gp.getLongitude(), tourId, order));
                order++;
                //update livedata here
                //TODO return some flag that data is successfully inserted
            }
        });

    }
//TODO: Why LiveData doesn't work, why DAO can't return LiveData
    public LiveData<List<TourWithGeoPointsPlanned>> getAllTours() {
        MyTourAssistentDatabase.databaseWriteExecutor.execute(()->{
            List<TourWithGeoPointsPlanned> tourList = toursDAO.getAllToursWithGeopoints();
            if (tourList == null) {
                plannedTour.postValue(new ArrayList<TourWithGeoPointsPlanned>());
            } else {
                plannedTour.postValue(tourList);
            }

           // when they receive value here
            //set the value to live
            //post livedata inside livedata.
            //
        });
        return plannedTour;

        //they will observe live data value from here
    }
}
