package com.aphex.mytourassistent.repository.db.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;


public class TourWithGeoPointsActual {
    @Embedded
    public Tour tour;
    @Relation(
            entity = GeoPointActual.class,
            parentColumn = "tourId",
            entityColumn = "fk_tourId"
    )
    public List<GeoPointPlanned> geoPointsActual;

    public TourWithGeoPointsActual() {}

}
