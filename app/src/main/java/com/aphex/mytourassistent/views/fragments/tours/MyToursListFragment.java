package com.aphex.mytourassistent.views.fragments.tours;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.viewmodels.ToursViewModel;
import com.aphex.mytourassistent.views.activities.ActiveTourActivity;
import com.aphex.mytourassistent.services.TourTrackingService;
import com.aphex.mytourassistent.repository.db.entities.Tour;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class MyToursListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private ToursViewModel toursViewModel;
    private boolean mIsFirstTime;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MyToursListFragment() {
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
        View view = inflater.inflate(R.layout.fragment_my_tours_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            mIsFirstTime = true;
        }

        toursViewModel = new ViewModelProvider(requireActivity()).get(ToursViewModel.class);
        //fetch data
        toursViewModel.getAllUncompletedTours(mIsFirstTime).observe(requireActivity(), new Observer<List<Tour>>() {
            @Override
            public void onChanged(List<Tour> tours) {
                //we will have all the tours here when database returns values
                //calculate the stuff once
                toursViewModel.setCurrentActiveTour(tours);
                // Set the adapter
                if (view instanceof RecyclerView) {
                    Context context = view.getContext();
                    RecyclerView recyclerView = (RecyclerView) view;
                    if (mColumnCount <= 1) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    } else {
                        recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                    }
                    MyTourRecyclerViewAdapter myMyTourRecyclerViewAdapter = new MyTourRecyclerViewAdapter(tours);
                    recyclerView.setAdapter(myMyTourRecyclerViewAdapter);
                    myMyTourRecyclerViewAdapter.setOnClickButton(new MyTourRecyclerViewAdapter.OnClickButton() {
                        @Override
                        public void onClickToDeleteTour(long tourId) {
                            toursViewModel.deleteTour(tourId);
                            if (toursViewModel.getCurrentActiveTour() == tourId) {
                                context.stopService(new Intent(requireContext(), TourTrackingService.class));
                            }

                        }

                        @Override
                        public void onClickStartActiveTourActivity(long tourId, int tourStatus) {
                            //check if we can go futher or not
                            //check db
                            boolean tourStarted;
                            if (toursViewModel.getCurrentActiveTour() == -1){
                                startActiveTourActivity(tourId, tourStatus);
                            } else if (toursViewModel.getCurrentActiveTour() == tourId) {
                                startActiveTourActivity(tourId, tourStatus);
                            } else {
                                Toast.makeText(requireContext(), R.string.toast_alread_active_tour, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
    public void startActiveTourActivity(long tourId, int tourStatus) {
        Intent intent = new Intent(requireContext(), ActiveTourActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("TOUR_ID", tourId);
        intent.putExtra("TOUR_STATUS", tourStatus);
        requireContext().startActivity(intent);
    }
}