package com.aphex.mytourassistent.views.fragments.tours.completed;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.databinding.FragmentCompletedToursListBinding;
import com.aphex.mytourassistent.repository.db.entities.Tour;
import com.aphex.mytourassistent.viewmodels.ToursViewModel;
import com.aphex.mytourassistent.views.fragments.tours.MyToursListFragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class CompletedToursListFragment extends Fragment {


    private static final String ARG_COLUMN_COUNT = "column-count";
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
        toursViewModel.getAllCompletedTours(mIsFirstTime).observe(requireActivity(), tours -> {
            if (!isAdded()) {
                return;
            }
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setTitle(R.string.btn_delete_tour);
                    builder.setMessage(R.string.are_you_sure_to_delete_tour);
                    builder.setPositiveButton(R.string.btn_yes, (dialog, which) -> toursViewModel.deleteTour(tourId));
                    builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
                    builder.show();

                }
            });

        });

    }
}