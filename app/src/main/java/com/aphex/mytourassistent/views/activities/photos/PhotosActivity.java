package com.aphex.mytourassistent.views.activities.photos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;


import android.os.Bundle;
import android.view.LayoutInflater;

import com.aphex.mytourassistent.databinding.ActivityPhotosBinding;
import com.aphex.mytourassistent.viewmodels.PhotosViewModel;

public class PhotosActivity extends AppCompatActivity {

    private FragmentStateAdapter pagerAdapter;
    private int NUM_PAGES = 0;
    private PhotosViewModel photosViewModel;
    private long geoPointId;
    private ActivityPhotosBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        binding = ActivityPhotosBinding.inflate(layoutInflater);
        setContentView(binding.getRoot());




        geoPointId = getIntent().getLongExtra("GPA_ID", 0);

        // Oppretter (/finner) ViewModel-objektet:
        photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
        // Legger data i ViewModel-objektet:
        // Abonnerer på endringer:
        photosViewModel.loadPhotos(geoPointId);
        photosViewModel.getPhotos().observe(this, photos -> {

           NUM_PAGES = photos.size();
           pagerAdapter = new ScreenSlidePagerAdapter(this);
           binding.viewPager.setAdapter(pagerAdapter);
        });
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            return PhotoSlidePageFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}