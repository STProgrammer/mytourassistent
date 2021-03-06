package com.aphex.mytourassistent.repository.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.aphex.mytourassistent.repository.db.entities.Tour;
import com.aphex.mytourassistent.repository.db.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.repository.db.entities.TourWithGeoPointsActual;
import com.aphex.mytourassistent.repository.db.entities.TourWithGeoPointsPlanned;

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
    @Query("SELECT * FROM Tour WHERE Tour.tourId = :tourId")
    public abstract TourWithAllGeoPoints getTourWithAllGeoPoints(long tourId);


    @Transaction
    @Query("SELECT * FROM Tour WHERE Tour.tourId = :tourId")
    public abstract LiveData<TourWithGeoPointsActual> getTourWithGeoPointsActual(long tourId);

    @Transaction
    @Query("SELECT * FROM Tour JOIN GeoPointPlanned ON GeoPointPlanned.fk_tourId = Tour.tourId")
    public abstract List<TourWithGeoPointsPlanned> getAllToursWithGeopoints();

    @Transaction
    @Query("DELETE FROM Tour WHERE Tour.tourId = :tourId")
    public abstract void delete(long tourId);

    @Transaction
    @Update
    public abstract void update(Tour tour);

    @Transaction
    @Query("DELETE FROM Tour WHERE 1")
    public abstract void deleteAll();

}
