package com.aphex.mytourassistent.tours;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.activetour.ActiveTourActivity;
import com.aphex.mytourassistent.databinding.FragmentMyToursBinding;
import com.aphex.mytourassistent.entities.Tour;
import com.aphex.mytourassistent.entities.TourWithGeoPointsPlanned;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.aphex.mytourassistent.entities.Tour}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyMyTourRecyclerViewAdapter extends RecyclerView.Adapter<MyMyTourRecyclerViewAdapter.ViewHolder> {

    private final List<Tour> mTours;
    private FragmentMyToursBinding binding;

    private Context context;

    public MyMyTourRecyclerViewAdapter(List<Tour> items) {
        mTours = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(FragmentMyToursBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bind(mTours.get(position));
    }

    @Override
    public int getItemCount() {
        return mTours.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private FragmentMyToursBinding binding;

        public ViewHolder(FragmentMyToursBinding viewBinding) {
            super(viewBinding.getRoot());
            binding = viewBinding;
        }

        void bind(Tour tour) {
            String date = new SimpleDateFormat("yyyy-MM-dd")
                    .format(new Date(tour.startTimePlanned));
            binding.itemTitle.setText(tour.title);
            binding.itemTourType.setText(tour.tourType);
            binding.itemTourStatus.setText(tour.tourStatus);
            binding.itemTourDate.setText(date);
            if (tour.tourStatus == "Completed") {
                binding.btnTourStart.setVisibility(View.INVISIBLE);
            } else {
                binding.btnTourStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActiveTourActivity(tour.tourId);

                    }
                });
            }
        }
    }

    public void startActiveTourActivity(long tourId) {
        Intent intent = new Intent(context, ActiveTourActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("TOUR_ID", tourId);
        context.startActivity(intent);
    }

}