package com.aphex.mytourassistent.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.aphex.mytourassistent.entities.Tour;
import com.aphex.mytourassistent.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.entities.TourWithGeoPointsPlanned;

import java.util.List;

@Dao
public abstract class ToursDAO {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(Tour tour);

    @Transaction
    @Query("SELECT * FROM Tour WHERE Tour.tourStatus IS NOT 4")
    public abstract LiveData<List<Tour>> getAllUncompletedTours();

    @Transaction
    @Query("SELECT * FROM Tour WHERE Tour.tourStatus = 4")
    public abstract LiveData<List<Tour>> getAllCompletedTours();


    @Transaction
    @Query("SELECT * FROM Tour JOIN GeoPointPlanned ON GeoPointPlanned.fk_tourId = Tour.tourId")
    public abstract List<TourWithGeoPointsPlanned> getAllToursWithGeopoints();

    @Transaction
    @Query("SELECT * FROM Tour WHERE Tour.tourId = :tourId")
    public abstract LiveData<Tour> getTour(long tourId);

    @Transaction
    @Query("SELECT * FROM Tour WHERE Tour.tourId = :tourId")
    public abstract LiveData<TourWithAllGeoPoints> getTourWithAllGeoPoints(long tourId);


    @Transaction
    @Query("DELETE FROM Tour WHERE Tour.tourId = :tourId")
    public abstract void delete(long tourId);

    @Transaction
    @Query("UPDATE Tour SET startTimeActual = :startTime, tourStatus = :status WHERE Tour.tourId = :tourId")
    public abstract void startTour(long tourId, long startTime, int status);

    @Transaction
    @Update
    public abstract void update(Tour tour);


    // public abstract void startTour(long tourId);
}
