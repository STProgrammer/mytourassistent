package com.aphex.mytourassistent.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.aphex.mytourassistent.entities.Tour;
import com.aphex.mytourassistent.entities.TourWithGeoPointsPlanned;

import java.util.List;

@Dao
public abstract class ToursDAO {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(Tour tour);

    @Transaction
    @Query("SELECT * FROM Tour")
    public abstract List<Tour> getAll();


    @Transaction
    @Query("SELECT * FROM Tour JOIN GeoPointPlanned ON GeoPointPlanned.fk_tourId = Tour.tourId")
    public abstract List<TourWithGeoPointsPlanned> getAllToursWithGeopoints();

}
