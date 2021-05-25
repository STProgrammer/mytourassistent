package com.aphex.mytourassistent.views.activities.photos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aphex.mytourassistent.repository.Repository;
import com.aphex.mytourassistent.repository.db.entities.Photo;

import java.util.ArrayList;
import java.util.List;

// ViewModel:
public class PhotosViewModel extends AndroidViewModel {


    private Repository repository;
    private LiveData<List<Photo>> photos;

    public PhotosViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);

    }


    public LiveData<List<Photo>> getPhotos() {
        return photos;
    }

    public void loadPhotos(long geoPointId) {
        photos = repository.getPhotos(geoPointId);
    }
}
