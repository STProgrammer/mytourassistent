package com.aphex.mytourassistent.repository.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.aphex.mytourassistent.repository.db.entities.GeoPointPlanned;


import java.util.List;

@Dao
public abstract class GeoPointsPlannedDAO {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(GeoPointPlanned geoPointPlanned);

    @Transaction
    @Query("SELECT * FROM GeoPointPlanned WHERE GeoPointPlanned.fk_tourId = :tourId ORDER BY GeoPointPlanned.travelOrder")
    public abstract LiveData<List<GeoPointPlanned>> getGeoPointsPlanned(long tourId);
}
