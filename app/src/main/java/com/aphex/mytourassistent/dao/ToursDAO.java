package com.aphex.mytourassistent.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Transaction;

import com.aphex.mytourassistent.entities.Tour;

@Dao
public abstract class ToursDAO {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(Tour tour);
}