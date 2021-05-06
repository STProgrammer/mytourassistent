package com.aphex.mytourassistent.entities;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {

    @PrimaryKey(autoGenerate = true)
    public long userId;

    public String name;
    public String email;

    public User(@NonNull String name, @NonNull String email) {
        this.name = name;
        this.email = email;
    }
}
