package com.aphex.mytourassistent.repository.db.entities;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = GeoPointActual.class, parentColumns="geoPointActualId", childColumns = "fk_geoPointActualId", onDelete = ForeignKey.CASCADE)
})
public class Photo {

    @PrimaryKey(autoGenerate = true)
    public long photoId;

    public long fk_geoPointActualId;
    public String imageUri;

    public Photo(long fk_geoPointActualId, String imageUri) {
        this.fk_geoPointActualId = fk_geoPointActualId;
        this.imageUri = imageUri;
    }
}
