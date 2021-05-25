package com.aphex.mytourassistent.views.activities.photos;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.databinding.ActivityActiveTourBinding;
import com.aphex.mytourassistent.databinding.FragmentAddTourBinding;
import com.aphex.mytourassistent.databinding.FragmentPhotoSlidePageBinding;
import com.aphex.mytourassistent.repository.db.entities.Photo;
import com.bumptech.glide.Glide;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhotoSlidePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoSlidePageFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private int position;

    private PhotosViewModel myViewModel;

    private FragmentPhotoSlidePageBinding binding;

    public PhotoSlidePageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param position Parameter 1.
     * @return A new instance of fragment UtstyrSlidePageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhotoSlidePageFragment newInstance(int position) {
        PhotoSlidePageFragment fragment = new PhotoSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            this.position = getArguments().getInt(ARG_PARAM1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPhotoSlidePageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myViewModel = new ViewModelProvider(requireActivity()).get(PhotosViewModel.class);
        myViewModel.getPhotos().observe(getViewLifecycleOwner(), photos -> {
            // Getting image from the position:
            Photo photo = photos.get(this.position);

            // Showing image
            Glide
                .with(this)
                .load(photo.imageUri)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_search_24)
                .error(R.drawable.ic_baseline_error_24)
                .into(binding.ivPhoto);
        });
    }
}