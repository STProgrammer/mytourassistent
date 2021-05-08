package com.aphex.mytourassistent.entities;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Relation;

import java.util.List;


public class TourWithGeoPointsPlanned {
    @Embedded
    public Tour tour;
    @Relation(
            entity = GeoPointPlanned.class,
            parentColumn = "tourId",
            entityColumn = "fk_tourId"
    )
    public List<GeoPointPlanned> geoPointsPlanned;

    public TourWithGeoPointsPlanned() {}
}
