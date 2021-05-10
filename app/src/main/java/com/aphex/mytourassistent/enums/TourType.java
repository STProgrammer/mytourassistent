package com.aphex.mytourassistent.enums;

import org.jetbrains.annotations.Contract;

public enum TourType {
    WALKING(1),
    BIKING(2),
    SKIING(3);

    int tourType = 0;
    TourType(int tourType) {
        this.tourType = tourType;
    }


    public int getValue(){
     return tourType;
    }
}