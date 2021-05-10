package com.aphex.mytourassistent.repository;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aphex.mytourassistent.dao.GeoPointsActualDAO;
import com.aphex.mytourassistent.dao.GeoPointsPlannedDAO;
import com.aphex.mytourassistent.dao.ToursDAO;
import com.aphex.mytourassistent.db.MyTourAssistentDatabase;
import com.aphex.mytourassistent.entities.GeoPointActual;
import com.aphex.mytourassistent.entities.GeoPointPlanned;
import com.aphex.mytourassistent.entities.Tour;
import com.aphex.mytourassistent.entities.TourWithAllGeoPoints;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    private static Repository repository;

    private ToursDAO toursDAO;
    private GeoPointsPlannedDAO geoPointsPlannedDAO;
    private GeoPointsActualDAO geoPointsActualDAO;

    private LiveData<List<Tour>> toursList;

    private LiveData<List<GeoPointPlanned>> geoPointsPlanned;

    private LiveData<TourWithAllGeoPoints> tourWithAllGeoPoints;


    public Repository(Application application) {
        MyTourAssistentDatabase db = MyTourAssistentDatabase.getDatabase(application);
        toursDAO = db.toursDAO();
        geoPointsPlannedDAO = db.geoPointsPlannedDAO();
        geoPointsActualDAO = db.geoPointsActualDAO();
        toursList = new MutableLiveData<>();
        geoPointsPlanned = new MutableLiveData<>();
        tourWithAllGeoPoints = new MutableLiveData<>();

    }

    public static Repository getInstance(Application application) {
        if (repository == null) {
            repository = new Repository(application);
        }
        return repository;
    }


    public void addTour(String tourName, long startTime, long endTime, int tourType, int tourStatus, ArrayList<GeoPoint> geoPoints) {

        MyTourAssistentDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long tourId = toursDAO.insert(new Tour(tourName, startTime, endTime, -1, -1, "", tourStatus, tourType));
                long order = 1;
                for (GeoPoint gp : geoPoints) {
                    geoPointsPlannedDAO.insert(new GeoPointPlanned(gp.getLatitude(), gp.getLongitude(), tourId, order));
                    order++;
                    //update livedata here
                    //TODO return some flag that data is successfully inserted
                }
            } catch (SQLiteConstraintException e) {

            }

        });
    }


    public LiveData<List<Tour>> getAllTours(boolean mIsFirstTime) {
        if (!mIsFirstTime) {
            return toursList;
        }
        toursList = toursDAO.getAll();

        return toursList;

        //they will observe live data value from here
    }

    public LiveData<List<GeoPointPlanned>> getGeoPointsPlanned(long tourId, boolean mIsFirstTime) {
        if (!mIsFirstTime) {
            return geoPointsPlanned;
        }
        //     MyTourAssistentDatabase.databaseWriteExecutor.execute(()->{
        geoPointsPlanned = geoPointsPlannedDAO.getGeoPointsPlanned(tourId);
        //   });
        return geoPointsPlanned;

    }

    public MutableLiveData<Tour> getTour(long tourId) {
        return new MutableLiveData<Tour>();

    }

    public LiveData<TourWithAllGeoPoints> getTourWithAllGeoPoints(long tourId, boolean mIsFirstTime) {
        if (!mIsFirstTime) {
            return tourWithAllGeoPoints;
        }
        //MyTourAssistentDatabase.databaseWriteExecutor.execute(()-> {
        tourWithAllGeoPoints = toursDAO.getTourWithAllGeoPoints(tourId);
        //});
        return tourWithAllGeoPoints;
    }

    public void deleteTour(long tourId) {
        MyTourAssistentDatabase.databaseWriteExecutor.execute(() -> {
            toursDAO.delete(tourId);
        });

    }

    public void addGeoPointActual(GeoPointActual gpa) {
        MyTourAssistentDatabase.databaseWriteExecutor.execute(() -> {
            try {
                geoPointsActualDAO.insert(gpa);
            } catch (SQLiteConstraintException e) {
            }
        });

    }

    public void startTour(long tourId, long startTime, int status) {
        MyTourAssistentDatabase.databaseWriteExecutor.execute(() -> {
            try {
                toursDAO.startTour(tourId, startTime, status);
            } catch (SQLiteConstraintException e) {
            }
        });

    }

    public void clearGeoPoints(long tourId) {
        MyTourAssistentDatabase.databaseWriteExecutor.execute(() -> {
            geoPointsActualDAO.clear(tourId);
        });

    }

    public void updateTour(Tour tour) {
        MyTourAssistentDatabase.databaseWriteExecutor.execute(()-> {
            toursDAO.update(tour);
        });
    }
}
