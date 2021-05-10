package com.aphex.mytourassistent.dao;

import androidx.room.Dao;
import androidx.room.Insert;

import com.aphex.mytourassistent.entities.GeoPointActual;

@Dao
public abstract class GeoPointsActualDAO {

    @Insert
    public abstract void insert(GeoPointActual gpa);

}
