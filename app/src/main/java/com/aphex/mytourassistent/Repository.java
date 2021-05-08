package com.aphex.mytourassistent;

import android.app.Application;

import com.aphex.mytourassistent.dao.GeoPointsActualDAO;
import com.aphex.mytourassistent.dao.GeoPointsPlannedDAO;
import com.aphex.mytourassistent.dao.ToursDAO;
import com.aphex.mytourassistent.db.MyTourAssistentDatabase;

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


}
