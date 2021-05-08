package com.aphex.mytourassistent.repository;

import android.app.Application;

import com.aphex.mytourassistent.dao.GeoPointsActualDAO;
import com.aphex.mytourassistent.dao.GeoPointsPlannedDAO;
import com.aphex.mytourassistent.dao.ToursDAO;
import com.aphex.mytourassistent.db.MyTourAssistentDatabase;
import com.aphex.mytourassistent.entities.GeoPointPlanned;
import com.aphex.mytourassistent.entities.Tour;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class Repository {
    private static Repository repository;

    private ToursDAO toursDAO;
    private GeoPointsPlannedDAO geoPointsPlannedDAO;
    private GeoPointsActualDAO geoPointsActualDAO;

    public Repository(Application application) {
        MyTourAssistentDatabase db = MyTourAssistentDatabase.getDatabase(application);
        toursDAO = db.toursDAO();
        geoPointsPlannedDAO = db.geoPointsPlannedDAO();
        geoPointsActualDAO = db.geoPointsActualDAO();

    }

    public static Repository getInstance(Application application){
        if (repository == null) {
            repository = new Repository(application);
        }
        return repository;
    }


    public void addTour(String tourName, long startTime, long endTime, String tourType, ArrayList<GeoPoint> geoPoints) {

        MyTourAssistentDatabase.databaseWriteExecutor.execute(()-> {
            long tourId = toursDAO.insert(new Tour(tourName, startTime, endTime, -1,-1, "", false,false, tourType));
            long order = 1;
            for (GeoPoint gp: geoPoints) {
                geoPointsPlannedDAO.insert(new GeoPointPlanned(gp.getLatitude(), gp.getLongitude(), tourId, order));
                order++;
                //update livedata here
                //TODO return some flag that data is successfully inserted

            }
        });

    }
}
