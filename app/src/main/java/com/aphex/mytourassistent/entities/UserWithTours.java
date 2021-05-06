package com.aphex.mytourassistent.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class UserWithTours {
    @Embedded
    public User user;
    @Relation(
            entity = Tour.class,
            parentColumn = "userId",
            entityColumn = "fk_userId"
    )
    public List<Tour> tours;

    public UserWithTours() {}
}
