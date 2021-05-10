package com.aphex.mytourassistent.enums;

public enum TourStatus {
    NOT_STARTED(1),
    ACTIVE(2),
    PAUSED(3),
    COMPLETED(4);

    int tourStatus = 0;
    TourStatus(int status) {
        tourStatus = status;
    }
    public int getValue(){
     return tourStatus;
    }
}
