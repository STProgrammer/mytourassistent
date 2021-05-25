package com.aphex.mytourassistent.views.fragments.tours;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.databinding.FragmentCompletedToursListBinding;
import com.aphex.mytourassistent.repository.db.entities.Tour;
import com.aphex.mytourassistent.viewmodels.ToursViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class CompletedToursListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private ToursViewModel toursViewModel;
    private boolean mIsFirstTime;
    private FragmentCompletedToursListBinding binding;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CompletedToursListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MyToursListFragment newInstance(int columnCount) {
        MyToursListFragment fragment = new MyToursListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCompletedToursListBinding.inflate(LayoutInflater.from(container.getContext()));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            mIsFirstTime = true;
        }

        toursViewModel = new ViewModelProvider(requireActivity()).get(ToursViewModel.class);
        //fetch data
        toursViewModel.getAllCompletedTours(mIsFirstTime).observe(requireActivity(), new Observer<List<Tour>>() {
            @Override
            public void onChanged(List<Tour> tours) {
                if (tours.isEmpty()) {
                    binding.emptyPlaceHolderTextView.setVisibility(View.VISIBLE);
                    binding.list.setVisibility(View.GONE);
                    return;
                }
                //we will have all the tours here when database returns values
                // Set the adapter
                    if (mColumnCount <= 1) {
                        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
                    } else {
                        binding.list.setLayoutManager(new GridLayoutManager(requireContext(), mColumnCount));
                    }
                    MyCompletedToursListRecyclerViewAdapter myCompletedToursListRecyclerViewAdapter = new MyCompletedToursListRecyclerViewAdapter(tours);
                    binding.list.setAdapter(myCompletedToursListRecyclerViewAdapter);
                    myCompletedToursListRecyclerViewAdapter.setOnClickButton(new MyCompletedToursListRecyclerViewAdapter.OnClickButton() {
                        @Override
                        public void onClickToDetailsFragment(long tourId) {
                            Navigation.findNavController(view).navigate(CompletedToursListFragmentDirections.completedToursListFragmentToCompletedTourDetailsFragment().setTOURID(tourId));
                        }

                        @Override
                        public void onClickToDeleteTour(long tourId) {
                            toursViewModel.deleteTour(tourId);
                        }
                    });

            }
        });

    }
}