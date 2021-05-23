
package com.aphex.mytourassistent.repository.network.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Details {

    @SerializedName("air_pressure_at_sea_level")
    @Expose
    private Double airPressureAtSeaLevel;
    @SerializedName("air_temperature")
    @Expose
    private Double airTemperature;
    @SerializedName("cloud_area_fraction")
    @Expose
    private Double cloudAreaFraction;
    @SerializedName("relative_humidity")
    @Expose
    private Double relativeHumidity;
    @SerializedName("wind_from_direction")
    @Expose
    private Double windFromDirection;
    @SerializedName("wind_speed")
    @Expose
    private Double windSpeed;

    public Double getAirPressureAtSeaLevel() {
        return airPressureAtSeaLevel;
    }

    public void setAirPressureAtSeaLevel(Double airPressureAtSeaLevel) {
        this.airPressureAtSeaLevel = airPressureAtSeaLevel;
    }

    public Double getAirTemperature() {
        return airTemperature;
    }

    public void setAirTemperature(Double airTemperature) {
        this.airTemperature = airTemperature;
    }

    public Double getCloudAreaFraction() {
        return cloudAreaFraction;
    }

    public void setCloudAreaFraction(Double cloudAreaFraction) {
        this.cloudAreaFraction = cloudAreaFraction;
    }

    public Double getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(Double relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public Double getWindFromDirection() {
        return windFromDirection;
    }

    public void setWindFromDirection(Double windFromDirection) {
        this.windFromDirection = windFromDirection;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

}
