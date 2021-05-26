package com.aphex.mytourassistent.repository.db.entities;


import androidx.room.Embedded;

import androidx.room.Relation;

import java.util.List;


public class GeoPointActualWithPhotos {

    @Embedded
    public GeoPointActual geoPointActual;
    @Relation(
            entity = Photo.class,
            parentColumn = "geoPointActualId",
            entityColumn = "fk_geoPointActualId"
    )

    public List<Photo> photos;

    public GeoPointActualWithPhotos() {}
}


