package com.aphex.mytourassistent.repository.network.api;

import com.aphex.mytourassistent.repository.network.models.WeatherApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;


public interface WeatherAPI {


    @GET("/weatherapi/locationforecast/2.0/compact")
    @Headers({
            "Accept: application/json",
            "User-Agent: MyTourAssistent"
    })
    Call<WeatherApiResponse> getWeatherData(@Query("lat") String lat, @Query("lon") String lon);
}
