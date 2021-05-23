package com.aphex.mytourassistent.repository.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = Tour.class, parentColumns="tourId", childColumns = "fk_tourId", onDelete = ForeignKey.CASCADE)
})
public class GeoPointPlanned {

    @PrimaryKey(autoGenerate = true)
    public long geoPointPlannedId;

    public long fk_tourId;

    public double lat;
    public double lng;

    public long travelOrder;

    public GeoPointPlanned(double lat, double lng, long fk_tourId, long travelOrder) {
        this.lat = lat;
        this.lng = lng;
        this.fk_tourId = fk_tourId;
        this.travelOrder = travelOrder;
    }
}