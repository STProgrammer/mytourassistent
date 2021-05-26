package com.aphex.mytourassistent.repository.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.aphex.mytourassistent.repository.db.entities.Photo;


import java.util.List;

@Dao
public abstract class PhotoDAO {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(Photo photo);

    @Transaction
    @Update
    public abstract void update(Photo photo);

    @Transaction
    @Query("SELECT * FROM Photo WHERE Photo.fk_geoPointActualId = :geoPointId")
    public abstract List<Photo> getPhotos(long geoPointId);

}
