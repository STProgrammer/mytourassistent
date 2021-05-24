package com.aphex.mytourassistent.repository.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.aphex.mytourassistent.repository.db.entities.Photo;
import com.aphex.mytourassistent.repository.db.entities.Tour;
import com.aphex.mytourassistent.repository.db.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.repository.db.entities.TourWithGeoPointsPlanned;

import java.util.List;

@Dao
public abstract class PhotoDAO {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(Photo photo);

    @Transaction
    @Query("SELECT * FROM Photo WHERE 1")
    public abstract LiveData<List<Photo>> getAllPhotos();

    @Transaction
    @Update
    public abstract void update(Photo photo);


    // public abstract void startTour(long tourId);
}
