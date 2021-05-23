package com.aphex.mytourassistent.repository;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aphex.mytourassistent.repository.network.api.WeatherAPI;
import com.aphex.mytourassistent.repository.db.dao.GeoPointsActualDAO;
import com.aphex.mytourassistent.repository.db.dao.GeoPointsPlannedDAO;
import com.aphex.mytourassistent.repository.db.dao.ToursDAO;
import com.aphex.mytourassistent.repository.db.MyTourAssistentDatabase;
import com.aphex.mytourassistent.repository.db.entities.GeoPointActual;
import com.aphex.mytourassistent.repository.db.entities.GeoPointPlanned;
import com.aphex.mytourassistent.repository.db.entities.Tour;
import com.aphex.mytourassistent.repository.db.entities.TourWithAllGeoPoints;
import com.aphex.mytourassistent.repository.network.models.Data;
import com.aphex.mytourassistent.repository.network.models.Timeseries;
import com.aphex.mytourassistent.repository.network.models.WeatherApiResponse;

import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Repository {
    private static final String BASE_URL = "https://api.met.no";
    private static Repository repository;
    private final Retrofit retrofit;
    private final WeatherAPI weatherApi;

    private ToursDAO toursDAO;
    private GeoPointsPlannedDAO geoPointsPlannedDAO;
    private GeoPointsActualDAO geoPointsActualDAO;

    private LiveData<List<Tour>> toursList;
    private LiveData<List<Tour>> toursListCompleted;

    private LiveData<List<GeoPointPlanned>> geoPointsPlanned;

    private MutableLiveData<Integer> tourStatusLiveData;


    private LiveData<TourWithAllGeoPoints> tourWithAllGeoPoints;

    private MutableLiveData<Integer> statusInteger;
    private MutableLiveData<GeoPointActual> geoPointActualLiveData;
    private MutableLiveData<Data> weatherDataPlannginStartPoint;
    private MutableLiveData<Data> weatherDataPlanningEndPoint;


    public Repository(Context applicationContext) {
        MyTourAssistentDatabase db = MyTourAssistentDatabase.getDatabase(applicationContext);
        toursDAO = db.toursDAO();
        geoPointsPlannedDAO = db.geoPointsPlannedDAO();
        geoPointsActualDAO = db.geoPointsActualDAO();
        toursList = new MutableLiveData<>();
        geoPointsPlanned = new MutableLiveData<>();
        tourWithAllGeoPoints = new MutableLiveData<>();
        statusInteger = new MutableLiveData<>();
        geoPointActualLiveData = new MutableLiveData<>();
        tourStatusLiveData = new MutableLiveData<>();
        weatherDataPlannginStartPoint = new MutableLiveData<>();
        weatherDataPlanningEndPoint = new MutableLiveData<>();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherApi = retrofit.create(WeatherAPI.class);
    }

    public static Repository getInstance(Context applicationContext) {
        if (repository == null) {
            repository = new Repository(applicationContext);
        }
        return repository;
    }


    public void addTour(String tourName, long startTime, long endTime, int tourType, int tourStatus, ArrayList<GeoPoint> geoPoints) {

        MyTourAssistentDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long tourId = toursDAO.insert(new Tour(tourName, startTime, endTime, -1, -1, "", tourStatus, tourType));
                long order = 1;
                for (GeoPoint gp : geoPoints) {
                    geoPointsPlannedDAO.insert(new GeoPointPlanned(gp.getLatitude(), gp.getLongitude(), tourId, order));
                    order++;
                    //update livedata here
                    statusInteger.postValue(1);
                }
            } catch (SQLiteConstraintException e) {
                statusInteger.postValue(2);
            }
        });
    }


    public LiveData<List<Tour>> getAllUncompletedTours(boolean mIsFirstTime) {
        if (!mIsFirstTime) {
            return toursList;
        }
        toursList = toursDAO.getAllUncompletedTours();

        return toursList;

        //they will observe live data value from here
    }

    public LiveData<List<GeoPointPlanned>> getGeoPointsPlanned(long tourId, boolean mIsFirstTime) {
        if (!mIsFirstTime) {
            return geoPointsPlanned;
        }
        //     MyTourAssistentDatabase.databaseWriteExecutor.execute(()->{
        geoPointsPlanned = geoPointsPlannedDAO.getGeoPointsPlanned(tourId);
        //   });
        return geoPointsPlanned;

    }

    public MutableLiveData<Tour> getTour(long tourId) {
        return new MutableLiveData<Tour>();

    }

    public LiveData<TourWithAllGeoPoints> getTourWithAllGeoPoints(long tourId, boolean mIsFirstTime) {
        if (!mIsFirstTime) {
            return tourWithAllGeoPoints;
        }
        //MyTourAssistentDatabase.databaseWriteExecutor.execute(()-> {
        tourWithAllGeoPoints = toursDAO.getTourWithAllGeoPoints(tourId);
        //});
        return tourWithAllGeoPoints;
    }

    public void deleteTour(long tourId) {
        MyTourAssistentDatabase.databaseWriteExecutor.execute(() -> {
            toursDAO.delete(tourId);
        });

    }

    public void addGeoPointsActual(GeoPointActual gpa) {
        MyTourAssistentDatabase.databaseWriteExecutor.execute(() -> {
            try {
                geoPointsActualDAO.insert(gpa);
                geoPointActualLiveData.postValue(gpa);
                //set the gpa to our livedataobject
                //start listen to livedata from ActiveTourActivity
            } catch (SQLiteConstraintException e) {
            }
        });


    }

    public void startTour(long tourId, long startTime, int status) {
        MyTourAssistentDatabase.databaseWriteExecutor.execute(() -> {
            try {
                toursDAO.startTour(tourId, startTime, status);
            } catch (SQLiteConstraintException e) {
            }
        });

    }

    public void clearGeoPoints(long tourId) {
        MyTourAssistentDatabase.databaseWriteExecutor.execute(() -> {
            geoPointsActualDAO.clear(tourId);
        });

    }

    public void updateTour(Tour tour) {
        MyTourAssistentDatabase.databaseWriteExecutor.execute(()-> {
            toursDAO.update(tour);
            tourStatusLiveData.postValue(tour.tourStatus);
        });
    }

    public LiveData<List<Tour>> getAllCompletedTours(boolean mIsFirstTime) {
        if (!mIsFirstTime) {
            return toursListCompleted;
        }
        toursListCompleted = toursDAO.getAllCompletedTours();

        return toursListCompleted;

        //they will observe live data value from here
    }

    public long getLastInsertedGeoPointActualId(long tourId) {
         return geoPointsActualDAO.getLastInsertedId(tourId);
    }

    public LiveData<Integer> getStatusInteger() {
        return statusInteger;
    }

    public void setStatusInteger(MutableLiveData<Integer> statusInteger) {
        this.statusInteger = statusInteger;
    }

    public LiveData<GeoPointActual> getLastGeoPointRecorded() {
        return geoPointActualLiveData;
    }

    public LiveData<Integer> getTourStatus() {
        return tourStatusLiveData;
    }

    public void checkIfActiveTourExists(long tourId) {

    }

    public void getWeatherData(double latitude, double longitude, Date date, boolean isFirstGp) {
        Call<WeatherApiResponse> call = weatherApi.getWeatherData(String.valueOf(latitude), String.valueOf(longitude));
        call.enqueue(new Callback<WeatherApiResponse>() {
            @Override
            public void onResponse(Call<WeatherApiResponse> call, Response<WeatherApiResponse> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                WeatherApiResponse weatherApiResponse = response.body();
                SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd");
                List<Timeseries> timeseriesList = new ArrayList<>();
                String ourFormattedDate = formatter.format(date);
                if(weatherApiResponse.getProperties().getTimeseries()!=null&&weatherApiResponse.getProperties().getTimeseries().size()>0){
            //iterate over all timeseries and only get those records which match with our date
                    for(Timeseries timeSeriesItem:weatherApiResponse.getProperties().getTimeseries()){
                            if(timeSeriesItem.getTime().startsWith(ourFormattedDate)){
                                timeseriesList.add(timeSeriesItem);
                            }
                    }
                    //now we have entries of single day
                }
                Log.d("DebugDate", "onResponse:before  "+timeseriesList.size());
                if (timeseriesList.size() == 0) {
                    if (isFirstGp){
                        weatherDataPlannginStartPoint.postValue(null);
                    }else{
                        weatherDataPlanningEndPoint.postValue(null);
                    }

                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);//current format is numberic but we want to
                    //get it as 11:00:00
                    String hourToCompare = hour+":00:00";
                    Log.d("DebugDate", "onResponse: "+timeseriesList.size());
                    boolean foundMatch = false;
                    for (Timeseries hourlyTimeSeries : timeseriesList) {
                        //compare with hour
                        if (hourlyTimeSeries.getTime().contains(hourToCompare)){
                            //we have found the date which we
                            foundMatch = true;
                            if (isFirstGp){
                                Log.d("DebugDate", "onResponse: isFirstGp ");
                                weatherDataPlannginStartPoint.postValue(hourlyTimeSeries.getData());
                                break;
                            }else{
                                Log.d("DebugDate", "onResponse: NOT isFirstGp ");
                                weatherDataPlanningEndPoint.postValue(hourlyTimeSeries.getData());
                                break;
                            }
                        }
                    }
                    if (!foundMatch){
                        if (isFirstGp){
                            weatherDataPlannginStartPoint.postValue(null);
                        }else{
                            weatherDataPlanningEndPoint.postValue(null);
                        }
                    }

                    Log.d("DebugDate", "onResponse:after time series ");
                }
            }

            @Override
            public void onFailure(Call<WeatherApiResponse> call, Throwable t) {
                Log.d("DebugDate", "onFailure: ");
                if (isFirstGp){
                    weatherDataPlannginStartPoint.postValue(null);
                }else{
                    weatherDataPlanningEndPoint.postValue(null);
                }
            }
        });
    }

    public LiveData<Data> getFirstWeatherLiveData(){
        return weatherDataPlannginStartPoint;
    }
    public LiveData<Data> getLastWeatherLiveData(){
        return weatherDataPlanningEndPoint;
    }
}
