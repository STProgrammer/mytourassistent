
package com.aphex.mytourassistent.repository.network.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Details__1 {

    @SerializedName("precipitation_amount")
    @Expose
    private Double precipitationAmount;

    public Double getPrecipitationAmount() {
        return precipitationAmount;
    }

    public void setPrecipitationAmount(Double precipitationAmount) {
        this.precipitationAmount = precipitationAmount;
    }

}
