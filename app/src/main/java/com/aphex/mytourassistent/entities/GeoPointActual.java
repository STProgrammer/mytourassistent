package com.aphex.mytourassistent.entities;


import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = Tour.class, parentColumns="tourId", childColumns = "fk_tourId", onDelete = ForeignKey.CASCADE)
})
public class GeoPointActual {

    @PrimaryKey(autoGenerate = true)
    public long geoPointActualId;

    public long fk_tourId;

    public double lat;
    public double lng;
    public String imageUri;

    public long travelOrder;

    public GeoPointActual(double lat, double lng, long fk_tourId, long travelOrder, @Nullable String imageUri) {
        this.lat = lat;
        this.lng = lng;
        this.fk_tourId = fk_tourId;
        this.travelOrder = travelOrder;
        this.imageUri = imageUri;
    }
}
//background service
//foreground service->let u do anything continuesly
//user starts tour
// we show him notification that tour is in progress

