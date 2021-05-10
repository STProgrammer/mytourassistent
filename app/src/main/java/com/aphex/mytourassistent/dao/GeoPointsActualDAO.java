package com.aphex.mytourassistent.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.aphex.mytourassistent.entities.GeoPointActual;

@Dao
public abstract class GeoPointsActualDAO {

    @Insert
    public abstract void insert(GeoPointActual gpa);

    @Transaction
    @Query("DELETE FROM GeoPointActual WHERE GeoPointActual.fk_tourId = :tourId")
    public abstract void clear(long tourId);
}
