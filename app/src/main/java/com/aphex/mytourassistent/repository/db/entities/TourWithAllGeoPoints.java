package com.aphex.mytourassistent.repository.db.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;


public class TourWithAllGeoPoints {
    @Embedded
    public Tour tour;
    @Relation(
            entity = GeoPointActual.class,
            parentColumn = "tourId",
            entityColumn = "fk_tourId"
    )
    public List<GeoPointActual> geoPointsActual;

    @Relation(
            entity = GeoPointPlanned.class,
            parentColumn = "tourId",
            entityColumn = "fk_tourId"
    )
    public List<GeoPointPlanned> geoPointsPlanned;

    public TourWithAllGeoPoints() {}

}
