
package com.aphex.mytourassistent.repository.network.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Instant {

    @SerializedName("details")
    @Expose
    private Details details;

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

}
