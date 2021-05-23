
package com.aphex.mytourassistent.repository.network.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Units {

    @SerializedName("air_pressure_at_sea_level")
    @Expose
    private String airPressureAtSeaLevel;
    @SerializedName("air_temperature")
    @Expose
    private String airTemperature;
    @SerializedName("cloud_area_fraction")
    @Expose
    private String cloudAreaFraction;
    @SerializedName("precipitation_amount")
    @Expose
    private String precipitationAmount;
    @SerializedName("relative_humidity")
    @Expose
    private String relativeHumidity;
    @SerializedName("wind_from_direction")
    @Expose
    private String windFromDirection;
    @SerializedName("wind_speed")
    @Expose
    private String windSpeed;

    public String getAirPressureAtSeaLevel() {
        return airPressureAtSeaLevel;
    }

    public void setAirPressureAtSeaLevel(String airPressureAtSeaLevel) {
        this.airPressureAtSeaLevel = airPressureAtSeaLevel;
    }

    public String getAirTemperature() {
        return airTemperature;
    }

    public void setAirTemperature(String airTemperature) {
        this.airTemperature = airTemperature;
    }

    public String getCloudAreaFraction() {
        return cloudAreaFraction;
    }

    public void setCloudAreaFraction(String cloudAreaFraction) {
        this.cloudAreaFraction = cloudAreaFraction;
    }

    public String getPrecipitationAmount() {
        return precipitationAmount;
    }

    public void setPrecipitationAmount(String precipitationAmount) {
        this.precipitationAmount = precipitationAmount;
    }

    public String getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(String relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public String getWindFromDirection() {
        return windFromDirection;
    }

    public void setWindFromDirection(String windFromDirection) {
        this.windFromDirection = windFromDirection;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

}
