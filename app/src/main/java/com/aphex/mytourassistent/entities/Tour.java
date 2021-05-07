package com.aphex.mytourassistent.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

public class Tour {

    @PrimaryKey(autoGenerate = true)
    public long tourId;

    public String title;
    public String dateForTour;
    public String comment;

    public int nrOfDays;
    public int nrOfHours;
    public boolean isCompleted;
    public boolean isStarted;
    public String tourType;

    public Tour(@NonNull String title, @NonNull String dateForTour,
                int nrOfDays, int nrOfHours, @NonNull String tourType, boolean isCompleted,
                boolean isStarted) {
        this.title = title;
        this.dateForTour = dateForTour;
        this.nrOfDays = nrOfDays;
        this.nrOfHours = nrOfHours;
        this.tourType = tourType;
        this.isCompleted = isCompleted;
        this.isStarted = isStarted;
    }
}
