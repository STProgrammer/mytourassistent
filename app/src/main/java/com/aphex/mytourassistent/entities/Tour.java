package com.aphex.mytourassistent.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity
public class Tour {

    @PrimaryKey(autoGenerate = true)
    public long tourId;

    public String title;
    public long startTimePlanned;
    public long finishTimePlanned;
    public long startTimeActual;
    public long finishTimeActual;
    public String comment;

    public String tourStatus;
    public String tourType;


    public Tour(String title, long startTimePlanned, long finishTimePlanned, long startTimeActual, long finishTimeActual, String comment, String tourStatus, String tourType) {
        this.title = title;
        this.startTimePlanned = startTimePlanned;
        this.finishTimePlanned = finishTimePlanned;
        this.startTimeActual = startTimeActual;
        this.finishTimeActual = finishTimeActual;
        this.comment = comment;
        this.tourType = tourType;
        this.tourStatus = tourStatus;
    }
}
