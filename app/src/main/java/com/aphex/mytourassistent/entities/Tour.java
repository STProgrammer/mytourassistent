package com.aphex.mytourassistent.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = User.class, parentColumns="userId", childColumns = "fk_userId", onDelete = ForeignKey.CASCADE)
})
public class Tour {

    @PrimaryKey(autoGenerate = true)
    public long tourId;

    public long fk_userId;
    public String title;
    public String dateForTour;
    public String comment;

    public int nrOfDays;
    public int nrOfHours;
    public boolean isCompleted;
    public boolean isStarted;
    public String tourType;

    public Tour(long fk_userId, @NonNull String title, @NonNull String dateForTour,
                int nrOfDays, int nrOfHours, @NonNull String tourType, boolean isCompleted,
                boolean isStarted) {
        this.fk_userId = fk_userId;
        this.title = title;
        this.dateForTour = dateForTour;
        this.nrOfDays = nrOfDays;
        this.nrOfHours = nrOfHours;
        this.tourType = tourType;
        this.isCompleted = isCompleted;
        this.isStarted = isStarted;
    }
}
